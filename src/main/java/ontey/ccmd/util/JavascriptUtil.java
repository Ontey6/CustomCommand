package ontey.ccmd.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.minimessage.MiniMessage;
import ontey.api.javascript.Javascript;
import ontey.ccmd.command.context.ParseContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.graalvm.polyglot.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.Function;

public final class JavascriptUtil {
	
	public static Javascript createBaseJavascript() {
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
	
	public static void addContextToJavascript(CommandContext<CommandSourceStack> ctx, Javascript javascript) {
		addSourceToJavascript(ctx.getSource(), javascript);
		javascript
		  .addVariable("ctx", ctx)
		  .addVariable("context", ctx)
		  .addVariable("getArgument", (Function<String, Object>) str -> ctx.getArgument(str, Object.class));
	}
	
	public static void addSourceToJavascript(CommandSourceStack source, Javascript javascript) {
		javascript
		  .addVariable("source", source)
		  .addVariable("sender", source.getSender())
		  .addVariable("executor", source.getExecutor())
		  .addVariable("location", source.getLocation())
		  .addVariable("isSenderPlayer", source.getSender() instanceof Player)
		  .addVariable("isExecutorPlayer", source.getExecutor() instanceof Player);
	}
	
	public static void addSuggestionsToJavascript(SuggestionsBuilder suggestionsBuilder, Javascript javascript) {
		javascript
		  .addVariable("suggestionsBuilder", suggestionsBuilder)
		  .addVariable("remaining", suggestionsBuilder.getRemaining())
		  .addVariable("remainingLowerCase", suggestionsBuilder.getRemainingLowerCase())
		  .addVariable("input", suggestionsBuilder.getInput());
	}
	
	public static void addContextChangerHelpersToJavascript(Javascript javascript) {
		javascript
		  .addVariable("console", Bukkit.getConsoleSender())
		  .addVariable("playerWithName", (Function<String, Player>) Bukkit::getPlayer)
		  .addVariable("playerWithUUID", (Function<String, Player>) uuid -> {
			  try {
				  return Bukkit.getPlayer(UUID.fromString(uuid));
			  } catch(IllegalArgumentException e) {
				  return null;
			  }
		  });
	}
	
	public static String getFileContents(ParseContext context, String filename) {
		File javascriptDirectory = new File(".", "plugins/CustomCommand/javscript");
		
		if(!javascriptDirectory.exists())
			javascriptDirectory.mkdirs();
		
		File file = new File(javascriptDirectory, filename);
		
		StringBuilder sb = new StringBuilder();
		
		try {
			Scanner scanner = new Scanner(file);
			while(scanner.hasNextLine())
				sb.append(scanner.nextLine());
		} catch(FileNotFoundException e) {
			throw context.newException("Specifies javascript file that doesn't exist", e);
		}
		return sb.toString();
	}
}
