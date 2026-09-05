package ontey.ccmd.command.component;

import lombok.Builder;
import lombok.NonNull;
import ontey.ccmd.command.execution.Execution;
import ontey.ccmd.command.requirement.Requirement;
import org.jetbrains.annotations.Nullable;

@Builder
public record LiteralCommandComponent(
  @NonNull String name,
  @Nullable Execution execution,
  @Nullable Requirement requirement
) implements CommandComponent {
	
	@Override
	public @NonNull String prefix() {
		return "literal:";
	}
}
