package ontey.ccmd.updater;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import ontey.api.loader.AutoRegistered;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

import static ontey.ccmd.Main.plugin;

@AutoRegistered
public class Updater implements Listener {
	
	private static final String HANGAR_AUTHOR = "Ontey";
	
	private static final String HANGAR_PROJECT = "CustomCommand";
	
	@Getter
	private static volatile String latest = null;
	
	public static void checkForUpdates() {
		CompletableFuture.runAsync(() -> {
			try {
				String latest = fetchHangar();
				
				String current = plugin.getMeta().getVersion();
				if(latest != null && !isUpToDate(current, latest)) {
					Updater.latest = latest;
					plugin.getSLF4JLogger().warn("An update is available: {}", latest);
				}
			} catch(Exception e) {
				plugin.getSLF4JLogger().error("[Updater] Could not check for updates: {}", e.getMessage());
				plugin.getFileLog().saveStackTrace(e);
			}
		});
	}
	
	private static String fetchHangar() throws Exception {
		String url = "https://hangar.papermc.io/api/v1/projects/" + HANGAR_AUTHOR + "/" + HANGAR_PROJECT + "/versions";
		HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
		conn.setRequestProperty("User-Agent", "Ontey/CustomCommand Updater");
		try(InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
			JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
			return root.getAsJsonArray("result")
			  .get(0).getAsJsonObject()
			  .get("name").getAsString();
		}
	}
	
	private static boolean isUpToDate(String current, String latest) {
		if(current.equalsIgnoreCase(latest))
			return true;
		
		try {
			float curr = Float.parseFloat(current);
			float lat = Float.parseFloat(latest);
			return curr >= lat;
		} catch(NumberFormatException exc) {
			return false;
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if(!event.getPlayer().isOp() && latest != null)
			return;
		
		event.getPlayer().sendMessage(Component.text("[CustomCommand] An update is available: " + latest, NamedTextColor.YELLOW));
	}
}
