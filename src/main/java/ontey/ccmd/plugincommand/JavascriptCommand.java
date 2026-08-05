package ontey.ccmd.plugincommand;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.kyori.adventure.text.Component;
import ontey.api.command.Command;
import ontey.api.command.argument.Arg;
import ontey.api.javascript.JavaScriptException;
import ontey.api.javascript.Javascript;
import ontey.api.loader.AutoRegistered;
import org.graalvm.polyglot.Context;

@AutoRegistered
public class JavascriptCommand extends Command {
	
	public JavascriptCommand() {
		super("javascript");
		
		root
		  .then(
			 Arg.varargs("code")
			   .executes(ctx -> {
				   try {
						var source = ctx.getSource();
					   var result = new Javascript(
					     Context
						    .newBuilder("js")
						    .allowHostClassLookup(_ -> true)
						    .hostClassLoader(getClass().getClassLoader())
						    .allowAllAccess(true)
						    .build())
					     .addVariable("ctx", ctx)
					     .addVariable("source", source)
					     .addVariable("sender", source.getSender())
					     .addVariable("executor", source.getExecutor())
					     .addVariable("location", source.getLocation())
					     .eval(ctx.getArgument("code", String.class));
						
						if(result != null)
							source.getSender().sendPlainMessage(result.getClass().getName());
						
						source.getSender().sendMessage(Component.text(result != null ? result.toString() : "No result"));
				   } catch(JavaScriptException e) {
					   throw new SimpleCommandExceptionType(new LiteralMessage("Javascript error: " + e.getMessage())).create();
				   }
				   
				   return SUCCESS;
			   })
		  );
	}
}
