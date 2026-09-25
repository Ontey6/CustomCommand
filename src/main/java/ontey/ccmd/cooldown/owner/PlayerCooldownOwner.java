package ontey.ccmd.cooldown.owner;

import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;

/// A [CooldownOwner] that uses a player's UUID to identify equality
/// @param uuid The UUID of the player
/// @param name The name of the player. Not stored on restarts.

record PlayerCooldownOwner(@NonNull UUID uuid, @Nullable String name) implements CooldownOwner {
	
	@Override
	public @NonNull String displayName() {
		return name != null ? name : uuid.toString();
	}
	
	@Override
	public @NonNull Map<String, String> serializeExtraData() {
		return Map.of("uuid", uuid.toString());
	}
	
	@Override
	public @NonNull String type() {
		return "player";
	}
}
