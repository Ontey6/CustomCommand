package ontey.ccmd.util;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import ontey.api.javascript.Javascript;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.command.suggestion.entry.SuggestionEntry;
import ontey.ccmd.js.Arguments;
import ontey.ccmd.js.Suggestions;
import ontey.ccmd.shared.SharedConstants;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.graalvm.polyglot.Context;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

public final class JavascriptUtil {
	
	public static Javascript createBaseJavascript() {
		var javascript = new Javascript(
		  Context
			 .newBuilder("js")
			 .hostClassLoader(JavascriptUtil.class.getClassLoader())
			 .allowAllAccess(true)
			 .option("engine.WarnInterpreterOnly", "false")
			 /*
			  [14:34:23 WARN]: [To redirect Truffle log output to a file use one of the following options:
			  [14:34:23 WARN]: * '--log.file=<path>' if the option is passed using a guest language launcher.
			  [14:34:23 WARN]: * '-Dpolyglot.log.file=<path>' if the option is passed using the host Java launcher.
			  [14:34:23 WARN]: * Configure logging using the polyglot embedding API.]
			  [14:34:23 WARN]: [engine] WARNING: The polyglot engine uses a fallback runtime that does not support runtime compilation to native code.
			  [14:34:23 WARN]: Execution without runtime compilation will negatively impact the guest application performance.
			  [14:34:23 WARN]: The following cause was found: JVMCI is required to enable optimizations. Pass -XX:+EnableJVMCI as a virtual machine argument to the java executable to resolve this.
			  [14:34:23 WARN]: For more information see: https://www.graalvm.org/latest/reference-manual/embed-languages/#runtime-optimization-support.
			  [14:34:23 WARN]: To disable this warning use the '--engine.WarnInterpreterOnly=false' option or the '-Dpolyglot.engine.WarnInterpreterOnly=false' system property.
			  */
			 .build());
		
		addBaseToJavascript(javascript);
		
		return javascript;
	}
	
	public static void addBaseToJavascript(Javascript javascript) {
		javascript
		  .addVariable("console", Bukkit.getConsoleSender())
		  .addVariable("playerWithName", (Function<String, Player>) Bukkit::getPlayer)
		  .addVariable("playerWithUUID", (Function<String, Player>) uuid -> {
			  try {
				  return Bukkit.getPlayer(UUID.fromString(uuid));
			  } catch(IllegalArgumentException e) {
				  return null;
			  }
		  })
		  .addVariable("server", Bukkit.getServer());
	}
	
	public static void addContextToJavascript(CommandContext<CommandSourceStack> ctx, Javascript javascript) {
		addSourceToJavascript(ctx.getSource(), javascript);
		javascript
		  .addVariable("ctx", ctx)
		  .addVariable("context", ctx)
		  .addVariable("args", new Arguments(ctx))
		  .addVariable("getArgument", (Function<String, Object>) str -> ctx.getArgument(str, Object.class));
	}
	
	public static void addSourceToJavascript(CommandSourceStack source, Javascript javascript) {
		javascript
		  .addVariable("source", source)
		  .addVariable("sender", source.getSender())
		  .addVariable("executor", source.getExecutor())
		  .addVariable("location", source.getLocation())
		  .addVariable("isSenderPlayer", source.getSender() instanceof Player)
		  .addVariable("isExecutorPlayer", source.getExecutor() instanceof Player)
		  .addVariable("isExecutorEntity", source.getExecutor() != null)
		  .addVariable("isExecutorConsole", source.getExecutor() == null);
	}
	
	public static void addSuggestionsToJavascript(SuggestionsBuilder suggestionsBuilder, Javascript javascript) {
		javascript
		  .addVariable("suggestionsBuilder", suggestionsBuilder)
		  .addVariable("remaining", suggestionsBuilder.getRemaining())
		  .addVariable("remainingLowerCase", suggestionsBuilder.getRemainingLowerCase())
		  .addVariable("input", suggestionsBuilder.getInput())
		  .addVariable("suggestions", Suggestions.INSTANCE)
		  .addVariable("stringSuggestion", (BiFunction<String, String, SuggestionEntry>) SuggestionEntry::string)
		  .addVariable("integerSuggestion", (BiFunction<Integer, String, SuggestionEntry>) SuggestionEntry::integer);
	}
	
	public static String getFileContents(ParseContext context, String filename) {
		File javascriptDirectory = SharedConstants.dataDirectory.resolve("javascript").toFile();
		
		if(!javascriptDirectory.exists()) {
			javascriptDirectory.mkdir();
			throw context.newException("Javascript directory not found, creating it");
		}
		
		File file = new File(javascriptDirectory, filename);
		
		if(!file.exists())
			throw context.newException("Javascript file '" + filename + "' not found");
		
		StringBuilder sb = new StringBuilder();
		
		try {
			Scanner scanner = new Scanner(file);
			while(scanner.hasNextLine())
				sb.append(scanner.nextLine()).append('\n');
		} catch(FileNotFoundException e) {
			throw context.newException("Specifies javascript file that doesn't exist", e);
		}
		return sb.toString();
	}
}
