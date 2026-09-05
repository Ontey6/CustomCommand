package ontey.ccmd.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.NonNull;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.command.suggestion.entry.SuggestionEntry;
import ontey.ccmd.util.JavascriptUtil;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

public record JavascriptReferenceSuggestion(String javascriptFile) implements Suggestion {
	
	@Override
	public @NonNull List<? extends @NonNull SuggestionEntry> parseSuggestionList(@NonNull ParseContext context, @NonNull CommandContext<CommandSourceStack> commandContext, @NonNull SuggestionsBuilder suggestionsBuilder) {
		var javascript = JavascriptUtil.getFileContents(context, javascriptFile);
		return new JavascriptSuggestion(javascript)
		  .parseSuggestionList(context, commandContext, suggestionsBuilder);
	}
	
	@Override
	public @NotNull Map<String, Object> serialize() {
		return Map.of(
		  "type", "javascript_reference",
		  "javascript-file", javascriptFile
		);
	}
}
