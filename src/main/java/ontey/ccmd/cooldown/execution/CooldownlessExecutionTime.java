package ontey.ccmd.cooldown.execution;

import lombok.NonNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;

import java.util.function.Supplier;

public interface CooldownlessExecutionTime extends ExecutionTime {
	
	@NonNull
	static CooldownlessExecutionTime permanent() {
		return PermanentCooldownlessExecutionTime.INSTANCE;
	}
	
	@NonNull
	static CooldownlessExecutionTime temporary(@NonNull ExecutionTime convert) {
		return new TemporaryCooldownlessExecutionTime(() -> convert);
	}
	
	@NonNull
	static CooldownlessExecutionTime temporary(@NonNull Supplier<ExecutionTime> convertSupplier) {
		return new TemporaryCooldownlessExecutionTime(convertSupplier);
	}
	
	/// `CooldownlessLastExecutionTime`s don't ever have a cooldown
	///
	/// @return `0`
	
	@Override
	@Range(from = 0L, to = Long.MAX_VALUE)
	@Contract(pure = true)
	default long remaining() {
		return 0;
	}
	
	/// `CooldownlessLastExecutionTime`s don't ever have a cooldown
	///
	/// @return `0s`
	
	@Override
	@NonNull
	@Contract(pure = true)
	default String formatRemaining() {
		return "0s";
	}
	
	/// `CooldownlessLastExecutionTime`s don't have a cooldown message.
	///
	/// @throws UnsupportedOperationException always
	
	@Override
	@NonNull
	@Contract(pure = true)
	default String formatCooldownMessage() throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}
	
	/// `CooldownlessLastExecutionTime`s don't ever have a cooldown, so they are technically always expired.
	///
	/// @return `true`
	
	@Override
	@Contract(pure = true)
	default boolean isExpired() {
		return true;
	}
	
	@Override
	@NonNull
	@Contract(pure = true)
	default String type() {
		return "disabled_" + identifier();
	}
	
	@NonNull
	@Contract(pure = true)
	String identifier();
	
	/// @return `true` - Blocks all cooldown attempts
	///         <br>
	///         `false` - Only blocks the next cooldown attempt, then converts to a different [ExecutionTime] ([#createNext()])
	
	boolean isPermanent();
}
