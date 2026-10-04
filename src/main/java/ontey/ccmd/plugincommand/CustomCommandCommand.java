package ontey.ccmd.plugincommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import ontey.api.command.Command;
import ontey.api.command.argument.Arg;
import ontey.api.loader.AutoRegistered;
import ontey.ccmd.command.CommandSectionLike;
import ontey.ccmd.command.CustomCommand;
import ontey.ccmd.command.data.CommandData;
import ontey.ccmd.command.registry.CustomCommandRegistry;
import ontey.ccmd.cooldown.execution.CooldownlessExecutionTime;
import ontey.ccmd.cooldown.execution.ExecutionTime;
import ontey.ccmd.cooldown.owner.CooldownOwner;
import ontey.ccmd.updater.Updater;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static ontey.ccmd.Main.plugin;

@AutoRegistered
public class CustomCommandCommand extends Command {
	
	private final String PERMISSION = "ccmd.command.custom-command";
	
	private final List<String> subPermissions = List.of(
	  "update",
	  "commands",
	  "command",
	  "help",
	  "reload",
	  "cooldowns",
	  "cooldownless"
	);
	
	public CustomCommandCommand() {
		super("custom-command");
		
		//permission = "ccmd.command.custom-command";
		description = "The main command of the CustomCommand plugin by Ontey";
		aliases.add("ccmd");
		
		root
		  .requires(source -> source.getSender().hasPermission("ccmd.command.custom-command") || subPermissions.stream().anyMatch(node -> hasNodePermission(source, node)))
		  .executes(versionExecution())
		  .then(update()
			 .then(updateForce()))
		  .then(commands()
			 .then(commandsWith()))
		  .then(command())
		  .then(help())
		  .then(reload())
		  .then(version())
		  .then(cooldowns())
		  .then(cooldownless())
		;
	}
	
	private static void sendConditional(CommandSender sender, List<String> selected, String key, Object value, String description) {
		if(selected.isEmpty() || selected.contains(key))
			sender.sendMessage(keyValue(key, value, description));
	}
	
	private static Component keyValue(String key, Object value, String description) {
		return Component
		  .text("> ")
		  .append(Component.text(key)
			 .hoverEvent(HoverEvent.showText(Component.text(description))))
		  .append(Component.text(": "))
		  .append(Component.text(String.valueOf(value), NamedTextColor.YELLOW));
	}
	
	private boolean hasNodePermission(CommandSourceStack source, String identifier) {
		return source.getSender().hasPermission(PERMISSION + "." + identifier);
	}
	
	private com.mojang.brigadier.Command<CommandSourceStack> versionExecution() {
		return ctx -> {
			var sender = ctx.getSource().getSender();
			
			sender.sendPlainMessage("Running " + plugin.toString());
			
			return SUCCESS;
		};
	}
	
	private LiteralArgumentBuilder<CommandSourceStack> version() {
		return Arg.literal("version")
		  .executes(versionExecution());
	}
	
	private LiteralArgumentBuilder<CommandSourceStack> update() {
		return Arg.literal("update")
		  .requires(source -> hasNodePermission(source, "update"))
		  .executes(ctx -> {
			  var sender = ctx.getSource().getSender();
			  var latest = Updater.getLatest();
			  
			  if(latest == null)
				  throw Arg.simpleException("[CustomCommand] Already on the latest version!");
			  
			  sender.sendMessage(latest.getUpdaterMessage());
			  sender.sendMessage(Component.text("[CustomCommand] ")
				 .append(Component
					.text("Click to download (/ccmd update force)", NamedTextColor.YELLOW)
					.clickEvent(ClickEvent.runCommand("ccmd update force"))));
			  
			  return SUCCESS;
		  });
	}
	
	private LiteralArgumentBuilder<CommandSourceStack> updateForce() {
		return Arg.literal("force")
		  .executes(ctx -> {
			  var sender = ctx.getSource().getSender();
			  var latest = Updater.getLatest();
			  
			  if(latest == null)
				  throw Arg.simpleException("[CustomCommand] Already on the latest version!");
			  
			  latest.createDownloadUpdateFuture().complete(sender);
			  
			  return SUCCESS;
		  });
	}
	
