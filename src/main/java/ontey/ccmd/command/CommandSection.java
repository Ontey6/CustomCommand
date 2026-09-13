package ontey.ccmd.command;

import lombok.Builder;
import lombok.NonNull;
import ontey.ccmd.command.component.CommandComponent;
import ontey.ccmd.command.config.CustomCommandConfig;

import java.util.List;

@Builder
public record CommandSection(@NonNull CustomCommandConfig values, @NonNull CommandComponent component,
                             @NonNull List<CommandSection> children) implements CommandSectionLike {
	
	public CommandSection {
		children = List.copyOf(children);
	}
}
