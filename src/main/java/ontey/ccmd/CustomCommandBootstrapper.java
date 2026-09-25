package ontey.ccmd;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import lombok.NonNull;
import ontey.api.filelog.FileLog;
import ontey.ccmd.command.component.CommandComponents;
import ontey.ccmd.command.execution.Executions;
import ontey.ccmd.command.registry.CustomCommandRegistry;
import ontey.ccmd.command.requirement.Requirements;
import ontey.ccmd.command.suggestion.Suggestions;

import static ontey.ccmd.shared.SharedConstants.*;

public class CustomCommandBootstrapper implements PluginBootstrap {
	
	@Override
	public void bootstrap(@NonNull BootstrapContext context) {
		logger = context.getLogger();
		pluginMeta = context.getPluginMeta();
		dataDirectory = context.getDataDirectory();
		pluginsDirectory = context.getPluginSource();
		fileLog = new FileLog(logger, dataDirectory.toFile());
		
		CommandComponents.registerDefaultArgumentTypes();
		Executions.registerDefaultExecutions();
		Requirements.registerDefaultRequirements();
		Suggestions.registerDefaultSuggestions();
		
		CustomCommandRegistry.registerCustomCommands(context.getLifecycleManager(), false); //TODO add config toggle
	}
}
