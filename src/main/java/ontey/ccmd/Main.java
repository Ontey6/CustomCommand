package ontey.ccmd;

import ontey.api.plugin.OnteyPlugin;
import ontey.ccmd.updater.Updater;
import org.bstats.bukkit.Metrics;

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
		new Metrics(this, BSTATS_METRICS_ID);
	}
}
