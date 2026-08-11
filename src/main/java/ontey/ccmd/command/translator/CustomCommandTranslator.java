package ontey.ccmd.command.translator;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.NonNull;
import ontey.api.command.config.CommandConfig;
import ontey.api.config.ConfigSection;
import ontey.ccmd.command.CommandSection;
import ontey.ccmd.command.CustomCommand;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.command.translator.enums.CommandComponentType;
import ontey.ccmd.command.translator.enums.ExecutionType;
import ontey.ccmd.command.translator.enums.RequirementType;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomCommandTranslator {
	
	private static final String ARGUMENT_PREFIX = "then:", LITERAL_PREFIX = "literal:", REQUIRED_ARGUMENT_PREFIX = "argument:";
	
	public static CustomCommand translateYaml(ConfigSection section) {
		CommandConfig values = createCommandConfig(section);
		
		var literalBuilder = createRoot(values, section);
		
		CustomCommand cmd = new CustomCommand(literalBuilder, new ArrayList<>(), values);
		
		var keys = section.getKeys(false);
		
		for(var key : keys)
			if(key.startsWith(ARGUMENT_PREFIX) && section.isSection(key))
				cmd.addChild(translateSection(cmd.getName(), cmd, section.getSection(key)));
		
		return cmd;
	}
	
	private static LiteralArgumentBuilder<CommandSourceStack> createRoot(@NonNull CommandConfig values, @NonNull ConfigSection section) {
		CommandComponentType commandComponentType = section.getEnum("type", CommandComponentType.class);
		ParseContext context = new ParseContext(values.name(), true);
		
		if(commandComponentType == CommandComponentType.ARGUMENT)
			throw context.newException("The command root has a command component type of ARGUMENT, but the root has to be LITERAL. You can remove the field as LITERAL is the default for the root");
		
		//noinspection unchecked as the argument type has to be LITERAL, this is safe to assume
		return (LiteralArgumentBuilder<CommandSourceStack>) createNode(context, section);
	}
	
	private static CommandSection translateSection(String rootName, CommandSection root, ConfigSection section) {
		String name = section.getName().substring(ARGUMENT_PREFIX.length());
		var context = new ParseContext(rootName, name);
		var node = createNode(context, section);
		
		var child = root.createChild(name, node);
		
		for(var key : section.getKeys(false)) {
			if(!key.startsWith(ARGUMENT_PREFIX) || !section.isSection(key))
				continue;
			
			child.addChild(translateSection(rootName, child, section.getSection(key)));
		}
		
		return child;
	}
	
	private static ArgumentBuilder<CommandSourceStack, ?> createNode(ParseContext context, ConfigSection section) {
		String name = context.isRoot() ? context.rootName() : section.getName().substring(ARGUMENT_PREFIX.length());
		context = context.withArgumentName(name);
		
		CommandComponentType commandComponentType = context.isRoot()
		  ? CommandComponentType.LITERAL
		  : section.getEnum("type", CommandComponentType.class, CommandComponentType.LITERAL);
		
		ArgumentBuilder<CommandSourceStack, ?> argumentBuilder = commandComponentType.getAction().apply(null, section, context);
		
		var executesSection = section.getSection("executes");
		if(executesSection != null)
			ExecutionType.addExecution(argumentBuilder, executesSection, context.withSection("executes"));
		
		var requiresSection = section.getSection("requires");
		if(requiresSection != null)
			RequirementType.addRequirement(argumentBuilder, requiresSection, context.withSection("requires"));
		
		return argumentBuilder;
	}
	
	private static CommandConfig createCommandConfig(ConfigSection section) {
		return new CommandConfig(
		  section.getName(),
		  section.getStringList("aliases"),
		  section.getString("description"),
		  section.getString("permission"),
		  section.getBoolean("console-only", false),
		  new HashMap<>(),
		  section.getBoolean("enabled", true)
		);
	}
}
