package ontey.ccmd.command.translator;

import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import ontey.api.command.argument.Arg;
import ontey.api.command.config.CommandConfig;
import ontey.api.config.ConfigSection;
import ontey.ccmd.command.CommandSection;
import ontey.ccmd.command.CustomCommand;
import ontey.ccmd.command.argument.ArgumentTypeHolder;
import ontey.ccmd.command.exception.CustomCommandParseException;
import ontey.ccmd.command.suggestion.SuggestionEntry;
import ontey.ccmd.command.translator.enums.ArgumentPreset;
import ontey.ccmd.command.translator.enums.ArgumentSelectionType;
import ontey.ccmd.command.translator.enums.ArgumentType;
import ontey.ccmd.command.translator.enums.SuggestionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomCommandTranslator {
	
	private static final String SELECTION_PREFIX = "then:";
	
	public static CustomCommand translateYaml(ConfigSection section) {
		CommandConfig values = new CommandConfig(
		  section.getName(),
		  section.getStringList("aliases"),
		  section.getString("description"),
		  section.getString("permission"),
		  section.getBoolean("console-only", false),
		  new HashMap<>(), // "options" are differently implemented here, therefore not needed
		  section.getBoolean("enabled", true)
		);
		
		var argumentBuilder = createNode(values.name(), section, true);
		var literalBuilder = Arg
		  .literal(values.name())
		  .executes(argumentBuilder.getCommand())
		  .requires(argumentBuilder.getRequirement());
		
		CustomCommand cmd = new CustomCommand(literalBuilder, new ArrayList<>(), values);
		
		var keys = section.getKeys(false);
		
		for(var key : keys)
			if(key.startsWith(SELECTION_PREFIX) && section.isSection(key))
				cmd.addChild(translateSection(cmd.getName(), cmd, section.getSection(key)));
		
		return cmd;
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
		
		ArgumentType argumentType = isRoot ? ArgumentType.LITERAL : section.getEnum("type", ArgumentType.class, ArgumentType.LITERAL);
		
		if(argumentType == ArgumentType.LITERAL)
			return Arg.literal(name);
		
		var argumentSection = section.getSection("argument");
		
		if(argumentSection == null)
			throw new CustomCommandParseException("In command '" + rootName + "', the argument '" + name + "' doesn't specify an argument declaration (The 'argument' section is missing)");
		
		var selectionType = argumentSection.getEnum("type", ArgumentSelectionType.class);
		
		if(selectionType == null)
			throw new CustomCommandParseException("In command '" + rootName + "', the argument '" + name + "' doesn't specify an argument selection type (The 'argument.type' field is missing)");
		
		ArgumentTypeHolder argumentTypeHolder = null;
		
		if(selectionType == ArgumentSelectionType.PRESET) {
			var preset = argumentSection.getEnum("preset", ArgumentPreset.class);
			
			if(preset == null)
				throw new CustomCommandParseException("In command '" + rootName + "', the argument '" + name + "' doesn't specify a preset even though the argument selection type is PRESET (The 'argument.preset' field is missing)");
			
			argumentTypeHolder = preset;
		}
		
		if(argumentTypeHolder == null)
			throw new CustomCommandParseException("In command '" + rootName + "', the argument type of the argument '" + name + "' couldn't be resolved");
		
		var argument = Arg.of(name, argumentTypeHolder.argumentType(argumentSection));
		
		//TODO custom arguments
		//TODO add logic for execution so the commands can actually do something
		
		var suggestsSection = section.getSection("suggests");
		if(suggestsSection != null)
			addSuggestions(argument, suggestsSection, rootName, name);
		
		return argument;
	}
	
	private static void addSuggestions(RequiredArgumentBuilder<CommandSourceStack, ?> builder, ConfigSection section, String rootName, String name) {
		var suggestionType = section.getEnum("type", SuggestionType.class);
		
		if(suggestionType == null)
			throw new CustomCommandParseException("In command '" + rootName + "', the argument '" + name + "' specifies a 'suggests' section, but not a (valid) type! (suggestions.type is either not set or an invalid value)");
		
		if(suggestionType == SuggestionType.LIST) {
			List<?> rawSuggestions = section.getList("list");
			
			if(rawSuggestions == null)
				throw new CustomCommandParseException("In command '" + rootName + "', the argument '" + name + "' specifies the LIST type, but doesn't specify (a valid) one (suggestions.list is either not set or an invalid value)");
			
			List<SuggestionEntry> suggestions = new ArrayList<>(rawSuggestions.size());
			
			for(Object rawSuggestion : rawSuggestions)
				suggestions.add(SuggestionEntry.deserialize(rawSuggestion));
			
			if(suggestions.contains(null))
				throw new CustomCommandParseException("In command '" + rootName + "', the argument '" + name + "''s list contains one or more invalid suggestions");
			
			builder.suggests((_, suggestionsBuilder) -> {
				for(var suggestion : suggestions)
					suggestion.suggestIn(suggestionsBuilder);
				
				return suggestionsBuilder.buildFuture();
			});
		}
		
		if(suggestionType == SuggestionType.JAVASCRIPT) {
			//TODO javascript suggestions
			
		}
	}
}
