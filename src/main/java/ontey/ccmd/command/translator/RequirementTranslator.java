package ontey.ccmd.command.translator;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import ontey.api.config.ConfigSection;
import ontey.api.javascript.JavaScriptException;
import ontey.api.javascript.Javascript;
import ontey.ccmd.command.exception.CustomCommandParseException;

import java.util.function.Function;

import static ontey.api.command.Command.SUCCESS;
import static ontey.ccmd.command.translator.JavascriptUtil.addSourceToJavascript;

class RequirementTranslator {
	
	static void addRequirement(ArgumentBuilder<CommandSourceStack, ?> builder, ConfigSection section, String rootName, String name) {
		String code = section.getString("javascript");
		
		if(code == null)
			throw new CustomCommandParseException("In command '" + rootName + "', the argument '" + name + "' specifies the JAVASCRIPT requirement type, but doesn't specify the javascript String (requires.javascript doesn't exist)");
		
		Javascript javascript = JavascriptUtil.createBaseJavascript();
		
		builder.requires(source -> {
			addSourceToJavascript(source, javascript);
			
			try {
				var function = (Function<Object[], Object>) javascript.eval(code);
				
				if(function == null)
					throw new CustomCommandParseException("In command '" + rootName + "', the argument '" + name + "''s javascript requirement doesn't return anything, but they should return an arrow function returning a boolean!");
				
				var returned = (Boolean) function.apply(new Object[0]);
				
				if(returned == null)
					throw new CustomCommandParseException("In command '" + rootName + "', the argument '" + name + "''s javascript requirement doesn't return anything, but they should return an arrow function returning a boolean!");
				
				return returned;
			} catch(JavaScriptException e) {
				throw new CustomCommandParseException("A javascript error occurred.\n" + e.getMessage());
			} catch(ClassCastException e) {
				throw new CustomCommandParseException("In command '" + rootName + "', the argument '" + name + "''s javascript requirement doesn't return the right type. It has to be an arrow function that returns a boolean!");
			}
		});
	}
}
