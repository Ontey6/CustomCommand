package ontey.ccmd.command.execution;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.NonNull;
import ontey.api.command.argument.Arg;
import ontey.ccmd.command.context.ParseContext;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static ontey.api.command.Command.SUCCESS;
import static ontey.ccmd.format.Formatter.format;

public record MessageExecution(@NonNull String message, boolean broadcast) implements Execution {
	
	@Override
	public @NonNull Command<CommandSourceStack> parseExecution(@NonNull ParseContext context) {
		return ctx -> {
			if(broadcast) {
				for(var player : Bukkit.getOnlinePlayers())
					player.sendMessage(format(message, player));
			} else {
				CommandSender sender;
				var executor = ctx.getSource().getExecutor();
				
				if(executor instanceof Player player)
					sender = player;
				else if(executor == null)
					sender = Bukkit.getConsoleSender();
				else
					throw Arg.simpleException("Can't send message to non-player entity");
				
				sender.sendMessage(format(message, sender));
			}
			
			return SUCCESS;
		};
	}
	
	@Override
	public @NotNull Map<String, Object> serialize() {
		return Map.of(
		  "type", "message",
		  "message", Map.of(
			 "message", message,
			 "broadcast", broadcast
		  )
		);
	}
}