	private LiteralArgumentBuilder<CommandSourceStack> commands() {
		return Arg.literal("commands")
		  .requires(source -> hasNodePermission(source, "commands"))
		  .executes(commandsCommand());
	}
	
	private com.mojang.brigadier.Command<CommandSourceStack> commandsCommand() {
		return ctx -> {
			var sender = ctx.getSource().getSender();
			
			var input = ctx.getInput();
			input = input.substring(input.indexOf(' ') + 1);
			input = input.substring("commands".length());
			
			if(!input.isEmpty())
				input = input.substring(1);
			
			var selected = List.of(input.split(" "));
			
			for(var command : CustomCommandRegistry.getRegisteredCommands()) {
				var values = command.values();
				var description = values.description();
				var permission = values.permission();
				var consoleOnly = values.consoleOnly();
				var enabled = values.enabled();
				var aliases = values.aliases();
				var cooldown = values.cooldown();
				var cooldownDuration = cooldown.rawDuration();
				var cooldownMessage = cooldown.message().serialize();
				
				sender.sendMessage(Component.text(command.name(), enabled ? NamedTextColor.GREEN : NamedTextColor.RED));
				sendConditional(sender, selected, "description", description, "A short description of what the command does");
				sendConditional(sender, selected, "permission", permission, "The permission players need to run the command");
				sendConditional(sender, selected, "aliases", aliases, "Commands that do the same things as this command with a different name");
				sendConditional(sender, selected, "console_only", consoleOnly, "Whether the command can only be run as the console");
				sendConditional(sender, selected, "cooldown", cooldownDuration, cooldownMessage);
				sendConditional(sender, selected, "children", command.children().stream().map(CommandSectionLike::name).toList(), "The children of the command");
			}
			
			return SUCCESS;
		};
	}
	
	private SuggestionProvider<CommandSourceStack> commandsSuggestions() {
		var list = List.of("description", "permission", "console_only", "aliases", "cooldown", "children");
		
		return (_, builder) -> {
			var inputtedSelection = builder.getRemaining().split(" ");
			List<String> suggestions = new ArrayList<>(list);
			
			for(var item : inputtedSelection)
				suggestions.remove(item);
			
			for(var suggestion : suggestions) {
				var newLength = inputtedSelection.length - 1;
				
				if(list.contains(inputtedSelection[newLength]))
					newLength += 1;
				
				var array = new String[newLength];
				System.arraycopy(inputtedSelection, 0, array, 0, newLength);
				var before = String.join(" ", array);
				
				builder.suggest(before.isEmpty() ? suggestion : before + " " + suggestion);
			}
			
			return builder.buildFuture();
		};
	}
	
	private LiteralArgumentBuilder<CommandSourceStack> commandsWith() {
		//addWithNodes(base, list, new ArrayList<>());
		
		return Arg.literal("with")
		  .then(Arg.varargs("selection")
			 .executes(commandsCommand())
			 .suggests(commandsSuggestions()));
	}
	
	private LiteralArgumentBuilder<CommandSourceStack> command() {
		var base = Arg.literal("command")
		  .requires(source -> hasNodePermission(source, "command"))
		  .executes(_ -> {
			  throw Arg.simpleException("No command or command section specified");
		  });
		
		addCommandsNodes(base, new ArrayList<>(CustomCommandRegistry.getRegisteredCommands()));
		
		return base;
	}
	
	private void addCommandsNodes(LiteralArgumentBuilder<CommandSourceStack> base, List<? extends CommandSectionLike> list) {
		for(var section : list) {
			var child = Arg.literal(section.name())
			  .executes(commandCommand(section));
			
			if(!section.children().isEmpty())
				addCommandsNodes(child, section.children());
			
			base.then(child);
		}
	}
	
	private LiteralArgumentBuilder<CommandSourceStack> reload() {
		return Arg.literal("reload")
		  .requires(source -> hasNodePermission(source, "reload"))
		  .executes(ctx -> {
			  var sender = ctx.getSource().getSender();
			  
			  sender.sendPlainMessage("Reloading...");
			  
			  CustomCommandRegistry.reloadCommands();
			  sender.sendPlainMessage("Finished reloading! Check console for errors");
			  
			  return SUCCESS;
		  });
	}
	
