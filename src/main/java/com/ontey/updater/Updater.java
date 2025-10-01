package com.ontey.updater;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ontey.Main;
import com.ontey.execution.Formattation;
import com.ontey.files.Config;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class Updater {
   private static final String HANGAR_AUTHOR = "Ontey";
   private static final String HANGAR_PROJECT = "CustomCommand";
   
   public static String LATEST = null;
   
   public static void checkForUpdates(CommandSender sender) {
      CompletableFuture.runAsync(() -> {
         try {
            String latest = fetchHangar();
            
            String current = Main.version;
            if(latest != null && !isUpToDate(current, latest)) {
               LATEST = latest;
               sendUpdaterMessage(sender);
            }
         } catch (Exception e) {
            sender.sendMessage("[Updater] Could not check for updates: " + e.getMessage());
         }
      });
   }
   
   public static void sendUpdaterMessage(CommandSender sender) {
      if(sender instanceof final Player p) {
         p.sendMessage(Config.PREFIX + " New version available: " + LATEST + " (current: " + Main.version + ")");
         p.sendMessage(Formattation.replaceMM(
           "Download: &6<<uurl:'https://www.spigotmc.org/resources/custom-command.128478'>spigot</uurl>> "
             + "&b<<uurl:https://dev.bukkit.org/projects/customcommand>bukkit</uurl>> "
             + "&a<<uurl:https://modrinth.com/plugin/ccmd/version/" + LATEST + ">modrinth</uurl>> "
             + "&e<<uurl:https://hangar.papermc.io/Ontey/CustomCommand/versions/" + LATEST + ">paper</uurl>>"
         ));
         return;
      }
      Bukkit.getScheduler().runTask(Main.instance, () ->
        sender.sendMessage("[Updater] New version available: " + LATEST + " (current: " + Main.version + ")")
      );
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
}