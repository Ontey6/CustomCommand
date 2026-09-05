package ontey.ccmd.command.suggestion.entry;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

record StringSuggestionEntry(@NonNull String value, @Nullable String tooltip) implements SuggestionEntry {
	
	@Override
	public void suggestIn(@NonNull SuggestionsBuilder builder, @Nullable String input) {
		if(input == null || value.toLowerCase().startsWith(input))
			builder.suggest(value, new LiteralMessage(tooltip));
	}
	
	@Override
	public @NonNull Map<String, Object> serialize() {
		if(tooltip != null)
			return Map.of("value", value, "tooltip", tooltip);
		else
			return Map.of("value", value);
	}
}
