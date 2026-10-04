package ontey.ccmd.cooldown.execution;

import lombok.NonNull;
import ontey.api.util.DurationFormatter;
import ontey.ccmd.cooldown.Cooldown;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import java.time.Duration;
import java.util.Map;

record BasicExecutionTime(@NonNull Cooldown cooldown, long startTimeMillis) implements ExecutionTime {
	
	@Range(from = 0L, to = Long.MAX_VALUE)
	public long remaining() {
		var current = System.currentTimeMillis();
		var passedMillis = current - startTimeMillis;
		var remainingMillis = cooldown.duration().toMillis() - passedMillis;
		
		return Math.max(remainingMillis, 0L);
	}
	
	@NonNull
	public String formatRemaining() {
		return DurationFormatter.formatHumanReadable(Duration.ofMillis(remaining()));
	}
	
	@NonNull
	public String formatCooldownMessage() {
		return cooldown.message().format(formatRemaining());
	}
	
	@Override
	public @NonNull String type() {
		return "basic";
	}
	
	public boolean isExpired() {
		return remaining() == 0;
	}
	
	@Override
	public @NotNull Map<String, Object> serialize() {
		return Map.of(
		  "type", type(),
		  "start", startTimeMillis
		);
	}
	
	@Override
	public @NonNull ExecutionTime createNext() {
		return new BasicExecutionTime(cooldown, System.currentTimeMillis());
	}
}
