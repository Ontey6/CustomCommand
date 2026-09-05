package ontey.ccmd.command.requirement;

import lombok.NonNull;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import ontey.api.config.ConfigSection;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.command.requirement.registry.RequirementRegistry;
import org.jetbrains.annotations.Nullable;

import static ontey.ccmd.command.requirement.registry.RequirementRegistry.register;

public final class Requirements {
	
	private Requirements() {
		throw new UnsupportedOperationException();
	}
	
	@SuppressWarnings("PatternValidation")
	@Nullable
	public static Requirement parseRequirement(@NonNull ParseContext context, @Nullable ConfigSection requiresSection) {
		if(requiresSection == null)
			return null;
		
		String type = requiresSection.getString("type");
		
		if(type == null)
			throw context.newException("Requirement type 'requires.type' not set");
		
		type = type.toLowerCase().replace(' ', '_').replace('-', '_');
		
		if(!type.matches("(?:([a-z0-9_\\-.]+:)?|:)[a-z0-9_\\-./]+"))
			throw context.newException("Requirement type doesn't match required pattern!");
		
		var key = type.contains(":")
		  ? Key.key(type)
		  : Key.key("ccmd", type);
		
		var creator = RequirementRegistry.get(key);
		
		if(creator == null)
			throw context.newException("Requirement type '" + key.asString() + "' doesn't exist");
		
		return creator.createRequirement(context, requiresSection);
	}
	
	public static void registerDefaultRequirements() {
		register(key("javascript"), (context, baseSection) -> {
			var javascript = baseSection.getString("javascript");
			
			if(javascript == null)
				throw context.newException("String 'javascript' not set");
			
			return new JavascriptRequirement(javascript);
		});
		
		register(key("javascript_reference"), (context, baseSection) -> {
			var javascriptFile = baseSection.getString("javascript-file");
			
			if(javascriptFile == null)
				throw context.newException("String 'javascript-file' not set");
			
			return new JavascriptReferenceRequirement(javascriptFile);
		});
	}
	
	@NonNull
	private static Key key(@NonNull @KeyPattern.Value String value) {
		return Key.key("ccmd", value);
	}
}
