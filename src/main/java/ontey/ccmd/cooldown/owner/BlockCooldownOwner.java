package ontey.ccmd.cooldown.owner;

import lombok.NonNull;
import net.kyori.adventure.key.Key;

import java.util.Map;

/// A [CooldownOwner] that wraps the location of a command block

public record BlockCooldownOwner(@NonNull Key worldKey, int x, int y, int z) implements CooldownOwner {
	
	@Override
	public @NonNull String displayName() {
		return toString();
	}
	
	@Override
	public @NonNull Map<String, String> serializeExtraData() {
		return Map.of(
		  "world", worldKey.asString(),
		  "x", String.valueOf(x),
		  "y", String.valueOf(y),
		  "z", String.valueOf(z)
		);
	}
	
	@Override
	public @NonNull String type() {
		return "block";
	}
}
