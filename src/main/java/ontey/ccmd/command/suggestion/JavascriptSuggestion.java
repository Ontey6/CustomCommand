package ontey.ccmd.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.NonNull;
import ontey.api.javascript.JavaScriptException;
import ontey.api.javascript.Javascript;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.command.suggestion.entry.SuggestionEntry;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static ontey.ccmd.util.JavascriptUtil.*;

public record JavascriptSuggestion(String javascript) implements Suggestion {
	
	@Override
	public @NonNull List<? extends @NonNull SuggestionEntry> parseSuggestionList(@NonNull ParseContext context, @NonNull CommandContext<CommandSourceStack> commandContext, @NonNull SuggestionsBuilder suggestionsBuilder) {
		Javascript javascript = createBaseJavascript();
		addContextToJavascript(commandContext, javascript);
		addSuggestionsToJavascript(suggestionsBuilder, javascript);
		
		try {
			var function = (Function<Object[], Object>) javascript.eval(this.javascript);
			
			if(function == null)
				throw context.newException("The javascript doesn't return anything (Should return an arrow function returning a list like '() => [1, 2, 3, \"Hello World\"]')");
			
			var list = (List<?>) function.apply(new Object[0]);
			
			return list.stream().map(suggestion -> {
				var entry = suggestion instanceof SuggestionEntry entry1 ? entry1 : SuggestionEntry.deserialize(suggestion);
				
				if(entry == null)
					throw context.newException("An entry in the javascript suggestions is invalid");
				
				return entry;
			}).toList();
		} catch(JavaScriptException e) {
			throw context.newException("A javascript error occurred.", e);
		} catch(ClassCastException e) {
			throw context.newException("The javascript doesn't return the right type (It should be an arrow function returning a list like '() => [1, 2, 3, \"Hello World\"]')", e);
		}
	}
	
	@Override
	public @NotNull Map<String, Object> serialize() {
		return Map.of(
		  "type", "javascript",
		  "javascript", javascript
		);
	}
}
