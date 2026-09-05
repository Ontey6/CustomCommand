package ontey.ccmd.command.requirement.registry;

import lombok.NonNull;
import net.kyori.adventure.key.Key;
import ontey.ccmd.command.requirement.creator.RequirementCreator;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class RequirementRegistry {
	
	private static final Map<Key, RequirementCreator> map = new HashMap<>();
	
	private RequirementRegistry() {
		throw new UnsupportedOperationException();
	}
	
	@Nullable
	public static RequirementCreator get(@NonNull Key key) {
		return map.get(key);
	}
	
	public static void register(@NonNull Key key, @NonNull RequirementCreator creator) {
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
