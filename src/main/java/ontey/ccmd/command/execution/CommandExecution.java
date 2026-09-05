package ontey.ccmd.command.execution;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.NonNull;
import me.clip.placeholderapi.PlaceholderAPI;
import ontey.api.command.argument.Arg;
import ontey.api.command.registry.CommandRegistry;
import ontey.ccmd.Main;
import ontey.ccmd.command.context.ParseContext;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.command.VanillaCommandWrapper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;

import static ontey.api.command.Command.SUCCESS;

public record CommandExecution(@NonNull List<@NonNull String> commands, boolean runAsConsole,
                               boolean broadcast) implements Execution {
	
	public CommandExecution {
		if(runAsConsole && broadcast)
			throw new IllegalArgumentException("Can't have both runAsConsole and broadcast on a command execution");
	}
	
	@Override
	public @NonNull Command<CommandSourceStack> parseExecution(@NonNull ParseContext context) {
		return ctx -> {
			if(runAsConsole) {
				var sender = Bukkit.getConsoleSender();
				runCommands(sender, VanillaCommandWrapper.getListener(sender));
			} else if(broadcast) {
				for(var player : Bukkit.getOnlinePlayers())
					runCommands(player, VanillaCommandWrapper.getListener(player));
			} else {
				var executor = ctx.getSource().getExecutor();
				CommandSender sender = switch(executor) {
					case Player player -> player;
					case null -> Bukkit.getConsoleSender();
					default -> throw Arg.simpleException("The specified executor can not run commands!");
				};
				
				runCommands(sender, VanillaCommandWrapper.getListener(sender));
			}
			//TODO Add own placeholders to commands
			//TODO add own actions to commands when prefixing with '@' like '@javascript player.sendPlainMessage("Hello, World!")'
			
			//TODO custom return values. Try to intercept minecraft's /return command (best solution). If not possible, add '@return <int>' action and possibility in javascript action.
			return SUCCESS;
		};
	}
	
	private void runCommands(CommandSender sender, CommandSourceStack source) throws CommandSyntaxException {
		for(String command : commands) {
			if(Main.isPlaceholderApiEnabled() && sender instanceof Player player)
				command = PlaceholderAPI.setPlaceholders(player, command);
			
			CommandRegistry.getCommandDispatcher().execute(command, source);
		}
	}
	
	@Override
	public @NonNull String path() {
		return "commands";
	}
	
	@Override
	public @NotNull Map<String, Object> serialize() {
		return Map.of(
		  "type", "COMMANDS",
		  "command", Map.of(
			 "commands", commands,
			 "run-as-console", runAsConsole,
			 "broadcast", broadcast
		  )
		);
	}
}
