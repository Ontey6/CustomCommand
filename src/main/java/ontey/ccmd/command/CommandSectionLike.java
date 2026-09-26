package ontey.ccmd.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.NonNull;
import net.minecraft.network.chat.Component;
import ontey.api.command.argument.Arg;
import ontey.ccmd.command.component.ArgumentCommandComponent;
import ontey.ccmd.command.component.CommandComponent;
import ontey.ccmd.command.config.CustomCommandConfig;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.command.data.CommandData;
import ontey.ccmd.command.execution.Execution;
import ontey.ccmd.cooldown.message.CooldownMessage;
import ontey.ccmd.cooldown.owner.CooldownOwner;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.craftbukkit.command.VanillaCommandWrapper;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.List;
import java.util.function.Predicate;

public interface CommandSectionLike {
	
	/// The special uses of the values:
	/// - `name` is the name of the command or section.
	/// - `aliases` are commands that execute the same things as the command or section
	/// - `description` is a description of the command or section
	/// - `permission` is the permission needed to run the command or section
	/// - `consoleOnly` determines whether the command or section can only be run by a console
	/// - `options` are ignored and always empty.
	/// - `enabled` determines if this command or section is loaded and parsed
	///
	/// @return The values of this command
	
	@NonNull
	CustomCommandConfig values();
	
	/// @return The name of this command or argument
	
	@NonNull
	@Contract(pure = true)
	default String name() {
		return values().name();
	}
	
	/// @return An unmodifiable list of this section or command's children
	
	@NonNull
	@Unmodifiable
	List<CommandSection> children();
	
	/// @return This section or command's [CommandComponent]
	
	@NonNull
	CommandComponent component();
	
	/// @return Gets the mutable data associated with this command section
	
	@NonNull
	@Contract(pure = true)
	default CommandData data() {
		return CommandData.getOrCreateData(this);
	}
	
	/// Builds this command section into a [CommandNode] - including the children - and returns it.
	
	default @NonNull CommandNode<CommandSourceStack> build(@NonNull ParseContext context) {
		var execution = component().execution();
		var requirement = component().requirement();
		
		Command<CommandSourceStack> command = createCommand(context, execution);
		
		Predicate<CommandSourceStack> brigRequirement = requirement == null
		  ? null
		  : requirement.parseRequirement(context.withSection("requires"));
		
		var permission = values().permission();
		Predicate<CommandSourceStack> check =
		  source -> (permission == null || source.getSender().hasPermission(permission))
			 && (!values().consoleOnly() || source.getSender() instanceof ConsoleCommandSender);
		
		var finalRequirement = brigRequirement == null
		  ? check
		  : check.and(brigRequirement);
		
		CommandNode<CommandSourceStack> base = createBase(context, command, finalRequirement);
		
		for(var child : children()) {
			var childContext = context.withArgumentName(child.name());
			base.addChild(child.build(childContext));
		}
		
		return base;
	}
	
	default @Nullable Command<CommandSourceStack> createCommand(@NonNull ParseContext context, Execution execution) {
		Command<CommandSourceStack> command;
		
		if(execution == null)
			command = null;
		else
			command = ctx -> {
				try {
					checkAndSetCooldown(ctx.getSource());
					
					return execution.parseExecution(context.withSection("executes")).run(ctx);
				} catch(CommandSyntaxException e) { //TODO find out why command blocks' "last output" doesn't work when the plugin is on the server
					if(ctx.getSource().getSender() instanceof BlockCommandSender blockSender) {
						var source = VanillaCommandWrapper.getListener(blockSender);
						
						source.sendFailure(Component.literal(e.getMessage()));
					}
					
					throw e;
				}
			};
		return command;
	}
	
	private @NonNull CommandNode<CommandSourceStack> createBase(@NonNull ParseContext context, @Nullable Command<CommandSourceStack> command, @Nullable Predicate<CommandSourceStack> finalRequirement) {
		if(this instanceof CustomCommand cmd)
			return new CustomCommandNode(cmd, name(), command, finalRequirement);
		else if(component() instanceof ArgumentCommandComponent argumentComponent) {
			var suggestions = argumentComponent.suggestions();
			SuggestionProvider<CommandSourceStack> brigSuggestions = suggestions == null ? null : suggestions.parseSuggestions(context.withSection("suggests"));
			
			return new ArgumentCommandNode<>(name(), argumentComponent.argumentType(), command, finalRequirement, null, null, false, brigSuggestions);
		} else {
			return new LiteralCommandNode<>(name(), command, finalRequirement, null, null, false);
		}
	}
	
	private void checkAndSetCooldown(@NonNull CommandSourceStack source) throws CommandSyntaxException {
		var owner = switch(source.getExecutor()) {
			case Player player -> CooldownOwner.player(player);
			case null -> switch(source.getSender()) {
				case BlockCommandSender blockSender -> CooldownOwner.block(blockSender.getBlock());
				case ConsoleCommandSender _ -> CooldownOwner.console();
				default -> null;
			};
			default -> null;
		};
		
		if(owner == null)
			return;
		
		var lastExecutionTime = data().getLastExecutionTime(owner);
		var cooldown = values().cooldown();
		var message = cooldown == null ? CooldownMessage.defaultMessage() : cooldown.message();
		
		if(lastExecutionTime != null && !lastExecutionTime.isExpired())
			throw Arg.simpleException(message.format(lastExecutionTime.formatRemaining()));
		
		data().setLastExecutionTime(owner);
	}
}
