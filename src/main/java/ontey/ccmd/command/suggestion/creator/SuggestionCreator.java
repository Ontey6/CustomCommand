package ontey.ccmd.command.suggestion.creator;

import lombok.NonNull;
import ontey.api.config.ConfigSection;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.command.suggestion.Suggestion;

@FunctionalInterface
public interface SuggestionCreator {
	
	@NonNull
	Suggestion createSuggestion(@NonNull ParseContext context, @NonNull ConfigSection baseSection);
}
