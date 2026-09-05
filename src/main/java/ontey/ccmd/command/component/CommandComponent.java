package ontey.ccmd.command.component;

import lombok.NonNull;
import ontey.ccmd.command.execution.Execution;
import ontey.ccmd.command.requirement.Requirement;
import org.jetbrains.annotations.Nullable;

/// A `CommandComponent` is either a literal or required argument in a brigadier command tree.
/// Literals have the bare minimum (an execution and requirement).
/// Arguments also have suggestions.

public sealed interface CommandComponent permits LiteralCommandComponent, ArgumentCommandComponent {
	
	/// @return The code that this command or argument executes.
	
	@Nullable
	Execution execution();
	
	/// @return The requirement to see and execute this command or argument
	
	@Nullable
	Requirement requirement();
	
	/// @return This component's name
	
	@NonNull
	String name();
}
