package ontey.ccmd.cooldown.owner;

import lombok.NonNull;
import net.kyori.adventure.key.InvalidKeyException;
import net.kyori.adventure.key.Key;
import ontey.api.serialization.CombinedConfigSerializable;
import org.bukkit.Bukkit;
import org.bukkit.block.Block;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/// A wrapper for a specific type of [CommandSender] that is serializable and can be on cooldown

public interface CooldownOwner extends CombinedConfigSerializable {
	
	@Nullable
	static CooldownOwner of(@NonNull CommandSender sender) {
		return switch(sender) {
			case Player player -> CooldownOwner.player(player);
			case BlockCommandSender blockSender -> CooldownOwner.block(blockSender.getBlock());
			case ConsoleCommandSender _ -> CooldownOwner.console();
			default -> null;
		};
	}
	
	@SuppressWarnings("PatternValidation")
	@NonNull
	static CooldownOwner deserialize(@NonNull Map<String, Object> map) {
		var rawType = map.get("type");
		
		if(rawType == null)
			throw new IllegalArgumentException("'type' not set");
		
		var type = rawType.toString();
		
		return switch(type) {
			case "console" -> ConsoleCooldownOwner.INSTANCE;
			case "player" -> {
				var rawUuid = map.get("uuid");
				
				if(rawUuid == null)
					throw new IllegalArgumentException("'uuid' not set");
				
				try {
					var uuid = UUID.fromString(rawUuid.toString());
					var player = Bukkit.getOfflinePlayer(uuid);
					var name = player.getName();
					
					yield new PlayerCooldownOwner(uuid, name);
				} catch(IllegalArgumentException e) {
					throw new IllegalArgumentException("'uuid' is malformed");
				}
			}
			case "block" -> {
				var rawWorldKey = map.get("world");
				var rawX = map.get("x");
				var rawY = map.get("y");
				var rawZ = map.get("z");
				
				if(rawWorldKey == null)
					throw new IllegalArgumentException("'world' not set");
				
				Key worldKey;
				try {
					worldKey = Key.key(rawWorldKey.toString());
				} catch(InvalidKeyException e) {
					throw new IllegalArgumentException("'world' is not a valid key");
				}
				
				if(!(rawX instanceof Integer x) || !(rawY instanceof Integer y) || !(rawZ instanceof Integer z))
					throw new IllegalArgumentException("'x', 'y' or 'z' is not an integer");
				
				yield new BlockCooldownOwner(worldKey, x, y, z);
			}
			default -> throw new IllegalArgumentException("'type' doesn't exist or is invalid");
		};
	}
	
	@NonNull
	static CooldownOwner player(@NonNull Player player) {
		return new PlayerCooldownOwner(player.getUniqueId(), player.getName());
	}
	
	@NonNull
	static CooldownOwner block(@NonNull Block block) {
		return new BlockCooldownOwner(block.getWorld().key(), block.getX(), block.getY(), block.getZ());
	}
	
	static CooldownOwner console() {
		return ConsoleCooldownOwner.INSTANCE;
	}
	
	/// @return A human-readable name
	
	@NonNull
	@Contract(pure = true)
	String displayName();
	
	/// @return The serialized extra data this `CooldownOwner` carries
	
	@NonNull
	@Contract(pure = true)
	Map<String, String> serializeExtraData();
	
	/// @return A string representation of the type that is used for serialization
	
	@NonNull
	@Contract(pure = true)
	String type();
	
	@Override
	@Unmodifiable
	default @NonNull Map<String, Object> serialize() {
		Map<String, Object> map = new HashMap<>();
		
		map.put("type", type());
		map.putAll(serializeExtraData());
		
		return Map.copyOf(map);
	}
}
