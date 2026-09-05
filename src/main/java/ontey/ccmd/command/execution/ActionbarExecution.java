package ontey.ccmd.command.execution;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.NonNull;
import ontey.api.command.argument.Arg;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.format.Formatter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static ontey.api.command.Command.SUCCESS;

public record ActionbarExecution(@NonNull String actionbar, boolean broadcast) implements Execution {
	
	@Override
	public @NonNull Command<CommandSourceStack> parseExecution(@NonNull ParseContext context) {
		return ctx -> {
			if(broadcast) {
				for(var player : Bukkit.getOnlinePlayers())
					showActionBar(player);
			} else {
				if(!(ctx.getSource().getExecutor() instanceof Player player))
					throw Arg.simpleException("Can't show actionbar to non-player entity or console");
				
				showActionBar(player);
			}
			
			return SUCCESS;
		};
	}
	
	private void showActionBar(Player player) {
		var actionbar = Formatter.format(this.actionbar, player);
		
		player.sendActionBar(actionbar);
	}
	
	@Override
	public @NonNull String path() {
		return "actionbar";
	}
	
	@Override
	public @NotNull Map<String, Object> serialize() {
		return Map.of(
		  "type", "ACTIONBAR",
		  "actionbar", Map.of(
			 "actionbar", actionbar,
			 "broadcast", broadcast
		  )
		);
	}
}