	private com.mojang.brigadier.Command<CommandSourceStack> commandCommand(CommandSectionLike section) {
		return ctx -> {
			var sender = ctx.getSource().getSender();
			var values = section.values();
			var description = values.description();
			var permission = values.permission();
			var consoleOnly = values.consoleOnly();
			var enabled = values.enabled();
			var aliases = values.aliases();
			var cooldown = values.cooldown();
			var cooldownDuration = cooldown.rawDuration();
			var cooldownMessage = cooldown.message().serialize();
			
			sender.sendMessage(Component.text(section.name(), enabled ? NamedTextColor.GREEN : NamedTextColor.RED));
			sender.sendMessage(keyValue("description", description, "A short description of what the command does"));
			sender.sendMessage(keyValue("permission", permission, "The permission players need to run the command"));
			sender.sendMessage(keyValue("aliases", aliases, "Commands that do the same things as this command with a different name"));
			sender.sendMessage(keyValue("console_only", consoleOnly, "Whether the command can only be run as the console"));
			sender.sendMessage(keyValue("cooldown", cooldownDuration, cooldownMessage));
			sender.sendMessage(keyValue("children", section.children().stream().map(CommandSectionLike::name).toList(), "The children of the command (section)"));
			
			return SUCCESS;
		};
	}
	
	private void addHelpNodes(LiteralArgumentBuilder<CommandSourceStack> base, List<? extends CommandSectionLike> list) {
		for(var section : list) {
			if(!section.values().enabled())
				continue;
			
			var child = Arg.literal(section.name())
			  .executes(ctx -> {
				  var sender = ctx.getSource().getSender();
				  var description = section.values().description();
				  
				  if(description != null)
					  sender.sendMessage(Component.text(section.name()).append(Component.text(": ")).append(Component.text(description, NamedTextColor.YELLOW)));
				  else
					  sender.sendMessage(Component.text("This " + (section instanceof CustomCommand ? "command" : "command section") + " doesn't specify a description", NamedTextColor.RED));
				  
				  return SUCCESS;
			  });
			
			if(!section.children().isEmpty())
				addHelpNodes(child, section.children());
			
			base.then(child);
		}
	}
	
	private LiteralArgumentBuilder<CommandSourceStack> help() {
		var base = Arg.literal("help")
		  .requires(source -> hasNodePermission(source, "help"))
		  .executes(_ -> {
			  throw Arg.simpleException("No command or command section specified");
		  });
		
		addHelpNodes(base, new ArrayList<>(CustomCommandRegistry.getRegisteredCommands()));
		
		return base;
	}
	
	private LiteralArgumentBuilder<CommandSourceStack> cooldowns() {
		return Arg.literal("cooldowns")
		  .requires(source -> hasNodePermission(source, "cooldowns"))
		  .executes(ctx -> {
			  var sender = ctx.getSource().getSender();
			  
			  sender.sendMessage(Component.text("Cooldowns", NamedTextColor.YELLOW));
			  
			  for(var entry : CommandData.getData().entrySet()) {
				  var name = entry.getKey().name();
				  var lastExecutionTimes = entry.getValue().getLastExecutionTimes();
				  
				  if(lastExecutionTimes.isEmpty())
					  continue;
				  
				  sender.sendMessage(
					 Component
						.text("• ", NamedTextColor.GRAY)
						.append(Component.text(name, NamedTextColor.WHITE))
				  );
				  
				  for(var entry0 : lastExecutionTimes.entrySet()) {
					  var owner = entry0.getKey();
					  var lastExecutionTime = entry0.getValue();
					  
					  sender.sendMessage(Component.text("  ◦ ", NamedTextColor.GRAY)
						 .append(Component.text(owner.displayName(), NamedTextColor.YELLOW))
						 .append(Component.text(": ", NamedTextColor.WHITE))
						 .append(Component.text(lastExecutionTime.formatRemaining(), NamedTextColor.YELLOW)));
				  }
			  }
			  
			  return SUCCESS;
		  });
	}
	
