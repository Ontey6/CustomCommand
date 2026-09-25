package ontey.ccmd.cooldown.message;

import lombok.NonNull;

class EmptyCooldownMessage implements CooldownMessage {
	
	public static final EmptyCooldownMessage INSTANCE = new EmptyCooldownMessage();
	
	private EmptyCooldownMessage() {
	
	}
	
	@Override
	public @NonNull String format(@NonNull String formattedDuration) {
		return "";
	}
	
	@Override
	public @NonNull String serialize() {
		return "";
	}
}
