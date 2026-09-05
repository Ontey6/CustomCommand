package ontey.ccmd.command.suggestion.entry;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

record IntegerSuggestionEntry(int integer, @Nullable String tooltip) implements SuggestionEntry {
	
	@Override
	public void suggestIn(@NonNull SuggestionsBuilder builder, @Nullable String input) {
		if(input == null || String.valueOf(integer).startsWith(input))
			builder.suggest(integer, new LiteralMessage(tooltip));
	}
	
	@Override
	public @NonNull Object value() {
		return integer;
	}
	
	@Override
	public @NonNull Map<String, Object> serialize() {
		if(tooltip != null)
			return Map.of("integer", integer, "tooltip", tooltip);
		else
			return Map.of("value", integer);
	}
}
