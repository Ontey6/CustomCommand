package ontey.ccmd.command.requirement;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.NonNull;
import ontey.api.serialization.CombinedConfigSerializable;
import ontey.ccmd.command.context.ParseContext;

import java.util.function.Predicate;

public interface Requirement extends CombinedConfigSerializable {
	
	/// @return The parsed requirement
	
	@NonNull
	Predicate<CommandSourceStack> parseRequirement(@NonNull ParseContext context);
	
	/// @return The name of the section/field in the base section that this component is located
	
	@NonNull
	String path();
}
