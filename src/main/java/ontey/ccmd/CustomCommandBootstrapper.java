package ontey.ccmd;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import lombok.NonNull;
import ontey.ccmd.command.registry.CustomCommandRegistry;

public class CustomCommandBootstrapper implements PluginBootstrap {
	
	@Override
	public void bootstrap(@NonNull BootstrapContext context) {
		CustomCommandRegistry.registerCustomCommands(context);
	}
}
