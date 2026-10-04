package ontey.ccmd.command;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;

public class RootCustomCommandNode extends RootCommandNode<CommandSourceStack> {
	
	public LiteralCommandNode<CommandSourceStack> toLiteral() {
		LiteralCommandNode<CommandSourceStack> literal = new LiteralCommandNode<>("", null, _ -> true, null, null, false);
		
		for(var child : getChildren())
			literal.addChild(child);
		
		return literal;
	}
}
