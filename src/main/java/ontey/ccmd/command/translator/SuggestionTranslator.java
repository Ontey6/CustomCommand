package ontey.ccmd.command.translator;

import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import ontey.api.config.ConfigSection;
import ontey.api.javascript.JavaScriptException;
import ontey.api.javascript.Javascript;
import ontey.ccmd.command.exception.CustomCommandParseException;
import ontey.ccmd.command.suggestion.SuggestionEntry;
import ontey.ccmd.command.translator.enums.SuggestionType;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

import static ontey.ccmd.command.translator.JavascriptUtil.*;

final class SuggestionTranslator {
	static void addSuggestions(RequiredArgumentBuilder<CommandSourceStack, ?> builder, ConfigSection section, String rootName, String name) {
		var suggestionType = section.getEnum("type", SuggestionType.class);
		
		if(suggestionType == null)
			throw new CustomCommandParseException("In command '" + rootName + "', the argument '" + name + "' specifies a 'suggests' section, but not a (valid) type! (suggests.type is either not set or an invalid value)");
		
		if(suggestionType == SuggestionType.LIST)
			addListSuggestions(builder, section, rootName, name);
		
		if(suggestionType == SuggestionType.JAVASCRIPT)
			addJavascriptSuggestions(builder, section, rootName, name);
	}
	
	private static void addListSuggestions(RequiredArgumentBuilder<CommandSourceStack, ?> builder, ConfigSection section, String rootName, String name) {
		List<?> rawSuggestions = section.getList("list");
		
		if(rawSuggestions == null)
			throw new CustomCommandParseException("In command '" + rootName + "', the argument '" + name + "' specifies the LIST suggestion type, but doesn't specify (a valid) one (suggests.list is either not set or an invalid value)");
		
		List<SuggestionEntry> suggestions = new ArrayList<>(rawSuggestions.size());
		
		for(Object rawSuggestion : rawSuggestions)
			suggestions.add(SuggestionEntry.deserialize(rawSuggestion));
		
		if(suggestions.contains(null))
			throw new CustomCommandParseException("In command '" + rootName + "', the argument '" + name + "''s list contains one or more invalid suggestions");
		
		boolean dynamicSuggestions = section.getBoolean("dynamic");
		
		builder.suggests((_, suggestionsBuilder) -> {
			var remaining = suggestionsBuilder.getRemainingLowerCase();
			for(var suggestion : suggestions)
				if(dynamicSuggestions)
					suggestion.suggestIn(suggestionsBuilder, remaining);
			
			return suggestionsBuilder.buildFuture();
		});
	}
	
	private static void addJavascriptSuggestions(RequiredArgumentBuilder<CommandSourceStack, ?> builder, ConfigSection section, String rootName, String name) {
		var code = section.getString("javascript");
		
		if(code == null)
			throw new CustomCommandParseException("In command '" + rootName + "', the argument '" + name + "' specifies the JAVASCRIPT suggestion type, but doesn't specify the javascript String (suggests.javascript doesn't exist)");
		
		Javascript javascript = createBaseJavascript();
		
		builder.suggests((ctx, suggestionsBuilder) -> {
			addContextToJavascript(ctx, javascript);
			addSuggestionsToJavascript(suggestionsBuilder, javascript);
			javascript
			  .addClass(SuggestionEntry.class)
			  .addVariable("stringSuggestion", (BiFunction<String, String, SuggestionEntry>) SuggestionEntry::string)
			  .addVariable("integerSuggestion", (BiFunction<Integer, String, SuggestionEntry>) SuggestionEntry::integer);
			
			try {
				var function = (Function<Object[], Object>) javascript.eval(code);
				
				if(function == null)
					throw new CustomCommandParseException("In command '" + rootName + "', the argument '" + name + "''s javascript suggestions don't return anything, but they should return an arrow function returning a list!");
				
				var list = (List<?>) function.apply(new Object[0]);
				
				for(var suggestion : list) {
					var suggester = suggestion instanceof SuggestionEntry entry ? entry : SuggestionEntry.deserialize(suggestion);
					
					if(suggester == null) {
						throw new CustomCommandParseException("A javascript error occurred.\nAn entry in the javascript suggestions list is of an invalid type!");
					}
					
					suggester.suggestIn(suggestionsBuilder, suggestionsBuilder.getRemainingLowerCase());
				}
				
				return suggestionsBuilder.buildFuture();
			} catch(JavaScriptException e) {
				throw new CustomCommandParseException("A javascript error occurred.\n" + e.getMessage());
			} catch(ClassCastException e) {
				throw new CustomCommandParseException("In command '" + rootName + "', the argument '" + name + "''s javascript suggestions don't return the right type. It has to be an arrow function that returns a list!");
			}
		});
	}
}
