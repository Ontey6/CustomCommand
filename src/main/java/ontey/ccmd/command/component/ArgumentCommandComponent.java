package ontey.ccmd.command.component;

import com.mojang.brigadier.arguments.ArgumentType;
import lombok.Builder;
import lombok.NonNull;
import ontey.ccmd.command.execution.Execution;
import ontey.ccmd.command.requirement.Requirement;
import ontey.ccmd.command.suggestion.Suggestion;
import org.jetbrains.annotations.Nullable;

@Builder
public record ArgumentCommandComponent(
  @NonNull String name,
  @Nullable Execution execution,
  @Nullable Requirement requirement,
  @Nullable Suggestion suggestions,
  @NonNull ArgumentType<?> argumentType
) implements CommandComponent {
	
}
