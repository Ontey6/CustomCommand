package ontey.ccmd.command;

import lombok.Builder;
import lombok.NonNull;
import ontey.api.command.config.CommandConfig;
import ontey.api.command.registry.RegistryCommand;
import ontey.ccmd.command.component.LiteralCommandComponent;
import ontey.ccmd.command.context.ParseContext;

import java.io.File;
import java.util.List;

@Builder
public record CustomCommand(@NonNull CommandConfig values, @NonNull List<CommandSection> children,
                            @NonNull LiteralCommandComponent component, File file) implements CommandSectionLike {
	
	@Override
	public @NonNull CustomCommandNode build(@NonNull ParseContext context) {
		return (CustomCommandNode) CommandSectionLike.super.build(context);
	}
	
	public RegistryCommand buildCommand(@NonNull ParseContext context) {
		return new RegistryCommand(name(), values.aliases(), values().description(), build(context), values::enabled);
	}
}
