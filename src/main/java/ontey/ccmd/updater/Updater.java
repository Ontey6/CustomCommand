package ontey.ccmd.updater;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import ontey.api.loader.AutoRegistered;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

import static ontey.ccmd.Main.plugin;
import static ontey.ccmd.shared.SharedConstants.fileLog;
import static ontey.ccmd.shared.SharedConstants.logger;

@AutoRegistered
public class Updater implements Listener {
	
	private static final String HANGAR_AUTHOR = "Ontey";
	
	private static final String HANGAR_PROJECT = "CustomCommand";
	
	@Getter
	@Nullable
	private static volatile Update latest = null;
	
	public static void checkForUpdates() {
		CompletableFuture.runAsync(() -> {
			try {
				Update latest = fetchHangar();
				
				String current = plugin.getMeta().getVersion();
				if(!isUpToDate(current, latest.version())) {
					Updater.latest = latest;
					logger.warn("An update is available: {}", latest.version());
					logger.warn("Download it using '/ccmd update'");
				} else {
					var fiveMinutes = 5L * 60L * 20L;
					plugin.getScheduler().runTaskTimer(Updater::checkForUpdates, fiveMinutes, fiveMinutes);
				}
			} catch(Exception e) {
				logger.error("[Updater] Could not check for updates: {}", e.getMessage());
				fileLog.saveStackTrace(e);
			}
		});
	}
	
	private static Update fetchHangar() throws Exception {
		String url = "https://hangar.papermc.io/api/v1/projects/" + HANGAR_AUTHOR + "/" + HANGAR_PROJECT + "/versions";
		HttpURLConnection conn = (HttpURLConnection) URI.create(url).toURL().openConnection();
		conn.setRequestProperty("User-Agent", "Ontey/CustomCommand Updater");
		try(InputStreamReader reader = new InputStreamReader(conn.getInputStream())) {
			JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
			JsonObject latestVersion = root.getAsJsonArray("result").get(0).getAsJsonObject();
			JsonObject downloadsObject = latestVersion.get("downloads").getAsJsonObject().get("PAPER").getAsJsonObject();
			JsonArray formattedVersions = latestVersion.get("platformDependenciesFormatted").getAsJsonObject().get("PAPER").getAsJsonArray();
			String formattedVersionString = formattedVersions.size() == 1
			  ? formattedVersions.get(0).getAsString()
			  : formattedVersions.asList().stream().map(JsonElement::getAsString).toList().toString();
			
			return new Update(
			  latestVersion.get("name").getAsString(),
			  latestVersion.get("description").getAsString(),
			  downloadsObject.get("fileInfo").getAsJsonObject().get("name").getAsString(),
			  downloadsObject.get("downloadUrl").getAsString(),
			  latestVersion.get("platformDependencies").getAsJsonObject().get("PAPER").getAsJsonArray().asList().stream().map(JsonElement::getAsString).toList(),
			  formattedVersionString
			);
		}
	}
	
	private static boolean isUpToDate(String current, String latest) {
		if(current.equalsIgnoreCase(latest))
			return true;
		
		try {
			float curr = Float.parseFloat(current);
			float lat = Float.parseFloat(latest);
			return curr >= lat;
		} catch(NumberFormatException _) {
			return false;
		}
	}
	
	@EventHandler
	public void onJoin(PlayerJoinEvent event) {
		if(event.getPlayer().isOp() && latest != null)
			//noinspection DataFlowIssue
			event.getPlayer().sendMessage(latest.getUpdaterMessage());
	}
}
