package ontey.ccmd.cooldown.owner;

import lombok.NonNull;

import java.util.Map;

/// A [CooldownOwner] that represents the console
///
/// As there is only one console, this class is a singleton and equality will be compared using the memory address

final class ConsoleCooldownOwner implements CooldownOwner {
	
	private ConsoleCooldownOwner() {
	
	}
	
	public static final ConsoleCooldownOwner INSTANCE = new ConsoleCooldownOwner();
	
	@Override
	public @NonNull String displayName() {
		return "CONSOLE";
	}
	
	@Override
	public @NonNull Map<String, String> serializeExtraData() {
		return Map.of();
	}
	
	@Override
	public @NonNull String type() {
		return "console";
	}
}
