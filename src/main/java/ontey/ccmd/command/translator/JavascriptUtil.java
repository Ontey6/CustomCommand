package ontey.ccmd.command.translator;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import ontey.api.javascript.Javascript;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.graalvm.polyglot.Context;

import java.util.function.Function;

final class JavascriptUtil {
	static Javascript createBaseJavascript() {
		return new Javascript(
		  Context
			 .newBuilder("js")
		    .allowHostClassLookup(_ -> true)
		    .hostClassLoader(JavascriptUtil.class.getClassLoader())
			 .allowAllAccess(true)
			 .build())
		  .addClass(Bukkit.class)
		  .addClass(Component.class)
		  .addClass(NamedTextColor.class)
		  .addClass(Style.class)
		  .addClass(MiniMessage.class);
	}
	
	static void addContextToJavascript(CommandContext<CommandSourceStack> ctx, Javascript javascript) {
		addSourceToJavascript(ctx.getSource(), javascript);
		javascript
		  .addVariable("ctx", ctx)
		  .addVariable("context", ctx)
		  .addVariable("getArgument", (Function<String, Object>) str -> ctx.getArgument(str, Object.class));
	}
	
	static void addSourceToJavascript(CommandSourceStack source, Javascript javascript) {
		javascript
		  .addVariable("source", source)
		  .addVariable("sender", source.getSender())
		  .addVariable("executor", source.getExecutor())
		  .addVariable("location", source.getLocation())
		  .addVariable("isSenderPlayer", source.getSender() instanceof Player)
		  .addVariable("isExecutorPlayer", source.getExecutor() instanceof Player);
	}
	
	static void addSuggestionsToJavascript(SuggestionsBuilder suggestionsBuilder, Javascript javascript) {
		javascript
		  .addVariable("suggestionsBuilder", suggestionsBuilder)
		  .addVariable("remaining", suggestionsBuilder.getRemaining())
		  .addVariable("remainingLowerCase", suggestionsBuilder.getRemainingLowerCase())
		  .addVariable("input", suggestionsBuilder.getInput());
	}
}
