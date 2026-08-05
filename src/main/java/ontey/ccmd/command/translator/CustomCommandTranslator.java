package ontey.ccmd.command.translator;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import ontey.api.command.argument.Arg;
import ontey.api.command.config.CommandConfig;
import ontey.api.config.ConfigSection;
import ontey.ccmd.command.CommandSection;
import ontey.ccmd.command.CustomCommand;
import ontey.ccmd.command.exception.CustomCommandParseException;
import ontey.ccmd.command.translator.enums.ArgumentPreset;
import ontey.ccmd.command.translator.enums.ArgumentSelectionType;
import ontey.ccmd.command.translator.enums.ArgumentType;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomCommandTranslator {
	
	private static final String SELECTION_PREFIX = "then:";
	
	public static CustomCommand translateYaml(ConfigSection section) {
		CommandConfig values = createCommandConfig(section);
		
		var literalBuilder = createRoot(values, section);
		
		CustomCommand cmd = new CustomCommand(literalBuilder, new ArrayList<>(), values);
		
		var keys = section.getKeys(false);
		
		for(var key : keys)
			if(key.startsWith(SELECTION_PREFIX) && section.isSection(key))
				cmd.addChild(translateSection(cmd.getName(), cmd, section.getSection(key)));
		
		return cmd;
	}
	
	private static LiteralArgumentBuilder<CommandSourceStack> createRoot(CommandConfig values, ConfigSection section) {
		ArgumentType argumentType = section.getEnum("type", ArgumentType.class);
		
		if(argumentType == ArgumentType.ARGUMENT)
			throw new CustomCommandParseException("In command '" + values.name() + "', the root has an argument type of ARGUMENT, which is not allowed. The root has to be a LITERAL");
		
		//noinspection unchecked as the argument type has to be LITERAL, this is safe to assume
		return (LiteralArgumentBuilder<CommandSourceStack>) createNode(values.name(), section, true);
	}
	
	private static CommandSection translateSection(String rootName, CommandSection root, ConfigSection section) {
		var node = createNode(rootName, section, false);
		String name = section.getName().substring(SELECTION_PREFIX.length());
		
		var child = root.createChild(name, node);
		
		for(var key : section.getKeys(false)) {
			if(!key.startsWith(SELECTION_PREFIX) || !section.isSection(key))
				continue;
			
			child.addChild(translateSection(rootName, child, section.getSection(key)));
		}
		
		return child;
	}
	
	private static ArgumentBuilder<CommandSourceStack, ?> createNode(String rootName, ConfigSection section, boolean isRoot) {
		String name = isRoot ? rootName : section.getName().substring(SELECTION_PREFIX.length());
		
		ArgumentType argumentTypeEnum = isRoot ? ArgumentType.LITERAL : section.getEnum("type", ArgumentType.class, ArgumentType.LITERAL);
		
		ArgumentBuilder<CommandSourceStack, ?> argumentBuilder = null;
		
		if(argumentTypeEnum == ArgumentType.LITERAL) {
			argumentBuilder = Arg.literal(name);
		}
		
		if(argumentTypeEnum == ArgumentType.ARGUMENT) {
			var argumentSection = section.getSection("argument");
			
			if(argumentSection == null)
				throw new CustomCommandParseException("In command '" + rootName + "', the argument '" + name + "' doesn't specify an argument declaration (The 'argument' section is missing)");
			
			var selectionType = argumentSection.getEnum("type", ArgumentSelectionType.class);
			
			if(selectionType == null)
				throw new CustomCommandParseException("In command '" + rootName + "', the argument '" + name + "' doesn't specify an argument selection type (The 'argument.type' field is missing)");
			
			com.mojang.brigadier.arguments.ArgumentType<?> argumentType = null;
			
			if(selectionType == ArgumentSelectionType.PRESET) {
				var preset = argumentSection.getEnum("preset", ArgumentPreset.class);
				
				if(preset == null)
					throw new CustomCommandParseException("In command '" + rootName + "', the argument '" + name + "' doesn't specify a preset even though the argument selection type is PRESET (The 'argument.preset' field is missing)");
				
				argumentType = preset.argumentType(argumentSection);
			}
			
			if(argumentType == null)
				throw new CustomCommandParseException("In command '" + rootName + "', the argument type of the argument '" + name + "' couldn't be resolved");
			
			var argument = Arg.of(name, argumentType);
			
			//TODO custom arguments
			
			var suggestsSection = section.getSection("suggests");
			if(suggestsSection != null)
				SuggestionTranslator.addSuggestions(argument, suggestsSection, rootName, name);
			
			argumentBuilder = argument;
		}
		
		var executesSection = section.getSection("executes");
		if(executesSection != null)
			ExecutionTranslator.addExecution(argumentBuilder, executesSection, rootName, name);
		
		var requiresSection = section.getSection("requires");
		if(requiresSection != null)
			RequirementTranslator.addRequirement(argumentBuilder, requiresSection, rootName, name);
		
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
