package ontey.ccmd.command.parser;

import ontey.api.command.config.CommandConfig;
import ontey.api.config.ConfigSection;
import ontey.ccmd.command.CommandSection;
import ontey.ccmd.command.CustomCommand;
import ontey.ccmd.command.CustomCommand.CustomCommandBuilder;
import ontey.ccmd.command.component.CommandComponents;
import ontey.ccmd.command.component.LiteralCommandComponent;
import ontey.ccmd.command.context.ParseContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomCommandParser {
	
	/// Parses a [CustomCommand] from the given section.
	///
	/// @param baseSection The command's section
	
	public static CustomCommand parseYaml(ConfigSection baseSection) {
		CommandConfig values = createCommandConfig(baseSection, 0);
		var context = new ParseContext(values.name(), true);
		
		CustomCommandBuilder builder = CustomCommand.builder()
		  .values(values)
		  .component((LiteralCommandComponent) CommandComponents.parseCommandComponent(context, baseSection))
		  .children(parseChildren(context, baseSection));
		
		return builder.build();
	}
	
	private static CommandSection parseCommandSection(ParseContext context, ConfigSection baseSection) {
		List<CommandSection> baseChildren = new ArrayList<>();
		
		for(var key : baseSection.getKeys(false)) {
			int prefixLength = getPrefixLength(key);
			
			if(prefixLength == -1)
				continue;
			
			var childContext = context.withArgumentName(key.substring(prefixLength));
			var childSection = baseSection.getSection(key);
			
			assert childSection != null;
			
			var childValues = createCommandConfig(childSection, prefixLength);
			var childComponent = CommandComponents.parseCommandComponent(childContext, childSection);
			var childChildren = parseChildren(childContext, childSection);
			
			baseChildren.add(new CommandSection(childValues, childComponent, childChildren));
		}
		
		var prefixLength = getPrefixLength(baseSection.getName());
		var baseValues = createCommandConfig(baseSection, prefixLength);
		var baseComponent = CommandComponents.parseCommandComponent(context, baseSection);
		
		return new CommandSection(baseValues, baseComponent, List.copyOf(baseChildren));
	}
	
	private static List<CommandSection> parseChildren(ParseContext context, ConfigSection baseSection) {
		List<CommandSection> out = new ArrayList<>();
		
		for(var key : baseSection.getKeys(false)) {
			int prefixLength = getPrefixLength(key);
			
			if(prefixLength == -1)
				continue;
			
			var childContext = context.withArgumentName(key.substring(prefixLength));
			var childSection = baseSection.getSection(key);
			
			assert childSection != null;
			
			out.add(parseCommandSection(childContext, childSection));
		}
		
		return out;
	}
	
	private static int getPrefixLength(String key) {
		if(key.startsWith("literal:"))
			return "literal:".length();
		else if(key.startsWith("argument:"))
			return "argument:".length();
		else
			return -1;
	}
	
	private static CommandConfig createCommandConfig(ConfigSection section, int prefixLength) {
		return new CommandConfig(
		  section.getName().substring(prefixLength),
		  section.getStringList("aliases"),
		  section.getString("description"),
		  section.getString("permission"),
		  section.getBoolean("console-only", false),
		  new HashMap<>(),
		  section.getBoolean("enabled", true)
		);
	}
}
