package ontey.ccmd.command.suggestion.registry;

import lombok.NonNull;
import net.kyori.adventure.key.Key;
import ontey.ccmd.command.suggestion.creator.SuggestionCreator;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class SuggestionRegistry {
	
	private static final Map<Key, SuggestionCreator> map = new HashMap<>();
	
	private SuggestionRegistry() {
		throw new UnsupportedOperationException();
	}
	
	@Nullable
	public static SuggestionCreator get(@NonNull Key key) {
		return map.get(key);
	}
	
	public static void register(@NonNull Key key, @NonNull SuggestionCreator creator) {
		if(map.containsKey(key))
			throw new IllegalStateException("Execution component '" + key.asString() + "' is already registered");
		
		map.put(key, creator);
	}
	
	public static void unregister(@NonNull Key key) {
		if(!map.containsKey(key))
			throw new IllegalStateException("Execution component '" + key.asString() + "' is not registered, can not be unregistered");
		
		map.remove(key);
	}
}
