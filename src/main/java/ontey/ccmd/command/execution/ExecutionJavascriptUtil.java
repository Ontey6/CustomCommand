package ontey.ccmd.command.execution;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import ontey.api.javascript.JavaScriptException;
import ontey.api.javascript.Javascript;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.util.JavascriptUtil;

import java.util.function.Function;

import static ontey.api.command.Command.SUCCESS;
import static ontey.ccmd.util.JavascriptUtil.addContextToJavascript;

final class ExecutionJavascriptUtil {
	
	public static Command<CommandSourceStack> parseJavascript(String code, ParseContext context) {
		return ctx -> {
			Javascript javascript = JavascriptUtil.createBaseJavascript();
			addContextToJavascript(ctx, javascript);
			
			try {
				var function = (Function<Object[], Object>) javascript.eval(code);
				
				if(function == null)
					throw context.newException("Javascript execution could not be evaluated (It should return an arrow function that optionally returns an integer like '() => {...}' or '() => 1')");
				
				var returned = (Integer) function.apply(new Object[0]);
				
				return returned == null ? SUCCESS : returned;
			} catch(JavaScriptException e) {
				throw context.newException("A javascript error occurred", e);
			} catch(ClassCastException e) {
				throw context.newException("Javascript execution doesn't return the right type (It should return an arrow function that optionally returns an integer like '() => {...}'  or '() => 1')");
			}
		};
	}
}
