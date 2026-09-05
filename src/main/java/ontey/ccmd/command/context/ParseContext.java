package ontey.ccmd.command.context;

import lombok.NonNull;
import ontey.ccmd.command.exception.ParseException;
import org.jetbrains.annotations.Nullable;

/**
 * @param rootName The command's name
 * @param currentArgumentName The name of the argument that is currently being parsed. If {@link #isRoot}, returns {@code rootName}.
 * @param currentSection If a subsection of an argument is currently being parsed, this is the name of the subsection.
 * @param isRoot Whether the current argument is the root of the command
 */

public record ParseContext(@NonNull String rootName, @NonNull String currentArgumentName,
                           @Nullable String currentSection, boolean isRoot) {
	
	public ParseContext(String rootName, String currentArgumentName) {
		this(rootName, currentArgumentName, null, false);
	}
	
	public ParseContext(String rootName) {
		this(rootName, rootName, null, false);
	}
	
	public ParseContext(String rootName, String currentArgumentName, boolean isRoot) {
		this(rootName, currentArgumentName, null, isRoot);
	}
	
	public ParseContext(String rootName, boolean isRoot) {
		this(rootName, rootName, null, isRoot);
	}
	
	/**
	 * Creates a new {@link ParseException} with this context and the given error message.
	 * Uses {@link #buildErrorMessage(String)}.
	 */
	
	public ParseException newException(@NonNull String errorMessage) {
		return new ParseException(buildErrorMessage(errorMessage));
	}
	
	/**
	 * Creates a new {@link ParseException} with this context, the given error message and the given cause.
	 * Uses {@link #buildErrorMessage(String)}.
	 */
	
	public ParseException newException(@NonNull String errorMessage, @NonNull Throwable cause) {
		return new ParseException(buildErrorMessage(errorMessage), cause);
	}
	
	/**
	 * If {@link #currentSection} is null, builds {@code {'rootName':'currentArgumentName'} errorMessage}.
	 * <p>
	 * Otherwise, builds {@code {'rootName':'currentArgumentName':'currentSection'} errorMessage}.
	 */
	
	private String buildErrorMessage(String errorMessage) {
		StringBuilder sb = new StringBuilder("{'").append(rootName);
		
		if(!isRoot)
			sb.append("':'").append(currentArgumentName);
		
		if(currentSection != null)
			sb.append("':'").append(currentSection);
		
		sb.append("'} ").append(errorMessage);
		return sb.toString();
	}
	
	@NonNull
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
