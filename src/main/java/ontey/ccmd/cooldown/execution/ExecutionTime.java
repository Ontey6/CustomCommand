package ontey.ccmd.cooldown.execution;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentLike;
import ontey.api.serialization.CombinedConfigSerializable;
import ontey.ccmd.cooldown.Cooldown;
import ontey.ccmd.util.DurationUtil;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Range;

import java.util.Map;

public interface ExecutionTime extends ComponentLike, CombinedConfigSerializable {
	
	/// @return A basic `LastExecutionTime`
	
	@NonNull
	@Contract(pure = true)
	static ExecutionTime basic(@NonNull Cooldown cooldown) {
		return new BasicExecutionTime(cooldown, System.currentTimeMillis());
	}
	
	/// @param timeMillis The time millis (usually [System#currentTimeMillis])
	/// @return A basic `LastExecutionTime`
	
	@NonNull
	@Contract(pure = true)
	static ExecutionTime basic(@NonNull Cooldown cooldown, long timeMillis) {
		return new BasicExecutionTime(cooldown, timeMillis);
	}
	
	/// @param remainingTimeMillis The remaining time millis
	/// @return A basic `LastExecutionTime`
	
	@NonNull
	@Contract(pure = true)
	static ExecutionTime continuing(@NonNull Cooldown cooldown, long remainingTimeMillis) {
		return new ContinuingExecutionTime(cooldown, System.currentTimeMillis(), remainingTimeMillis);
	}
	
	/// @param startTimeMillis The start of this continuing [ExecutionTime] (usually [System#currentTimeMillis()])
	/// @param remainingTimeMillis The remaining time millis
	/// @return A basic `LastExecutionTime`
	
	@NonNull
	@Contract(pure = true)
	static ExecutionTime continuing(@NonNull Cooldown cooldown, long startTimeMillis, long remainingTimeMillis) {
		return new ContinuingExecutionTime(cooldown, startTimeMillis, remainingTimeMillis);
	}
	
	@NonNull
	static ExecutionTime deserialize(@NonNull Cooldown cooldown, @NonNull Map<String, Object> map) {
		var rawType = map.get("type");
		
		if(rawType == null)
			throw new IllegalArgumentException("map doesn't contain a type");
		
		var type = rawType.toString();
		
		return switch(type) {
			case "basic" -> {
				var rawMillis = map.get("start");
				long millis = rawMillis instanceof Long l ? l : 0L;
				yield basic(cooldown, millis);
			}
			case "continuing" -> {
				var rawMillis = map.get("remaining");
				long millis = rawMillis == null ? 0L : DurationUtil.parseDuration(rawMillis.toString(), "remaining").toMillis();
				yield continuing(cooldown, millis);
			}
			case "disabled_permanent" -> CooldownlessExecutionTime.permanent();
			case "disabled_temporary" -> {
				var rawConverted = map.get("convert");
				
				if(!(rawConverted instanceof Map<?, ?> nestedMap))
					throw new IllegalArgumentException("Can't resolve 'convert' as it's not a map");
				
				var converted = deserialize(cooldown, (Map<String, Object>) nestedMap);
				
				yield CooldownlessExecutionTime.temporary(converted);
			}
			default -> throw new IllegalArgumentException("Unknown type: '" + type + "'");
		};
	}
	
	/// @return The remaining millis at this time
	
	@Range(from = 0L, to = Long.MAX_VALUE)
	@Contract(pure = true)
	long remaining();
	
	/// @return A human-readable, formatted version of [#remaining()]
	
	@Contract(pure = true)
	@NonNull
	String formatRemaining();
	
	/// @return Utility method that formats the message of this [ExecutionTime]'s cooldown if it has one
	/// @throws UnsupportedOperationException If this [ExecutionTime] is cooldownless ([CooldownlessExecutionTime])
	
	@Contract(pure = true)
	@NonNull
	String formatCooldownMessage() throws UnsupportedOperationException;
	
	/// @return This [ExecutionTime]'s type used for serialization
	
	@Contract(pure = true)
	@NonNull
	String type();
	
	/// @return If this [ExecutionTime] is expired; the associated command (section) can be run by the owner this has expired for again.
	
	@Contract(pure = true)
	default boolean isExpired() {
		return remaining() == 0;
	}
	
	/// @return A component representation of this [ExecutionTime]
	
	@Override
	default @NonNull Component asComponent() {
		return Component.text(toString());
	}
	
	/// When the cooldown [#isExpired()] and the owner runs the associated command (section), a new [ExecutionTime] reflecting the new state has to be created.
	/// This method is used for that.
	/// [CooldownlessExecutionTime]s may return `this`.
	/// There is no guarantee on the class or serializability of the returned [ExecutionTime].
	
	@NonNull
	ExecutionTime createNext();
}
