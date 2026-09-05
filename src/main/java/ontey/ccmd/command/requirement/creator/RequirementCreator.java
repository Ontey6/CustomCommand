package ontey.ccmd.command.requirement.creator;

import lombok.NonNull;
import ontey.api.config.ConfigSection;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.command.requirement.Requirement;

@FunctionalInterface
public interface RequirementCreator {
	
	@NonNull
	Requirement createRequirement(@NonNull ParseContext context, @NonNull ConfigSection baseSection);
}
