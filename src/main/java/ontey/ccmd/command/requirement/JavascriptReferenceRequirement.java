package ontey.ccmd.command.requirement;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.NonNull;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.util.JavascriptUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Predicate;

public record JavascriptReferenceRequirement(
  @NonNull String javascriptFilePath) implements Requirement {
	
	@Override
	public @NonNull Predicate<CommandSourceStack> parseRequirement(@NonNull ParseContext context) {
		var javascript = JavascriptUtil.getFileContents(context, javascriptFilePath);
		
		return RequirementJavascriptUtil.parseJavascript(javascript, context);
	}
	
	@Override
	public @NonNull String path() {
		return "javascript-file";
	}
	
	@Override
	public @NotNull Map<String, Object> serialize() {
		return Map.of(
		  "type", "JAVASCRIPT_REFERENCE",
		  path(), javascriptFilePath
		);
	}
}
