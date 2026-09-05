package ontey.ccmd.command.execution.registry;

import lombok.NonNull;
import net.kyori.adventure.key.Key;
import ontey.ccmd.command.execution.creator.ExecutionCreator;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public final class ExecutionRegistry {
	
	private static final Map<Key, ExecutionCreator> map = new HashMap<>();
	
	private ExecutionRegistry() {
		throw new UnsupportedOperationException();
	}
	
	@Nullable
	public static ExecutionCreator get(@NonNull Key key) {
		return map.get(key);
	}
	
	public static void register(@NonNull Key key, @NonNull ExecutionCreator creator) {
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
