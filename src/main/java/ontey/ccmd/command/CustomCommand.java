package ontey.ccmd.command;

import lombok.Builder;
import lombok.NonNull;
import ontey.ccmd.command.component.LiteralCommandComponent;
import ontey.ccmd.command.config.CustomCommandConfig;
import ontey.ccmd.command.context.ParseContext;

import java.io.File;
import java.util.List;

@Builder
public record CustomCommand(@NonNull CustomCommandConfig values, @NonNull LiteralCommandComponent component,
                            @NonNull File file, @NonNull List<CommandSection> children) implements CommandSectionLike {
	
	public CustomCommand {
		children = List.copyOf(children);
	}
	
	@Override
	public @NonNull CustomCommandNode build(@NonNull ParseContext context) {
		return (CustomCommandNode) CommandSectionLike.super.build(context);
	}
}
