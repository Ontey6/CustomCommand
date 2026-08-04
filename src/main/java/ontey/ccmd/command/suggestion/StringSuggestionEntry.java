package ontey.ccmd.command.suggestion;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

record StringSuggestionEntry(@NonNull String value, @Nullable Message tooltip) implements SuggestionEntry {
	
	@Override
	public void suggestIn(@NonNull SuggestionsBuilder builder) {
		builder.suggest(value, tooltip);
	}
	
	@Override
	public Map<String, Object> serialize() {
		if(tooltip != null)
			return Map.of("value", value, "tooltip", tooltip.getString());
		else
			return Map.of("value", value);
	}
}
