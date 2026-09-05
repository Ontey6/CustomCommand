package ontey.ccmd.command;

import lombok.Builder;
import lombok.NonNull;
import ontey.api.command.config.CommandConfig;
import ontey.ccmd.command.component.CommandComponent;

import java.util.List;

@Builder
public record CommandSection(@NonNull CommandConfig values, @NonNull CommandComponent component,
                             @NonNull List<CommandSection> children) implements CommandSectionLike {
	
}