	private LiteralArgumentBuilder<CommandSourceStack> cooldownless() {
		return Arg.literal("cooldownless")
		  .requires(source -> hasNodePermission(source, "cooldownless"))
		  .then(
			 Arg.literal("run")
				.redirect(CustomCommandRegistry.getRootNode().toLiteral(), ctx -> {
					var sender = ctx.getSource().getSender();
					var command = cutCooldownlessPrefix(ctx.getInput());
					var section = findSection(command);
					var owner = CooldownOwner.of(sender);
					
					if(owner == null)
						throw Arg.simpleException("Can't recognize sender class '" + sender.getClass().getName() + "'");
					
					var oldExecutionTime = section.data().getLastExecutionTime(owner);
					Supplier<ExecutionTime> convertSupplier = oldExecutionTime == null ? () -> ExecutionTime.basic(section.values().cooldown()) : () -> oldExecutionTime;
					var executionTime = CooldownlessExecutionTime.temporary(convertSupplier);
					
					section.data().setExecutionTime(owner, executionTime);
					
					return ctx.getSource();
				})
		  );
	}
	
	private String cutCooldownlessPrefix(String input) {
		var index = input.indexOf(' ');
		input = input.substring(index + 1);
		
		return input.substring("cooldownless run ".length());
	}
	
	private CommandSectionLike findSection(String input) throws CommandSyntaxException {
		return findSection(input.split(" "), 0, CustomCommandRegistry.getRegisteredCommands(), null);
	}
	
	private CommandSectionLike findSection(@NonNull String @NonNull [] args, int index, @NonNull List<? extends CommandSectionLike> selection, @Nullable CommandSectionLike parent) throws CommandSyntaxException {
		if(index >= args.length) {
			if(parent == null)
				throw Arg.simpleException("Couldn't find command");
			
			return parent;
		}
		
		var commandName = args[index];
		CommandSectionLike section = null;
		
		for(var command : selection) {
			if(command.name().equals(commandName))
				section = command;
			
			findSection(args, ++index, command.children(), command);
		}
		
		if(section == null)
			throw Arg.simpleException("Couldn't find command");
		
		return section;
	}
	
	//private LiteralArgumentBuilder<CommandSourceStack> cooldown() {
	//	return Arg.literal("cooldown");
	//}
	//
	//private LiteralArgumentBuilder<CommandSourceStack> cooldownSet() {
	//	return Arg.literal("set");
	//}
	//
	//private LiteralArgumentBuilder<CommandSourceStack> cooldownSetPlayer() {
	//	return Arg.literal("player")
	//	  .then(Arg.playerProfilesArg("player"));
	//}
	//
	//private LiteralArgumentBuilder<CommandSourceStack> cooldownSetConsole() {
	//	return Arg.literal("console");
	//}
	//
	//private LiteralArgumentBuilder<CommandSourceStack> cooldownSetBlock() {
	//	return Arg.literal("block")
	//	  .then(
	//		 Arg.blockLocationArg("location")
	//		   .then(
	//		     Arg.worldArg("world")
	//		   )
	//	  );
	//}
	//
	//private LiteralArgumentBuilder<CommandSourceStack> cooldownTimes() {
	//	return Arg.literal("convert")
	//	  .then(cooldownTimeBasic())
	//	  .then(cooldownTimeContinuing())
	//	  .then(cooldownTimeDisabledPermanent())
	//	  .then(cooldownTimeDisabledTemporary()); //TODO infinite recursion
	//}
	//
	//private LiteralCommandNode<CommandSourceStack> cooldownTimeDisabledTemporary() {
	//
	//	var node = Arg.literal("disabled_temporary").build();
	//	var redirectNode = Arg.literal("convert")
	//	  .then(cooldownTimeBasic())
	//	  .then(cooldownTimeContinuing())
	//	  .then(cooldownTimeDisabledPermanent())
	//	  .then(node)
	//	  .build();
	//
	//
	//
	//	return node;
	//}
	//
	//private LiteralArgumentBuilder<CommandSourceStack> cooldownTimeDisabledPermanent() {
	//	return Arg.literal("disabled_permanent");
	//}
	//
	//private LiteralArgumentBuilder<CommandSourceStack> cooldownTimeBasic() {
	//	return Arg.literal("basic")
	//	  .then(Arg.longArg("time-millis", -1L));
	//}
	//
	//private LiteralArgumentBuilder<CommandSourceStack> cooldownTimeContinuing() {
	//	return Arg.literal("continuing")
	//	  .then(Arg.stringArg("remaining"));
	//}
}
