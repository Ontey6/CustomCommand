package ontey.ccmd.cooldown.message;

import lombok.NonNull;

public interface CooldownMessage {
	
	String SERIALIZATION_SPLITTER = "<cooldown>";
	
	CooldownMessage DEFAULT_COOLDOWN_MESSAGE = new BasicCooldownMessage("Command is on a cooldown! Wait ", "");
	
	@NonNull
	String format(@NonNull String formattedDuration);
	
	@NonNull
	String serialize();
	
	@NonNull
	static CooldownMessage deserialize(@NonNull String input) {
		if(input.isEmpty())
			return empty();
		
		var index = input.indexOf(SERIALIZATION_SPLITTER);
		
		if(index == -1)
			return new PlainCooldownMessage(input);
		
		var before = input.substring(0, index);
		var after = input.substring(index + SERIALIZATION_SPLITTER.length());
		
		return new BasicCooldownMessage(before, after);
	}
	
	@NonNull
	static CooldownMessage empty() {
		return EmptyCooldownMessage.INSTANCE;
	}
	
	@NonNull
	static CooldownMessage defaultMessage() {
		return DEFAULT_COOLDOWN_MESSAGE;
	}
	
	@NonNull
	static CooldownMessage plain(@NonNull String message) {
		return new PlainCooldownMessage(message);
	}
	
	@NonNull
	static CooldownMessage basic(@NonNull String before, @NonNull String after) {
		return new BasicCooldownMessage(before, after);
	}
}
