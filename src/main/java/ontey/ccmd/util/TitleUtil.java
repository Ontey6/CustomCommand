package ontey.ccmd.util;

import lombok.NonNull;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.format.Formatter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import static ontey.ccmd.util.DurationUtil.parseDuration;

public final class TitleUtil {
	
	public static void showTitle(@NonNull Player player, @NonNull String rawTitle, @Nullable String rawSubtitle, @NonNull Title.Times times) {
		player.sendTitlePart(TitlePart.TITLE, Formatter.format(rawTitle, player));
		if(rawSubtitle != null)
			player.sendTitlePart(TitlePart.SUBTITLE, Formatter.format(rawSubtitle, player));
		player.sendTitlePart(TitlePart.TIMES, times);
	}
	
	public static Title.Times getTimes(@NonNull String fadeIn, @NonNull String stay, @NonNull String fadeOut, ParseContext context) {
		var _fadeIn = parseDuration(fadeIn, "fade-in", context);
		var _stay = parseDuration(stay, "stay", context);
		var _fadeOut = parseDuration(fadeOut, "fade-out", context);
		
		return Title.Times.times(_fadeIn, _stay, _fadeOut);
	}
}
