package ontey.ccmd.cooldown.execution;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

final class PermanentCooldownlessExecutionTime implements CooldownlessExecutionTime {
	
	public static final @NonNull PermanentCooldownlessExecutionTime INSTANCE = new PermanentCooldownlessExecutionTime();
	
	private PermanentCooldownlessExecutionTime() {
	
	}
	
	@Override
	public @NonNull String identifier() {
		return "permanent";
	}
	
	@Override
	public boolean isPermanent() {
		return true;
	}
	
	@Override
	public @NotNull Map<String, Object> serialize() {
		return Map.of("type", type());
	}
	
	@Override
	public @NonNull ExecutionTime createNext() {
		return this;
	}
	
	@Override
	public String toString() {
		return getClass().getSimpleName();
	}
}
