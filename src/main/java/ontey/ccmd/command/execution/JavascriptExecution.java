package ontey.ccmd.command.execution;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.NonNull;
import ontey.ccmd.command.context.ParseContext;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

public record JavascriptExecution(@NonNull String javascript) implements Execution {
	
	@Override
	public @NonNull Command<CommandSourceStack> parseExecution(@NonNull ParseContext context) {
		return ExecutionJavascriptUtil.parseJavascript(javascript, context);
	}
	
	@Override
	public @NotNull Map<String, Object> serialize() {
		return Map.of(
		  "type", "javascript",
		  "javascript", javascript
		);
	}
}
