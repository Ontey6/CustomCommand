package ontey.ccmd.command.component.registry;

import com.mojang.brigadier.arguments.ArgumentType;
import lombok.NonNull;
import net.kyori.adventure.key.Key;
import ontey.ccmd.command.component.creator.ArgumentTypeCreator;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public final class ArgumentTypeRegistry {
	
	private static final Map<Key, ArgumentTypeCreator> map = new HashMap<>();
	
	private ArgumentTypeRegistry() {
		throw new UnsupportedOperationException();
	}
	
	@Nullable
	public static ArgumentTypeCreator get(@NonNull Key key) {
		return map.get(key);
	}
	
	public static void register(@NonNull Key key, @NonNull ArgumentTypeCreator creator) {
		if(map.containsKey(key))
			throw new IllegalStateException("Execution component '" + key.asString() + "' is already registered");
		
		map.put(key, creator);
	}
	
	public static void register(@NonNull Key key, @NonNull Supplier<@NonNull ArgumentType<?>> creator) {
		register(key, _ -> creator.get());
	}
	
	public static void unregister(@NonNull Key key) {
		if(!map.containsKey(key))
			throw new IllegalStateException("Execution component '" + key.asString() + "' is not registered, can not be unregistered");
		
		map.remove(key);
	}
}
