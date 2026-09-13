package ontey.ccmd.command.parser;

import lombok.NonNull;
import ontey.api.config.ConfigSection;
import ontey.ccmd.command.CommandSection;
import ontey.ccmd.command.CustomCommand;
import ontey.ccmd.command.component.CommandComponents;
import ontey.ccmd.command.component.LiteralCommandComponent;
import ontey.ccmd.command.config.CustomCommandConfig;
import ontey.ccmd.command.context.ParseContext;
import org.intellij.lang.annotations.MagicConstant;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CustomCommandParser {
	
	/// Parses a [CustomCommand] from the given section.
	///
	/// @param baseSection The command's section
	
	@NonNull
	public static CustomCommand parseYaml(@NonNull ConfigSection baseSection, @NonNull File file) {
		var context = new ParseContext(baseSection.getName(), true);
		var values = CustomCommandConfig.deserialize(context, baseSection, 0);
		var commandComponent = (LiteralCommandComponent) CommandComponents.parseCommandComponent(context, baseSection);
		var children = parseChildren(context, baseSection);
		
		return new CustomCommand(values, commandComponent, file, children);
	}
	
	private static CommandSection parseCommandSection(ParseContext baseContext, ConfigSection baseSection) {
		List<CommandSection> baseChildren = new ArrayList<>();
		
		for(var key : baseSection.getKeys(false)) {
			if(!baseSection.isSection(key))
				continue;
			
			int prefixLength = getPrefixLength(key);
			
			if(prefixLength == -1)
				continue;
			
			var childContext = baseContext.withArgumentName(key.substring(prefixLength));
			var childSection = baseSection.getSection(key);
			
			assert childSection != null;
			
			var childValues = CustomCommandConfig.deserialize(childContext, childSection, prefixLength);
			var childComponent = CommandComponents.parseCommandComponent(childContext, childSection);
			var childChildren = parseChildren(childContext, childSection);
			
			baseChildren.add(new CommandSection(childValues, childComponent, childChildren));
		}
		
		var prefixLength = getPrefixLength(baseSection.getName());
		var baseValues = CustomCommandConfig.deserialize(baseContext, baseSection, prefixLength);
		var baseComponent = CommandComponents.parseCommandComponent(baseContext, baseSection);
		
		return new CommandSection(baseValues, baseComponent, baseChildren);
	}
	
	private static List<CommandSection> parseChildren(ParseContext context, ConfigSection baseSection) {
		List<CommandSection> out = new ArrayList<>();
		
		for(var key : baseSection.getKeys(false)) {
			if(!baseSection.isSection(key))
				continue;
			
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
	
	@MagicConstant(intValues = {8, 9, -1})
	private static int getPrefixLength(String key) {
		if(key.startsWith("literal:"))
			return 8;
		else if(key.startsWith("argument:"))
			return 9;
		else
			return -1;
	}
}
