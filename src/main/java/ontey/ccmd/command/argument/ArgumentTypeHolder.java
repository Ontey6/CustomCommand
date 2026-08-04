package ontey.ccmd.command.argument;

import com.mojang.brigadier.arguments.ArgumentType;
import lombok.NonNull;
import ontey.api.config.ConfigSection;

public interface ArgumentTypeHolder {
	
	@NonNull
	ArgumentType<?> argumentType(@NonNull ConfigSection section);
}
