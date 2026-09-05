package ontey.ccmd.command.execution;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.NonNull;
import net.kyori.adventure.bossbar.BossBar;
import ontey.api.command.argument.Arg;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.format.Formatter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static ontey.api.command.Command.SUCCESS;
import static ontey.ccmd.Main.plugin;

public record BossBarExecution(@NonNull String name, float progress, @NonNull BossBar.Color color,
                               @NonNull BossBar.Overlay overlay,
                               @NonNull Set<BossBar.@NonNull Flag> flags, long stayTicks,
                               boolean broadcast) implements Execution {
	
	private static final Map<BossBarExecution, Map<UUID, BossBar>> timedBossBars = new HashMap<>();
	
	@Override
	public @NonNull Command<CommandSourceStack> parseExecution(@NonNull ParseContext context) {
		return ctx -> {
			if(broadcast) {
				for(var player : Bukkit.getOnlinePlayers())
					showToPlayer(player);
			} else {
				if(!(ctx.getSource().getExecutor() instanceof Player player))
					throw Arg.simpleException("Can't show actionbar to non-player entity or console");
				
				showToPlayer(player);
			}
			
			return SUCCESS;
		};
	}
	
	private void showToPlayer(Player player) {
		if(timedBossBars.containsKey(this) && timedBossBars.get(this).containsKey(player.getUniqueId()))
			return;
		
		var name = Formatter.format(this.name, player);
		var bossBar = BossBar.bossBar(name, progress, color, overlay, flags);
		
		player.showBossBar(bossBar);
		timedBossBars.put(this, new HashMap<>(Map.of(player.getUniqueId(), bossBar)));
		
		if(stayTicks != -1L) {
			if(broadcast) {
				plugin.getSLF4JLogger().warn("You have a boss-bar execution that specifies stay-ticks (that are not -1) and is broadcast.");
				plugin.getSLF4JLogger().warn("For that, a task is started for EVERY PLAYER every time a player runs this command");
			}
			
			player.getScheduler().runDelayed(plugin, _ -> {
				player.hideBossBar(bossBar);
				
				if(timedBossBars.containsKey(this)) {
					var nestedMap = timedBossBars.get(this);
					
					nestedMap.remove(player.getUniqueId());
				}
			}, () -> {
			}, stayTicks);
		}
	}
	
	@Override
	public @NotNull Map<String, Object> serialize() {
		return Map.of(
		  "type", "boss_bar",
		  "boss-bar", Map.of(
			 "name", name,
			 "progress", progress,
			 "color", color.toString(),
			 "overlay", overlay.toString(),
			 "flags", flags.stream().map(Enum::toString).toList(),
			 "stay-ticks", stayTicks,
			 "broadcast", broadcast
		  )
		);
	}
}
