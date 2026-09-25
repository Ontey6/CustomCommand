package ontey.ccmd.cooldown.message;

import lombok.NonNull;

record BasicCooldownMessage(@NonNull String before, @NonNull String after) implements CooldownMessage {
	
	@NonNull
	public String serialize() {
		return before + SERIALIZATION_SPLITTER + after;
	}
	
	@Override
	public @NonNull String format(@NonNull String formattedDuration) {
		return before + formattedDuration + after;
	}
}