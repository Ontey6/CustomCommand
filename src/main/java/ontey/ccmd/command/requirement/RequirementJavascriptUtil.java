package ontey.ccmd.command.requirement;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import ontey.api.javascript.JavaScriptException;
import ontey.api.javascript.Javascript;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.util.JavascriptUtil;

import java.util.function.Function;
import java.util.function.Predicate;

import static ontey.ccmd.util.JavascriptUtil.addSourceToJavascript;

final class RequirementJavascriptUtil {
	
	public static Predicate<CommandSourceStack> parseJavascript(String code, ParseContext context) {
		return source -> {
			Javascript javascript = JavascriptUtil.createBaseJavascript();
			addSourceToJavascript(source, javascript);
			
			try {
				var function = (Function<Object[], Object>) javascript.eval(code);
				
				if(function == null)
					throw context.newException("Javascript requirement could not be evaluated (It doesn't return an arrow function returning a boolean like '() => player.hasPermission(\"...\")')");
				
				var returned = (Boolean) function.apply(new Object[0]);
				
				if(returned == null)
					throw context.newException("Javascript requirement's arrow function doesn't return anything (It should return a boolean like '() => player.hasPermission(\"...\")')");
				
				return returned;
			} catch(JavaScriptException e) {
				throw context.newException("A javascript error occurred in the javascript requirement", e);
			} catch(ClassCastException e) {
				throw context.newException("Javascript requirement doesn't return the right type (It should return an arrow function returning a boolean like () => player.hasPermission(\"...\"))", e);
			}
		};
	}
}
