package ontey.ccmd.plugincommand;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import ontey.api.command.Command;
import ontey.api.command.argument.Arg;
import ontey.api.loader.AutoRegistered;
import ontey.ccmd.command.CommandSectionLike;
import ontey.ccmd.command.CustomCommand;
import ontey.ccmd.command.registry.CustomCommandRegistry;
import ontey.ccmd.updater.Updater;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

import static ontey.ccmd.Main.plugin;

@AutoRegistered
public class CustomCommandCommand extends Command {
	
	public CustomCommandCommand() {
		super("custom-command");
		
		permission = "ccmd.command.custom-command";
		description = "The main command of the CustomCommand plugin by Ontey";
		aliases.add("ccmd");
		
		root
		  .executes(rootExecution())
		  .then(update()
			 .then(updateForce()))
		  .then(commands()
		    .then(commandsWith()))
		  .then(command())
		  .then(help())
		  .then(reload());
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
	
	private com.mojang.brigadier.Command<CommandSourceStack> rootExecution() {
		return ctx -> {
			var sender = ctx.getSource().getSender();
			
			sender.sendPlainMessage("Running " + plugin.toString());
			
			return SUCCESS;
		};
	}
	
	private LiteralArgumentBuilder<CommandSourceStack> update() {
		return Arg.literal("update")
		  .executes(ctx -> {
			  var sender = ctx.getSource().getSender();
			  var latest = Updater.getLatest();
			  
			  if(latest == null)
				  throw Arg.simpleException("[CustomCommand] Already on the latest version!");
			  
			  sender.sendMessage(Component.text("[CustomCommand] Click here to download the update (Or run /ccmd update force)", NamedTextColor.YELLOW).clickEvent(ClickEvent.runCommand("ccmd update force")));
			  
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
		  .executes(commandsCommand(List.of()));
	}
	
	private com.mojang.brigadier.Command<CommandSourceStack> commandsCommand(List<String> selected) {
		return ctx -> {
			var sender = ctx.getSource().getSender();
			
			for(var command : CustomCommandRegistry.getRegisteredCustomCommands()) {
				var values = command.values();
				var description = values.description();
				var permission = values.permission();
				var consoleOnly = values.consoleOnly();
				var enabled = values.enabled();
				var aliases = values.aliases();
				
				sender.sendMessage(Component.text(command.name(), enabled ? NamedTextColor.GREEN : NamedTextColor.RED));
				sendConditional(sender, selected, "description", description, "A short description of what the command does");
				sendConditional(sender, selected, "permission", permission, "The permission players need to run the command");
				sendConditional(sender, selected, "aliases", aliases, "Commands that do the same things as this command with a different name");
				sendConditional(sender, selected, "console_only", consoleOnly, "Whether the command can only be run as the console");
				sendConditional(sender, selected, "children", command.children().stream().map(CommandSectionLike::name).toList(), "The children of the command");
			}
			
			return SUCCESS;
		};
	}
	
	private LiteralArgumentBuilder<CommandSourceStack> commandsWith() {
		var base = Arg.literal("with");
		
		var list = List.of("description", "permission", "console_only", "aliases", "children");
		
		addWithNodes(base, list, new ArrayList<>());
		
		return base;
	}
	
	private void addWithNodes(LiteralArgumentBuilder<CommandSourceStack> parent, List<String> available, List<String> selected) {
		for(String feature : available) {
			var childNode = Arg.literal(feature);
			
			List<String> newSelected = new ArrayList<>(selected);
			newSelected.add(feature);
			
			childNode.executes(commandsCommand(newSelected));
			
			List<String> newAvailable = new ArrayList<>(available);
			newAvailable.remove(feature);
			
			if(!newAvailable.isEmpty())
				addWithNodes(childNode, newAvailable, newSelected);
			
			parent.then(childNode);
		}
	}
	
	private LiteralArgumentBuilder<CommandSourceStack> command() {
		var base = Arg.literal("command")
		  .executes(_ -> {
			  throw Arg.simpleException("No command or command section specified");
		  });
	
		addCommandsNodes(base, new ArrayList<>(CustomCommandRegistry.getRegisteredCustomCommands()));
		
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
		  .executes(ctx -> {
			  var sender = ctx.getSource().getSender();
			  
			  sender.sendPlainMessage("Reloading...");
			  
			  CustomCommandRegistry.reloadCommands();
			  sender.sendPlainMessage("Finished reloading! Look at the console to see if there are any errors");
			  
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
			
			sender.sendMessage(Component.text(section.name(), enabled ? NamedTextColor.GREEN : NamedTextColor.RED));
			sender.sendMessage(keyValue("description", description, "A short description of what the command does"));
			sender.sendMessage(keyValue("permission", permission, "The permission players need to run the command"));
			sender.sendMessage(keyValue("aliases", aliases, "Commands that do the same things as this command with a different name"));
			sender.sendMessage(keyValue("console_only", consoleOnly, "Whether the command can only be run as the console"));
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
		  .executes(_ -> {
			  throw Arg.simpleException("No command or command section specified");
		  });
		
		addHelpNodes(base, new ArrayList<>(CustomCommandRegistry.getRegisteredCustomCommands()));
		
		return base;
	}
}
