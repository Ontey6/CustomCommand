package ontey.ccmd.command.data;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ontey.ccmd.command.CommandSectionLike;
import ontey.ccmd.cooldown.LastExecutionTime;
import ontey.ccmd.cooldown.owner.CooldownOwner;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/// Mutable data of a command (section)

@RequiredArgsConstructor
public class CommandData {
	
	@NonNull
	private static final Map<CommandSectionLike, CommandData> data = new HashMap<>();
	
	@NonNull
	public static Map<CommandSectionLike, CommandData> getData() {
		return Map.copyOf(data);
	}
	
	@NonNull
	private final CommandSectionLike sectionLike;
	
	@Nullable
	private Map<CooldownOwner, LastExecutionTime> lastExecutionTimes;
	
	public @NonNull Map<CooldownOwner, LastExecutionTime> getLastExecutionTimes() {
		return lastExecutionTimes == null ? Map.of() : Map.copyOf(lastExecutionTimes);
	}
	
	@NonNull
	public static CommandData getOrCreateData(@NonNull CommandSectionLike sectionLike) {
		return data.computeIfAbsent(sectionLike, CommandData::new);
	}
	
	@Nullable
	public LastExecutionTime getLastExecutionTime(@NonNull CooldownOwner owner) {
		if(lastExecutionTimes == null)
			return null;
		
		return lastExecutionTimes.get(owner);
	}
	
	public void setLastExecutionTime(@NonNull CooldownOwner owner, long lastExecutionTime) {
		if(lastExecutionTimes == null)
			lastExecutionTimes = new HashMap<>();
		
		var cooldown = sectionLike.values().cooldown();
		
		if(cooldown != null)
			lastExecutionTimes.put(owner, new LastExecutionTime(cooldown.duration(), lastExecutionTime));
	}
	
	public void setLastExecutionTime(@NonNull CooldownOwner owner) {
		setLastExecutionTime(owner, System.currentTimeMillis());
	}
}
