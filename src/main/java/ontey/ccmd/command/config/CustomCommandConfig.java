package ontey.ccmd.command.config;

import lombok.NonNull;
import ontey.api.config.ConfigSection;
import ontey.api.serialization.CombinedConfigSerializable;
import ontey.ccmd.command.CommandSectionLike;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.cooldown.Cooldown;
import ontey.ccmd.cooldown.message.CooldownMessage;
import ontey.ccmd.util.DurationUtil;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/// A command configuration for {@link CommandSectionLike}s.
///
/// @param name The name of the command (section)
/// @param aliases The aliases, which are alternate names, that can be used instead of the name
/// @param description An optional description, that briefly explains what this command does
/// @param permission An optional permission needed to run the command (section)
/// @param consoleOnly Whether the command (section) can only be run by the console
/// @param cooldown An optional cooldown that makes the command not runnable for the executor for a certain duration after executing it
/// @param enabled Whether this command (section) is enabled and should be parsed and registered

public record CustomCommandConfig(
  @NonNull String name,
  @NonNull List<@NonNull String> aliases,
  @Nullable String description,
  @Nullable String permission,
  boolean consoleOnly,
  @NonNull Cooldown cooldown,
  boolean enabled
) implements CombinedConfigSerializable {
	
	/// Deserializes the given [ConfigSection] into a [CustomCommandConfig]
	///
	/// @return A new [CustomCommandConfig] based on the values of the given [ConfigSection].
	
	public static @NonNull CustomCommandConfig deserialize(@NonNull ParseContext context, @NonNull ConfigSection section, int prefixLength) throws IllegalStateException {
		String name = section.getName().substring(prefixLength);
		List<String> aliases = section.getStringList("aliases");
		String description = section.getString("description");
		String permission = section.getString("permission");
		boolean consoleOnly = section.getBoolean("console-only");
		var rawCooldownDuration = section.getString("cooldown");
		var cooldownDuration = rawCooldownDuration == null ? null : DurationUtil.parseDuration(rawCooldownDuration, "cooldown", context);
		var rawCooldownMessage = section.getString("cooldown-message");
		var cooldownMessage = rawCooldownMessage == null ? CooldownMessage.defaultMessage() : CooldownMessage.deserialize(rawCooldownMessage);
		var cooldown = rawCooldownDuration == null ? Cooldown.ZERO : new Cooldown(cooldownDuration, rawCooldownDuration, cooldownMessage);
		boolean enabled = section.getBoolean("enabled", true);
		
		return new CustomCommandConfig(name, aliases, description, permission, consoleOnly, cooldown, enabled);
	}
	
	public @NonNull Map<@NonNull String, @Nullable Object> serialize() {
		Map<String, Object> out = new HashMap<>(8);
		
		out.put("name", name);
		
		if(!aliases.isEmpty())
			out.put("aliases", aliases);
		
		if(permission != null)
			out.put("permission", permission);
		
		if(description != null)
			out.put("description", description);
		
		if(consoleOnly)
			out.put("console-only", true);
		
		if(cooldown != null) {
			out.put("cooldown", cooldown.rawDuration());
			out.put("cooldown-message", cooldown.message().serialize());
		}
		
		if(!enabled)
			out.put("enabled", false);
		
		return out;
	}
}
