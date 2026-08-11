package ontey.ccmd.command.translator.enums.lambda;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.NonNull;
import ontey.api.config.ConfigSection;
import ontey.ccmd.command.context.ParseContext;
import org.jetbrains.annotations.Contract;

@FunctionalInterface
public interface Addition {
	
	@Contract(mutates = "param1")
	void addTo(@NonNull ArgumentBuilder<CommandSourceStack, ?> builder, @NonNull ConfigSection section, @NonNull ParseContext parseContext);
}
