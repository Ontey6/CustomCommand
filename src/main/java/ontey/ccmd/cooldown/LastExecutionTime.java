package ontey.ccmd.cooldown;

import lombok.NonNull;
import ontey.api.util.DurationFormatter;
import org.jetbrains.annotations.Range;

import java.time.Duration;

public record LastExecutionTime(Duration cooldown, long lastExecutionMillis) {
	
	@Range(from = 0L, to = Long.MAX_VALUE)
	public long getRemaining() {
		var current = System.currentTimeMillis();
		var passedMillis = current - lastExecutionMillis;
		var remainingMillis = cooldown.toMillis() - passedMillis;
		
		return remainingMillis > 0 ? remainingMillis : 0L;
	}
	
	@NonNull
	public String formatRemaining() {
		var remainingMillis = getRemaining();
		var remainingDuration = Duration.ofMillis(remainingMillis);
		
		return DurationFormatter.formatHumanReadable(remainingDuration);
	}
	
	public boolean isExpired() {
		return getRemaining() == 0;
	}
}
