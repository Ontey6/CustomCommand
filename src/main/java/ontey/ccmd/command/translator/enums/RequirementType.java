package ontey.ccmd.command.translator.enums;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ontey.api.config.ConfigSection;
import ontey.api.javascript.JavaScriptException;
import ontey.api.javascript.Javascript;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.command.translator.enums.lambda.Addition;
import ontey.ccmd.util.JavascriptUtil;

import java.util.function.Function;

import static ontey.ccmd.util.JavascriptUtil.addSourceToJavascript;
import static ontey.ccmd.util.JavascriptUtil.getFileContents;

@AllArgsConstructor
public enum RequirementType {
	JAVASCRIPT((builder, requiresSection, context) -> {
		String code = requiresSection.getString("javascript");
		
		if(code == null)
			throw context.newException("Specifies the JAVASCRIPT requirement type, but doesn't specify the javascript String ('requires.javscript' is not set)");
		
		addJavascript(builder, code, context);
	}),
	JAVASCRIPT_REFERENCE((builder, requiresSection, context) -> {
		String filename = requiresSection.getString("javascript-file");
		
		if(filename == null)
			throw context.newException("Specifies the JAVASCRIPT_REFERENCE requirement type, but doesn't specify the javascript String ('requires.javscript-file' is not set)");
		
		String code = getFileContents(context, filename);
		
		addJavascript(builder, code, context);
	});
	
	@Getter
	private final Addition action;
	
	private static void addJavascript(ArgumentBuilder<CommandSourceStack, ?> builder, String code, ParseContext context) {
		Javascript javascript = JavascriptUtil.createBaseJavascript();
		
		builder.requires(source -> {
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
		});
	}
	
	public static void addRequirement(ArgumentBuilder<CommandSourceStack, ?> builder, ConfigSection requiresSection, ParseContext context) {
		//if(baseSection.isSection("requires")) {
		//	var requiresSection = baseSection.getSection("requires");
		//	var requirementType = requiresSection.getEnum("type", RequirementType.class);
		//
		//	if(requirementType == null)
		//		throw context.newException("No type specified");
		//
		//	requirementType.getAction().addTo(builder, requiresSection, context);
		//} else if(baseSection.isString("requires")) {
		//
		//}
		
		RequirementType requirementType = requiresSection.getEnum("type", RequirementType.class);
		
		if(requirementType == null)
			throw context.newException("Missing type ('requires.type' is not set)");
		
		requirementType.getAction().addTo(builder, requiresSection, context);
	}
}
