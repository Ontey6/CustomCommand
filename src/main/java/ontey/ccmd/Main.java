package ontey.ccmd;

import ontey.api.plugin.OnteyPlugin;
import ontey.ccmd.command.registry.CustomCommandRegistry;
import ontey.ccmd.updater.Updater;
import org.bstats.bukkit.Metrics;
import org.bstats.charts.SimplePie;

public final class Main extends OnteyPlugin {
	
	public static int BSTATS_METRICS_ID = 33295;
	
	public static Main plugin;
	
	public Main() {
		plugin = this;
	}
	
	public static boolean isPlaceholderApiEnabled() {
		return getPluginManager().isPluginEnabled("PlaceholderAPI");
	}
	
	public static boolean isMiniPlaceholdersEnabled() {
		return getPluginManager().isPluginEnabled("MiniPlaceholders");
	}
	
	@Override
	public void onEnable() {
		load();
		
		Updater.checkForUpdates();
		registerMetrics();
	}
	
	private void registerMetrics() {
		Metrics metrics = new Metrics(this, BSTATS_METRICS_ID);
		
		// Optional: Add custom charts
		metrics.addCustomChart(
		  new SimplePie("registered_command_count", () -> {
			  int size = CustomCommandRegistry.getRegisteredCommands().size();
			  int value = size - size % 5;
			  
			  if(value > 50)
				  return value + "+";
			  
			  return value + "-" + (value + 5);
		  })
		);
	}
}
