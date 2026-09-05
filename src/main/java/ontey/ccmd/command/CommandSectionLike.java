package ontey.ccmd.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.ArgumentCommandNode;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.NonNull;
import ontey.api.command.config.CommandConfig;
import ontey.ccmd.command.component.ArgumentCommandComponent;
import ontey.ccmd.command.component.CommandComponent;
import ontey.ccmd.command.context.ParseContext;
import org.bukkit.command.ConsoleCommandSender;
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
	CommandConfig values();
	
	/// @return The name of this command or argument
	
	@NonNull
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
	
	/// Builds this command section into a [CommandNode] - including the children - and returns it.
	
	default @NonNull CommandNode<CommandSourceStack> build(@NonNull ParseContext context) {
		var execution = component().execution();
		var requirement = component().requirement();
		
		Command<CommandSourceStack> command = execution == null
		  ? null
		  : execution.parseExecution(context.withSection("executes"));
		
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
}
