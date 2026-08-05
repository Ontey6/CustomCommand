package ontey.ccmd.command.suggestion;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.Message;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import lombok.NonNull;
import ontey.api.config.serialization.ConfigSerializable;
import ontey.api.loader.AutoRegistered;
import org.graalvm.polyglot.HostAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@AutoRegistered
public interface SuggestionEntry extends ConfigSerializable {
	
	void suggestIn(@NonNull SuggestionsBuilder builder, @Nullable String input);
	
	@Nullable
	static SuggestionEntry deserialize(@NonNull Map<String, Object> map) {
		Object value = map.get("value");
		
		if(value == null)
			return null;
		
		Object rawTooltip = map.get("tooltip");
		Message tooltip = null;
		
		if(rawTooltip instanceof String str)
			tooltip = new LiteralMessage(str);
		
		if(value instanceof Integer i)
			return new IntegerSuggestionEntry(i, tooltip);
		
		if(value instanceof String str)
			return new StringSuggestionEntry(str, tooltip);
		
		return null;
	}
	
	static SuggestionEntry deserialize(@NonNull Object value) {
		return switch(value) {
			case Map<?, ?> _ -> deserialize((Map<String, Object>) value);
			case String str -> string(str);
			case Integer i -> integer(i);
			default -> null;
		};
	}
	
	@NonNull
	static SuggestionEntry string(@NonNull String string, @Nullable String tooltip) {
		return new StringSuggestionEntry(string, new LiteralMessage(tooltip));
	}
	
	@NonNull
	static SuggestionEntry string(@NonNull String string) {
		return new StringSuggestionEntry(string, null);
	}
	
	@NonNull
	@HostAccess.Export
	static SuggestionEntry integer(int integer, @Nullable String tooltip) {
		return new IntegerSuggestionEntry(integer, new LiteralMessage(tooltip));
	}
	
	@NonNull
	static SuggestionEntry integer(int integer) {
		return new IntegerSuggestionEntry(integer, null);
	}
}
