package ontey.ccmd.command.component.creator;

import com.mojang.brigadier.arguments.ArgumentType;
import lombok.NonNull;
import ontey.api.config.ConfigSection;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface ArgumentTypeCreator {
	
	@NonNull
	ArgumentType<?> createArgumentType(@Nullable ConfigSection section);
}
