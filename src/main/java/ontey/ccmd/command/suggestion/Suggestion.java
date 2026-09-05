package ontey.ccmd.command.suggestion;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.NonNull;
import ontey.api.serialization.CombinedConfigSerializable;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.command.suggestion.entry.SuggestionEntry;

import java.util.List;

public interface Suggestion extends CombinedConfigSerializable {
	
	/// @return The type of the suggestion component
	
	@NonNull
	SuggestionType type(); //TODO do I even need this? Maybe remove the enums and add the methods to the type components.
	
	@NonNull
	List<? extends @NonNull SuggestionEntry> parseSuggestionList(@NonNull ParseContext context, @NonNull CommandContext<CommandSourceStack> commandContext, @NonNull SuggestionsBuilder suggestionsBuilder);
	
	default SuggestionProvider<CommandSourceStack> parseSuggestions(@NonNull ParseContext context) {
		return (commandContext, suggestionBuilder) -> {
			var list = parseSuggestionList(context, commandContext, suggestionBuilder);
			
			for(var entry : list)
				entry.suggestIn(suggestionBuilder, suggestionBuilder.getRemainingLowerCase());
			
			return suggestionBuilder.buildFuture();
		};
	}
}
