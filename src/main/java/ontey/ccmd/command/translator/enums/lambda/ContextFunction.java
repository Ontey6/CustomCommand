package ontey.ccmd.command.translator.enums.lambda;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import ontey.api.config.ConfigSection;
import ontey.ccmd.command.context.ParseContext;

public interface ContextFunction<T> {
	
	T apply(ArgumentBuilder<CommandSourceStack, ?> builder, ConfigSection section, ParseContext context);
}
