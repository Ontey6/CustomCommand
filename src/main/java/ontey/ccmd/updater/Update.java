package ontey.ccmd.updater;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static ontey.ccmd.Main.plugin;

public record Update(@NonNull String version, @NonNull String description, @NonNull String fileName,
                     @NonNull String downloadURL, @NonNull List<@NonNull String> minecraftVersions,
                     String formattedMinecraftVersions) {
	
	/**
	 * Downloads this update from hangar asynchronously and puts it into the update folder so it will be installed on the next update.
	 */
	
	public CompletableFuture<CommandSender> createDownloadUpdateFuture() {
		CompletableFuture<CommandSender> future = new CompletableFuture<>();
		
		future.thenAcceptAsync(sender -> {
			try {
				var updateFolder = Bukkit.getUpdateFolderFile();
				
				if(!updateFolder.exists())
					updateFolder.mkdirs();
				
				var targetFile = new File(updateFolder, fileName);
				
				URL url = new URI(downloadURL).toURL();
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setRequestMethod("GET");
				
				try(InputStream in = connection.getInputStream();
				    FileOutputStream out = new FileOutputStream(targetFile)) {
					
					byte[] buffer = new byte[4096];
					int bytesRead;
					while((bytesRead = in.read(buffer)) != -1)
						out.write(buffer, 0, bytesRead);
				}
				
				plugin.getSLF4JLogger().info("Update downloaded! Restart the server to apply.");
				sendSyncMessage(sender, Component.text("Update downloaded! Restart server to apply.", NamedTextColor.GREEN));
			} catch(IOException | URISyntaxException e) {
				plugin.getSLF4JLogger().error("Failed to download the update: {}", e.getMessage());
				plugin.getFileLog().saveStackTrace(e);
				
				sendSyncMessage(sender, Component.text("Failed to download update " + version + ". Check console", NamedTextColor.RED));
			}
		});
		
		return future;
	}
	
	private void sendSyncMessage(CommandSender sender, Component message) {
		if(sender instanceof Entity entity)
			entity.getScheduler().run(plugin, _ -> entity.sendMessage(message), () -> plugin.getComponentLogger().warn(message));
		else
			plugin.getComponentLogger().warn(message);
	}
	
	public Component getUpdaterMessage() {
		var versionComponent = getVersion();
		if(minecraftVersions.contains(Bukkit.getMinecraftVersion()))
			return Component.text("[CustomCommand] An update is available: ").append(versionComponent);
		else
			return Component
			  .text("[CustomCommand] An update is available, but your it doesn't seem to support your current minecraft version: ").append(versionComponent).appendNewline()
			  .append(Component.text("The update supports version(s) " + formattedMinecraftVersions)).appendNewline();
	}
	
	public Component getVersion() {
		return Component.text(version)
		  .clickEvent(ClickEvent.suggestCommand("/ccmd update"))
		  .hoverEvent(HoverEvent.showText(Component.text("Supported minecraft version(s): " + formattedMinecraftVersions).appendNewline().append(Component.text("Description:\n" + description))));
	}
}
