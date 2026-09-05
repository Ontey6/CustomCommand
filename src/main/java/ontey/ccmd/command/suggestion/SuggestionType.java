package ontey.ccmd.command.suggestion;

import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ontey.api.config.ConfigSection;
import ontey.api.javascript.JavaScriptException;
import ontey.api.javascript.Javascript;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.command.suggestion.entry.SuggestionEntry;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static ontey.ccmd.util.JavascriptUtil.*;

@AllArgsConstructor
public enum SuggestionType {
	LIST((context, section) -> {
		List<?> rawSuggestions = section.getList("list");
		
		if(rawSuggestions == null)
			throw context.newException("Specifies the LIST suggestion type, but doesn't specify a (valid) list ('suggestions.list' is either not set or not a list)");
		
		List<SuggestionEntry> suggestions = new ArrayList<>(rawSuggestions.size());
		
		for(Object rawSuggestion : rawSuggestions)
			suggestions.add(SuggestionEntry.deserialize(rawSuggestion));
		
		if(suggestions.contains(null))
			throw context.newException("Suggestions list contains invalid suggestions");
		
		boolean dynamicSuggestions = section.getBoolean("dynamic");
		
		return (_, suggestionsBuilder) -> {
			var remaining = suggestionsBuilder.getRemainingLowerCase();
			for(var suggestion : suggestions)
				suggestion.suggestIn(suggestionsBuilder, dynamicSuggestions ? remaining : null);
			
			return suggestionsBuilder.buildFuture();
		};
	}),
	JAVASCRIPT((context, section) -> {
		var code = section.getString("javascript");
		
		if(code == null)
			throw context.newException("Specifies the JAVASCRIPT suggestion type, but doesn't specify a javascript String ('suggests.javascript' is not set)");
		
		return parseJavascript(code, context);
	}),
	JAVASCRIPT_REFERENCE((context, section) -> {
		String filename = section.getString("javascript-file");
		
		if(filename == null)
			throw context.newException("Specifies the JAVASCRIPT_REFERENCE suggestion type, but doesn't specify a javascript reference file ('suggests.javascript-file' is not set)");
		
		String code = getFileContents(context, filename);
		
		return parseJavascript(code, context);
	});
	
	@Getter
	private final SuggestionsParser action;
	
	public static SuggestionProvider<CommandSourceStack> parseSuggestions(ParseContext context, ConfigSection section) {
		var suggestionType = section.getEnum("type", SuggestionType.class);
		
		if(suggestionType == null)
			throw context.newException("Doesn't specify a valid type ('suggests.type' is either not set or invalid)");
		
		return suggestionType.action.parseSuggestions(context, section);
	}
	
	private static SuggestionProvider<CommandSourceStack> parseJavascript(String code, ParseContext context) {
		
		return (ctx, suggestionsBuilder) -> {
			Javascript javascript = createBaseJavascript();
			addContextToJavascript(ctx, javascript);
			addSuggestionsToJavascript(suggestionsBuilder, javascript);
			
			try {
				var function = (Function<Object[], Object>) javascript.eval(code);
				
				if(function == null)
					throw context.newException("The javascript doesn't return anything (Should return an arrow function returning a list like '() => [1, 2, 3, \"Hello World\"]')");
				
				var list = (List<?>) function.apply(new Object[0]);
				
				for(var suggestion : list) {
					var suggester = suggestion instanceof SuggestionEntry entry ? entry : SuggestionEntry.deserialize(suggestion);
					
					if(suggester == null)
						throw context.newException("An entry in the javascript suggestions is invalid");
					
					suggester.suggestIn(suggestionsBuilder, suggestionsBuilder.getRemainingLowerCase());
				}
				
				return suggestionsBuilder.buildFuture();
			} catch(JavaScriptException e) {
				throw context.newException("A javascript error occurred.", e);
			} catch(ClassCastException e) {
				throw context.newException("The javascript doesn't return the right type (It should be an arrow function returning a list like '() => [1, 2, 3, \"Hello World\"]')", e);
			}
		};
	}
	
	public interface SuggestionsParser {
		
		SuggestionProvider<CommandSourceStack> parseSuggestions(ParseContext context, ConfigSection requiresSection);
	}
}
