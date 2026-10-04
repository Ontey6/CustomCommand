package ontey.ccmd.command.data;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import ontey.ccmd.command.CommandSectionLike;
import ontey.ccmd.command.CustomCommand;
import ontey.ccmd.cooldown.Cooldown;
import ontey.ccmd.cooldown.execution.ExecutionTime;
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
	private final CommandSectionLike sectionLike;
	
	@Nullable
	private Map<CooldownOwner, ExecutionTime> lastExecutionTimes;
	
	@Getter
	@Setter
	private CustomCommand root;
	
	@Getter
	@Setter
	private CommandSectionLike parent;
	
	@NonNull
	public static Map<CommandSectionLike, CommandData> getData() {
		return Map.copyOf(data);
	}
	
	@NonNull
	public static CommandData getOrCreateData(@NonNull CommandSectionLike sectionLike) {
		return data.computeIfAbsent(sectionLike, CommandData::new);
	}
	
	public @NonNull Map<CooldownOwner, ExecutionTime> getLastExecutionTimes() {
		return lastExecutionTimes == null ? Map.of() : Map.copyOf(lastExecutionTimes);
	}
	
	@Nullable
	public ExecutionTime getLastExecutionTime(@NonNull CooldownOwner owner) {
		if(lastExecutionTimes == null)
			return null;
		
		return lastExecutionTimes.get(owner);
	}
	
	public void setExecutionTime(@NonNull CooldownOwner owner) {
		if(lastExecutionTimes == null)
			lastExecutionTimes = new HashMap<>();
		
		var cooldown = sectionLike.values().cooldown();
		
		if(cooldown != Cooldown.ZERO)
			lastExecutionTimes.compute(owner, (_, previousExecutionTime) -> previousExecutionTime == null ? ExecutionTime.basic(cooldown) : previousExecutionTime.createNext());
	}
	
	public void setExecutionTime(@NonNull CooldownOwner owner, ExecutionTime executionTime) {
		if(lastExecutionTimes == null)
			lastExecutionTimes = new HashMap<>();
		
		var cooldown = sectionLike.values().cooldown();
		
		if(cooldown != Cooldown.ZERO)
			lastExecutionTimes.put(owner, executionTime);
	}
}
