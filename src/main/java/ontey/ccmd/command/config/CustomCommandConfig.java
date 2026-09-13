package ontey.ccmd.command.config;

import lombok.NonNull;
import ontey.api.config.ConfigSection;
import ontey.api.serialization.CombinedConfigSerializable;
import ontey.ccmd.command.CustomCommand;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.util.DurationUtil;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A command configuration for {@link CustomCommand}s.
 */

public record CustomCommandConfig(
  @NonNull String name,
  @NonNull List<@NonNull String> aliases,
  @Nullable String description,
  @Nullable String permission,
  boolean consoleOnly,
  @Nullable String rawCooldown,
  @Nullable Duration cooldown,
  boolean enabled
) implements CombinedConfigSerializable {
	
	/**
	 * Deserializes the given {@link ConfigSection} into a {@link CustomCommandConfig}
	 *
	 * @return A new {@link CustomCommandConfig} based on the values of the given {@link ConfigSection}.
	 */
	
	public static @NonNull CustomCommandConfig deserialize(@NonNull ParseContext context, @NonNull ConfigSection section, int prefixLength) throws IllegalStateException {
		String name = section.getName().substring(prefixLength);
		List<String> aliases = section.getStringList("aliases");
		String description = section.getString("description");
		String permission = section.getString("permission");
		boolean consoleOnly = section.getBoolean("console-only");
		var rawCooldown = section.getString("cooldown");
		var cooldown = rawCooldown == null ? null : DurationUtil.parseDuration(rawCooldown, "cooldown", context);
		boolean enabled = section.getBoolean("enabled", true);
		
		return new CustomCommandConfig(name, aliases, description, permission, consoleOnly, rawCooldown, cooldown, enabled);
	}
	
	public @NonNull Map<@NonNull String, @Nullable Object> serialize() {
		Map<String, Object> out = new HashMap<>(7);
		
		out.put("name", name);
		
		if(!aliases.isEmpty())
			out.put("aliases", aliases);
		
		if(permission != null)
			out.put("permission", permission);
		
		if(description != null)
			out.put("description", description);
		
		if(consoleOnly)
			out.put("console-only", true);
		
		if(rawCooldown != null)
			out.put("cooldown", rawCooldown);
		
		if(!enabled)
			out.put("enabled", false);
		
		return out;
	}
}
