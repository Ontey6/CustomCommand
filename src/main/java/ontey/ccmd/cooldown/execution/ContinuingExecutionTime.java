package ontey.ccmd.cooldown.execution;

import lombok.NonNull;
import ontey.api.util.DurationFormatter;
import ontey.ccmd.cooldown.Cooldown;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.time.Duration;
import java.util.Map;

/// An [ExecutionTime] that is created for a stored cooldown after a restart.
///
/// @param startTimeMillis The start (usually [System#currentTimeMillis()])
/// @param remainingTimeMillis The remaining millis

public record ContinuingExecutionTime(@NonNull Cooldown cooldown, long startTimeMillis,
                                      long remainingTimeMillis) implements ExecutionTime {
	
	@Override
	public @Range(from = 0L, to = Long.MAX_VALUE) long remaining() {
		return Math.max(0, remainingTimeMillis - (System.currentTimeMillis() - startTimeMillis));
	}
	
	@Override
	public @NonNull String formatRemaining() {
		return DurationFormatter.formatHumanReadable(Duration.ofMillis(remaining()));
	}
	
	@Override
	public @NonNull String formatCooldownMessage() {
		return cooldown.message().format(formatRemaining());
	}
	
	@Override
	public @NonNull String type() {
		return "continuing";
	}
	
	@Override
	public @NotNull Map<String, Object> serialize() {
		return Map.of(
		  "type", type(),
		  "remaining", DurationFormatter.formatHumanReadable(Duration.ofMillis(remaining()))
		);
	}
	
	@Override
	public @NonNull ExecutionTime createNext() {
		return new BasicExecutionTime(cooldown, System.currentTimeMillis());
	}
}
