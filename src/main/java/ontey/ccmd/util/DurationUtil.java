package ontey.ccmd.util;

import lombok.NonNull;
import ontey.ccmd.command.context.ParseContext;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;

public final class DurationUtil {
	
	public static Duration parseDuration(@Nullable String input, @NonNull String fieldName, @NonNull ParseContext context) {
		if(input == null || input.isBlank())
			return Duration.ZERO;
		
		Duration totalDuration = Duration.ZERO;
		long currentValue = 0;
		
		for(int i = 0; i < input.length(); i++) {
			char c = input.charAt(i);
			
			if(Character.isDigit(c)) {
				currentValue = currentValue * 10 + toNumber(c);
				continue;
			}
			
			if(Character.isLetter(c)) {
				if(c == 'm' && i + 1 < input.length() && input.charAt(i + 1) == 's') {
					totalDuration = totalDuration.plusMillis(currentValue);
					i++;
					continue;
				}
				
				switch(c) {
					case 't' -> totalDuration = totalDuration.plusMillis(currentValue * 50);
					case 's' -> totalDuration = totalDuration.plusSeconds(currentValue);
					case 'm' -> totalDuration = totalDuration.plusMinutes(currentValue);
					case 'h' -> totalDuration = totalDuration.plusHours(currentValue);
					case 'd' -> totalDuration = totalDuration.plusDays(currentValue);
				}
				
				currentValue = 0;
				continue;
			}
			
			throw context.newException("'" + fieldName + "' is an invalid time format: '" + input + "'");
		}
		
		return totalDuration;
	}
	
	private static int toNumber(char c) {
		return c - '0';
	}
}
