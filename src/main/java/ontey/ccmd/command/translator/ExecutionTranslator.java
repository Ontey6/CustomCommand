package ontey.ccmd.command.translator;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import ontey.api.config.ConfigSection;
import ontey.api.javascript.JavaScriptException;
import ontey.api.javascript.Javascript;
import ontey.ccmd.command.exception.CustomCommandParseException;
import ontey.ccmd.command.translator.enums.ExecutionType;

import java.util.function.Function;

import static ontey.api.command.Command.SUCCESS;
import static ontey.ccmd.command.translator.JavascriptUtil.addContextToJavascript;

final class ExecutionTranslator {
	
	static void addExecution(ArgumentBuilder<CommandSourceStack, ?> builder, ConfigSection section, String rootName, String name) {
		ExecutionType executionType = section.getEnum("type", ExecutionType.class);
		
		if(executionType == null)
			throw new CustomCommandParseException("In command '" + rootName + "', the argument '" + name + "' specifies an execution declaration, but not an execution type.");
		
		if(executionType == ExecutionType.JAVASCRIPT) {
			String code = section.getString("javascript");
			
			if(code == null)
				throw new CustomCommandParseException("In command '" + rootName + "', the argument '" + name + "' specifies the JAVASCRIPT execution type, but doesn't specify the javascript String (executes.javascript doesn't exist)");
			
			Javascript javascript = JavascriptUtil.createBaseJavascript();
			
			builder.executes(ctx -> {
				addContextToJavascript(ctx, javascript);
				
				try {
					var function = (Function<Object[], Object>) javascript.eval(code);
					
					if(function == null)
						throw new CustomCommandParseException("In command '" + rootName + "', the argument '" + name + "''s javascript suggestions don't return anything, but they should return an arrow function returning either an int or nothing!");
					
					var returned = (Integer) function.apply(new Object[0]);
					
					return returned == null ? SUCCESS : returned;
				} catch(JavaScriptException e) {
					throw new CustomCommandParseException("A javascript error occurred.\n" + e.getMessage());
				} catch(ClassCastException e) {
					throw new CustomCommandParseException("In command '" + rootName + "', the argument '" + name + "''s javascript suggestions don't return the right type. It has to be an arrow function that returns either an int or nothing!");
				}
			});
		}
	}
}
