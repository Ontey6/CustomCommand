package ontey.ccmd.command.execution.creator;

import lombok.NonNull;
import ontey.api.config.ConfigSection;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.command.execution.Execution;

@FunctionalInterface
public interface ExecutionCreator {
	
	@NonNull
	Execution createExecution(@NonNull ParseContext context, @NonNull ConfigSection baseSection);
}
