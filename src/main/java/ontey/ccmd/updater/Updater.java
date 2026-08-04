package ontey.ccmd.updater;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import ontey.ccmd.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class Updater implements Listener {
	private static final String HANGAR_AUTHOR = "Ontey";
	private static final String HANGAR_PROJECT = "CustomCommand";
	
	public static String LATEST = null;
	
	public static void checkForUpdates(CommandSender sender) {
		CompletableFuture.runAsync(() -> {
			try {
				String latest = fetchHangar();
				
				String current = Main.plugin.getMeta().getVersion();
				if(latest != null && !isUpToDate(current, latest)) {
					LATEST = latest;
					sender.sendMessage(""/*Config.LANGUAGE.getConsoleUpdaterMessage()*/); //TODO
				}
			} catch (Exception e) {
				sender.sendMessage("[Updater] Could not check for updates: " + e.getMessage());
			}
		});
	}
	
	private static String fetchHangar() throws Exception {
		String url = "https://hangar.papermc.io/api/v1/projects/" + HANGAR_AUTHOR + "/" + HANGAR_PROJECT + "/versions";
		HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
		conn.setRequestProperty("User-Agent", "Updater");
		try (InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
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
		//if(!event.getPlayer().isOp() || LATEST == null || !Config.UPDATER)
		//	return;
		//event.getPlayer().sendMessage(Config.LANGUAGE.getJoinUpdaterMessage());
	}
}
