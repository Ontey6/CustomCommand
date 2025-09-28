package com.ontey.updater;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ontey.Main;
import com.ontey.log.Log;
import org.bukkit.Bukkit;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class Updater {
   private static final String HANGAR_AUTHOR = "Ontey";
   private static final String HANGAR_PROJECT = "CustomCommand";
   
   public static void checkForUpdates() {
      CompletableFuture.runAsync(() -> {
         try {
            String latest = fetchHangar();
            
            String current = Main.version;
            if(latest != null && !latest.equalsIgnoreCase(current)) {
               Bukkit.getScheduler().runTask(Main.instance, () ->
                 Log.info("[Updater] New version available: " + latest +
                   " (current: " + current + ")"));
            }
         } catch (Exception e) {
            Log.warning("[Updater] Could not check for updates: " + e.getMessage());
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
}