package ontey.ccmd.js;

import ontey.ccmd.command.suggestion.entry.SuggestionEntry;

public final class Suggestions {
	
	public static final Suggestions INSTANCE = new Suggestions();
	
	private Suggestions() {
	}
	
	public SuggestionEntry string(String suggestion, String tooltip) {
		return SuggestionEntry.string(suggestion, tooltip);
	}
	
	public SuggestionEntry string(String suggestion) {
		return SuggestionEntry.string(suggestion);
	}
	
	public SuggestionEntry integer(int suggestion, String tooltip) {
		return SuggestionEntry.integer(suggestion, tooltip);
	}
	
	public SuggestionEntry integer(int suggestion) {
		return SuggestionEntry.integer(suggestion);
	}
}
