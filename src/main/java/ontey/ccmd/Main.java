package ontey.ccmd;

import ontey.api.plugin.OnteyPlugin;
import ontey.ccmd.cooldown.CooldownManager;
import ontey.ccmd.updater.Updater;
import org.bstats.bukkit.Metrics;

import java.io.IOException;

import static ontey.ccmd.shared.SharedConstants.cooldownStorage;
import static ontey.ccmd.shared.SharedConstants.fileLog;

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
	public void onLoad() {
		try {
			cooldownStorage.load();
		} catch(IOException e) {
			fileLog.saveStackTrace(e);
			throw new IllegalStateException("Couldn't load cooldowns.yml", e);
		}
		
		CooldownManager.loadCooldowns();
	}
	
	@Override
	public void onEnable() {
		load();
		
		Updater.checkForUpdates();
		new Metrics(this, BSTATS_METRICS_ID);
	}
	
	@Override
	public void onDisable() {
		CooldownManager.saveCooldowns();
	}
}
