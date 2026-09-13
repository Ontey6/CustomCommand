package ontey.ccmd.command.data;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ontey.ccmd.command.CommandSectionLike;
import ontey.ccmd.cooldown.LastExecutionTime;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/// Mutable data of a command (section)

@RequiredArgsConstructor
public class CommandData {
	
	private static final Map<CommandSectionLike, CommandData> data = new HashMap<>();
	
	@NonNull
	private final CommandSectionLike sectionLike;
	
	@Nullable
	private Map<UUID, LastExecutionTime> cooldowns;
	
	@NonNull
	public static CommandData getOrCreateData(@NonNull CommandSectionLike sectionLike) {
		return data.computeIfAbsent(sectionLike, CommandData::new);
	}
	
	@Nullable
	public LastExecutionTime getLastExecutionTime(@NonNull Player player) {
		if(cooldowns == null)
			return null;
		
		return cooldowns.get(player.getUniqueId());
	}
	
	public void setLastExecutionTime(@NonNull Player player, long lastExecutionTime) {
		if(cooldowns == null)
			cooldowns = new HashMap<>();
		
		cooldowns.put(player.getUniqueId(), new LastExecutionTime(sectionLike.values().cooldown(), lastExecutionTime));
	}
	
	public void setLastExecutionTime(@NonNull Player player) {
		setLastExecutionTime(player, System.currentTimeMillis());
	}
}
