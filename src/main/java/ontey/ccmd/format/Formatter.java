package ontey.ccmd.format;

import io.github.miniplaceholders.api.MiniPlaceholders;
import lombok.NonNull;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import ontey.ccmd.Main;

public final class Formatter {
	
	@NonNull
	public static Component format(@NonNull String input, @NonNull Audience audience) {
		Component component;
		
		if(Main.isMiniPlaceholdersEnabled())
			component = MiniMessage.miniMessage().deserialize(input, audience, MiniPlaceholders.globalPlaceholders(), MiniPlaceholders.audiencePlaceholders());
		else
			component = MiniMessage.miniMessage().deserialize(input);
		
		return component;
	}
	
	@NonNull
	public static Component format(@NonNull String input) {
		Component component;
		
		if(Main.isMiniPlaceholdersEnabled())
			component = MiniMessage.miniMessage().deserialize(input, MiniPlaceholders.globalPlaceholders());
		else
			component = MiniMessage.miniMessage().deserialize(input);
		
		return component;
	}
}
