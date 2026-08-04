package ontey.ccmd.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.Getter;
import lombok.NonNull;
import ontey.api.command.Command;
import ontey.api.command.config.CommandConfig;

import java.util.List;

public class CustomCommand extends CommandSection {
	
	@Getter
	private final CommandConfig values;
	
	public CustomCommand(@NonNull LiteralArgumentBuilder<CommandSourceStack> argumentBuilder, @NonNull List<@NonNull CommandSection> children, @NonNull CommandConfig values) {
		super(values.name(), argumentBuilder, children);
		this.values = values;
	}
	
	@NonNull
	public String getName() {
		return values.name();
	}
	
	public Command buildCommand() {
		return new Command(values.name()) {
			{
				aliases = values.aliases();
				description = values.description();
				permission = values.permission();
				consoleOnly = values.consoleOnly();
				root = (LiteralArgumentBuilder<CommandSourceStack>) getArgumentBuilder();
			}
		};
	}
}
