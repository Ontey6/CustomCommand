package ontey.ccmd.command.requirement;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.NonNull;
import ontey.ccmd.command.context.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Predicate;

public record JavascriptRequirement(@NonNull String javascript) implements Requirement {
	
	@Override
	public @NonNull Predicate<CommandSourceStack> parseRequirement(@NonNull ParseContext context) {
		return RequirementJavascriptUtil.parseJavascript(javascript, context);
	}
	
	@Override
	public @NonNull String path() {
		return "javascript";
	}
	
	@Override
	public @NotNull Map<String, Object> serialize() {
		return Map.of(
		  "type", "JAVASCRIPT",
		  "javascript", javascript
		);
	}
}
