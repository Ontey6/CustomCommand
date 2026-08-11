package ontey.ccmd.command.context;

import lombok.NonNull;
import ontey.ccmd.command.exception.ParseException;
import org.jetbrains.annotations.Nullable;

//TODO replace manually thrown exceptions
public record ParseContext(String rootName, @Nullable String currentArgumentName, @Nullable String currentSection,
                           boolean isRoot) {
	
	public ParseContext(String rootName, String currentArgumentName) {
		this(rootName, currentArgumentName, null, false);
	}
	
	public ParseContext(String rootName) {
		this(rootName, null, null, false);
	}
	
	public ParseContext(String rootName, String currentArgumentName, boolean isRoot) {
		this(rootName, currentArgumentName, null, isRoot);
	}
	
	public ParseContext(String rootName, boolean isRoot) {
		this(rootName, null, null, isRoot);
	}
	
	/**
	 * Creates a new {@link ParseException} with this context and the given error message.
	 * Uses {@link #buildErrorMessage(String)}.
	 */
	
	public ParseException newException(String errorMessage) {
		return new ParseException(buildErrorMessage(errorMessage));
	}
	
	/**
	 * Creates a new {@link ParseException} with this context, the given error message and the given cause.
	 * Uses {@link #buildErrorMessage(String)}.
	 */
	
	public ParseException newException(String errorMessage, Throwable cause) {
		return new ParseException(buildErrorMessage(errorMessage), cause);
	}
	
	/**
	 * If {@link #currentSection} is null, builds {@code {'rootName':'currentArgumentName'} errorMessage}.
	 * <p>
	 * Otherwise, builds {@code {'rootName':'currentArgumentName':'currentSection'} errorMessage}.
	 */
	
	private String buildErrorMessage(String errorMessage) {
		StringBuilder sb = new StringBuilder("{'").append(rootName);
		
		if(currentArgumentName != null) {
			sb.append("':'").append(currentArgumentName);
			
			if(currentSection != null)
				sb.append("':'").append(currentSection);
		}
		
		sb.append("'} ").append(errorMessage);
		return sb.toString();
	}
	
	@Nullable
	public String name() {
		return isRoot() ? rootName : currentArgumentName;
	}
	
	public ParseContext withArgumentName(String argumentName) {
		return new ParseContext(rootName, argumentName, currentSection, false);
	}
	
	public ParseContext withSection(@NonNull String sectionName) {
		return new ParseContext(rootName, currentArgumentName, sectionName, isRoot);
	}
}
