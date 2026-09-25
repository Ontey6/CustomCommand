package ontey.ccmd.cooldown;

import lombok.NonNull;
import ontey.ccmd.command.CommandSectionLike;
import ontey.ccmd.cooldown.message.CooldownMessage;

import java.time.Duration;

/// A [CommandSectionLike]'s stored cooldown
///
/// @param duration The duration this cooldown applies for
/// @param rawDuration The string representation used for serialization
/// @param message The message displayed when the command is run but on cooldown

public record Cooldown(@NonNull Duration duration, @NonNull String rawDuration, @NonNull CooldownMessage message) {

}
