package ontey.ccmd.cooldown;

import lombok.NonNull;
import ontey.api.config.ConfigSection;
import ontey.ccmd.command.CommandSectionLike;
import ontey.ccmd.command.registry.CustomCommandRegistry;
import ontey.ccmd.cooldown.execution.CooldownlessExecutionTime;
import ontey.ccmd.cooldown.execution.ExecutionTime;
import ontey.ccmd.cooldown.owner.CooldownOwner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static ontey.ccmd.shared.SharedConstants.cooldownStorage;
import static ontey.ccmd.shared.SharedConstants.logger;

public final class CooldownManager {
	
	private CooldownManager() {
	
	}
	
	public static void saveCooldowns() {
		saveCooldowns(CustomCommandRegistry.getRegisteredCommands(), cooldownStorage);
		cleanupSections(cooldownStorage);
		try {
			cooldownStorage.save();
		} catch(IOException e) {
			logger.error("Couldn't save file cooldowns.yml");
		}
	}
	
	private static void cleanupSections(@NonNull ConfigSection current) {
		if(current.isEmpty()) {
			var parent = current.getParent();
			
			if(parent != null) {
				parent.remove(current.getName());
				cleanupSections(parent);
			}
		} else {
			if(current.getMapList("__cooldowns__").isEmpty())
				current.remove("__cooldowns__");
			
			for(var key : current.getKeys(false)) {
				var next = current.getSection(key);
				
				if(next != null)
					cleanupSections(next);
			}
		}
	}
	
	//TODO doesn't work
	private static void saveCooldowns(@NonNull List<? extends CommandSectionLike> list, @NonNull ConfigSection currentSection) {
		for(var section : list) {
			var cooldownDuration = section.values().cooldown().duration();
			var nextSection = currentSection.createSection(section.name());
			
			if(!cooldownDuration.isZero()) {
				var cooldowns = section.data().getLastExecutionTimes();
				List<Map<String, Object>> serializedList = new ArrayList<>();
				
				for(var entry : cooldowns.entrySet()) {
					var owner = entry.getKey();
					var executionTime = entry.getValue();
					
					if(!(executionTime instanceof CooldownlessExecutionTime) && executionTime.isExpired())
						continue;
					
					var wrapped = executionTime instanceof CooldownlessExecutionTime
					  ? executionTime
					  : ExecutionTime.continuing(section.values().cooldown(), executionTime.remaining());
					
					Map<String, Object> map = new HashMap<>(owner.serialize());
					//noinspection OverrideOnly why even
					map.put("cooldown", wrapped.serialize());
					
					serializedList.add(map);
				}
				
				nextSection.set("__cooldowns__", serializedList);
			}
			
			saveCooldowns(section.children(), nextSection);
		}
	}
	
	public static void loadCooldowns() {
		for(var command : CustomCommandRegistry.getRegisteredCommands()) {
			var section = cooldownStorage.getSection(command.name());
			
			if(section != null)
				loadCooldowns(command, section);
		}
	}
	
	private static void loadCooldowns(CommandSectionLike current, ConfigSection currentSection) {
		for(var child : current.children()) {
			var nextSection = currentSection.getSection(child.name());
			
			if(nextSection != null)
				loadCooldowns(child, nextSection);
		}
		
		var cooldowns = currentSection.getMapList("__cooldowns__");
		
		if(cooldowns.isEmpty())
			return;
		
		for(var map : cooldowns) {
			var owner = CooldownOwner.deserialize((Map<String, Object>) map);
			var rawExecutionTimeMap = map.get("cooldown");
			
			if(!(rawExecutionTimeMap instanceof Map<?, ?> executionTimeMap))
				throw new IllegalArgumentException("'cooldown' is not a map");
			
			var executionTime = ExecutionTime.deserialize(current.values().cooldown(), (Map<String, Object>) executionTimeMap);
			
			current.data().setExecutionTime(owner, executionTime);
		}
	}
}
