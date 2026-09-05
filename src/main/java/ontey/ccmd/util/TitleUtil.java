package ontey.ccmd.util;

import lombok.NonNull;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import ontey.api.config.ConfigSection;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.format.Formatter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

public final class TitleUtil {
	
	public static void showTitle(@NonNull Player player, @NonNull String rawTitle, @Nullable String rawSubtitle, @NonNull Title.Times times) {
		player.sendTitlePart(TitlePart.TITLE, Formatter.format(rawTitle, player));
		if(rawSubtitle != null)
			player.sendTitlePart(TitlePart.SUBTITLE, Formatter.format(rawSubtitle, player));
		player.sendTitlePart(TitlePart.TIMES, times);
	}
	
	@NonNull
	public static Title.Times getTimes(@NonNull ConfigSection argumentSection, ParseContext context) {
		var timesSection = argumentSection.getSection("times");
		
		if(timesSection == null)
			return Title.DEFAULT_TIMES;
		
		var fadeIn = getDuration(timesSection.getString("fade-in"), "fade-in", context);
		var stay = getDuration(timesSection.getString("stay"), "stay", context);
		var fadeOut = getDuration(timesSection.getString("fade-out"), "fade-out", context);
		
		return Title.Times.times(fadeIn, stay, fadeOut);
	}
	
	public static Title.Times getTimes(@NonNull String fadeIn, @NonNull String stay, @NonNull String fadeOut, ParseContext context) {
		var _fadeIn = getDuration(fadeIn, "fade-in", context);
		var _stay = getDuration(stay, "stay", context);
		var _fadeOut = getDuration(fadeOut, "fade-out", context);
		
		return Title.Times.times(_fadeIn, _stay, _fadeOut);
	}
	
	@NonNull
	private static Duration getDuration(@Nullable String times, @NonNull String fieldName, ParseContext context) {
		if(times == null)
			return Duration.ZERO;
		
		if(!matchesTimesFormat(times))
			throw context.newException("The field '" + fieldName + "' is not a valid time format (It should be an integer followed by ms/t/s/m/h/d)");
		
		int backshift = times.endsWith("ms") ? 2 : 1;
		
		var value = Long.parseLong(times.substring(0, times.length() - backshift));
		var timeUnit = switch(times.substring(times.length() - backshift)) {
			case "ms" -> ChronoUnit.MILLIS;
			case "t" -> null;
			case "s" -> ChronoUnit.SECONDS;
			case "m" -> ChronoUnit.MINUTES;
			case "h" -> ChronoUnit.HOURS;
			case "d" -> ChronoUnit.DAYS;
			default -> throw new IllegalStateException("This is a bug. Report to developer.");
		};
		
		if(timeUnit == null)
			return Duration.of(value * 50, ChronoUnit.MILLIS);
		
		return Duration.of(value, timeUnit);
	}
	
	private static boolean matchesTimesFormat(String input) {
		return input.matches("\\d+(ms|t|s|m|h|d)");
	}
}
