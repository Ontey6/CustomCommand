package ontey.ccmd.command.suggestion;

import lombok.NonNull;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import ontey.api.config.ConfigSection;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.command.suggestion.entry.SuggestionEntry;
import ontey.ccmd.command.suggestion.registry.SuggestionRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static ontey.ccmd.command.suggestion.registry.SuggestionRegistry.register;

public final class Suggestions {
	
	private Suggestions() {
		throw new UnsupportedOperationException();
	}
	
	@SuppressWarnings("PatternValidation")
	@Nullable
	public static Suggestion parseSuggestions(@NonNull ParseContext context, @Nullable ConfigSection suggestsSection) {
		if(suggestsSection == null)
			return null;
		
		String type = suggestsSection.getString("type");
		
		if(type == null)
			throw context.newException("Suggestion type 'suggests.type' not set");
		
		type = type.toLowerCase().replace(' ', '_').replace('-', '_');
		
		if(!type.matches("(?:([a-z0-9_\\-.]+:)?|:)[a-z0-9_\\-./]+"))
			throw context.newException("Suggestion type doesn't match required pattern!");
		
		var key = type.contains(":")
		  ? Key.key(type)
		  : Key.key("ccmd", type);
		
		var creator = SuggestionRegistry.get(key);
		
		if(creator == null)
			throw context.newException("Suggestion type '" + key.asString() + "' doesn't exist");
		
		return creator.createSuggestion(context, suggestsSection);
	}
	
	public static void registerDefaultSuggestions() {
		register(key("javascript_reference"), (context, baseSection) -> {
			var javascriptFile = baseSection.getString("javascript-file");
			
			if(javascriptFile == null)
				throw context.newException("String 'javascript-file' not set");
			
			return new JavascriptReferenceSuggestion(javascriptFile);
		});
		
		register(key("javascript"), (context, baseSection) -> {
			var javascript = baseSection.getString("javascript");
			
			if(javascript == null)
				throw context.newException("String 'javascript' not set");
			
			return new JavascriptSuggestion(javascript);
		});
		
		register(key("list"), (context, baseSection) -> {
			List<?> rawSuggestions = baseSection.getList("list");
			
			if(rawSuggestions == null)
				throw context.newException("List 'list' not set");
			
			List<SuggestionEntry> suggestions = new ArrayList<>(rawSuggestions.size());
			
			for(Object rawSuggestion : rawSuggestions)
				suggestions.add(SuggestionEntry.deserialize(rawSuggestion));
			
			if(suggestions.contains(null))
				throw context.newException("Suggestions list contains invalid suggestions");
			
			boolean dynamic = baseSection.getBoolean("dynamic");
			
			return new ListSuggestion(suggestions, dynamic);
		});
	}
	
	@NonNull
	private static Key key(@NonNull @KeyPattern.Value String value) {
		return Key.key("ccmd", value);
	}
}
