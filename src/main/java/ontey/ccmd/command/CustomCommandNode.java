package ontey.ccmd.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.APICommandMeta;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.Getter;
import ontey.ccmd.shared.SharedConstants;

import java.util.function.Predicate;

public class CustomCommandNode extends LiteralCommandNode<CommandSourceStack> {
	
	@Getter
	private final CustomCommand customCommand;
	
	public CustomCommandNode(CustomCommand customCommand, Command<CommandSourceStack> command, Predicate<CommandSourceStack> requirement) {
		super(customCommand.name(), command, requirement, null, null, false);
		this.customCommand = customCommand;
		apiCommandMeta = new APICommandMeta(SharedConstants.pluginMeta, customCommand.values().description(), customCommand.values().aliases(), "CustomCommand");
	}
}
