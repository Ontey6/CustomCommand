package ontey.ccmd.command;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@AllArgsConstructor
public class CommandSection {
	
	protected CommandSection(@NonNull String name, @NonNull ArgumentBuilder<CommandSourceStack, ?> argumentBuilder, @NonNull List<@NonNull CommandSection> children) {
		if(!(this instanceof CustomCommand cmd))
			throw new IllegalArgumentException("Root CommandSections must implement CustomCommand");
		
		this.name = name;
		this.argumentBuilder = argumentBuilder;
		this.children = children;
		this.root = cmd;
		this.parent = null;
	}
	
	@NonNull
	@Getter
	private final String name;
	
	@NonNull
	@Getter
	private final ArgumentBuilder<CommandSourceStack, ?> argumentBuilder;
	
	@NonNull
	@Getter
	private final CustomCommand root;
	
	@Nullable
	@Getter
	private final CommandSection parent;
	
	@NonNull
	@Getter
	private final List<@NonNull CommandSection> children;
	
	@NonNull
	@Contract(pure = true, value = "_, _ -> new")
	public CommandSection createChild(String name, @NonNull ArgumentBuilder<CommandSourceStack, ?> child) {
		return new CommandSection(name, child, root, this, new ArrayList<>());
	}
	
	@NonNull
	public CommandSection addChild(@NonNull CommandSection child) {
		children.add(child);
		argumentBuilder.then(child.argumentBuilder);
		return this;
	}
	
	@NonNull
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append("name: '").append(name).append("'");
		if(!children.isEmpty()) {
			sb.append(", children: [");
			for(var child : children)
				sb.append(child);
			sb.append("]");
		}
		sb.append("}");
		
		return sb.toString();
	}
}
