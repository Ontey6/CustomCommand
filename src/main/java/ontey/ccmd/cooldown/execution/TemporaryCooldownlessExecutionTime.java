package ontey.ccmd.cooldown.execution;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.function.Supplier;

record TemporaryCooldownlessExecutionTime(
  @NonNull Supplier<ExecutionTime> convert) implements CooldownlessExecutionTime {
	
	@Override
	public @NonNull String identifier() {
		return "temporary";
	}
	
	@Override
	public boolean isPermanent() {
		return false;
	}
	
	@Override
	public @NotNull Map<String, Object> serialize() {
		return Map.of(
		  "type", type(),
		  "convert", convert.get().serialize()
		);
	}
	
	@Override
	public @NonNull ExecutionTime createNext() {
		return convert.get();
	}
}
