package ontey.ccmd.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.NonNull;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.command.suggestion.entry.SuggestionEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

public record ListSuggestion(List<? extends SuggestionEntry> suggestions, boolean dynamic) implements Suggestion {
	
	@Override
	public @NonNull SuggestionType type() {
		return SuggestionType.LIST;
	}
	
	@Override
	public @NonNull List<? extends @NonNull SuggestionEntry> parseSuggestionList(@Nullable ParseContext a, @Nullable CommandContext<CommandSourceStack> c, @Nullable SuggestionsBuilder d) {
		return suggestions;
	}
	
	public @NonNull List<? extends @NonNull SuggestionEntry> parseSuggestions() {
		return suggestions;
	}
	
	@Override
	public @NotNull Map<String, Object> serialize() {
		return Map.of(
		  "type", "LIST",
		  "dynamic", dynamic,
		  "list", suggestions.stream().map(entry -> {
			  var tooltip = entry.tooltip();
			  if(tooltip == null)
				  return entry.value();
			  else
				  return Map.of(
					 "value", entry.value(),
					 "tooltip", tooltip
				  );
		  })
		);
	}
}
