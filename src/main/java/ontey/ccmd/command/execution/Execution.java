package ontey.ccmd.command.execution;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.NonNull;
import ontey.api.serialization.CombinedConfigSerializable;
import ontey.ccmd.command.context.ParseContext;

public interface Execution extends CombinedConfigSerializable {
	
	/// @return The execution
	
	@NonNull
	Command<CommandSourceStack> parseExecution(@NonNull ParseContext context);
	
	/// @return The name of the section/field in the base section that this component is located
	
	@NonNull
	String path();
}
