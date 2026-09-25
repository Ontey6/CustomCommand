package ontey.ccmd.cooldown.message;

import lombok.NonNull;

record PlainCooldownMessage(@NonNull String message) implements CooldownMessage {
	
	@Override
	public @NonNull String format(@NonNull String formattedDuration) {
		return message;
	}
	
	@Override
	public @NonNull String serialize() {
		return message;
	}
}
