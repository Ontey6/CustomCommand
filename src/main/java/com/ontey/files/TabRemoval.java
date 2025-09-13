package com.ontey.files;

import com.ontey.Main;
import com.ontey.holder.Paths;
import com.ontey.log.Log;
import org.bukkit.configuration.file.YamlConfiguration;
import java.util.List;
import java.io.File;

// Not copied from Config

public class TabRemoval {
   
   public static File file;
   
   public static YamlConfiguration config;
   
   private TabRemoval() { }
   
   public static void load() {
      Main main = Main.instance;
      file = new File(main.getDataFolder(), "tab.yml");
      
      if(!file.exists())
         main.saveResource("tab.yml", false);
      
      config = new YamlConfiguration();
      config.options().parseComments(true);
      
      try {
         config.load(file);
      } catch(Exception e) {
         Log.error(
           "+-+-+-+-+-+-+-+-+-+-+-+-CCMD-+-+-+-+-+-+-+-+-+-+-+-+-+",
           "  Couldn't load the tab file.",
           "  Look at the stack-trace below, so you can identify the error.",
           "  There is probably a syntax error in the yml.",
           "  Fix the error, then restart the server and it will work again.",
           "+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+"
         );
         //noinspection CallToPrintStackTrace
         e.printStackTrace();
         Main.disablePlugin();
         return;
      }
      Paths.load();
   }
   
   public static void save() {
      try {
         config.save(file);
      } catch(Exception e) {
         Log.error(
           "+-+-+-+-+-+-+-+-+-+-+-+-CCMD-+-+-+-+-+-+-+-+-+-+-+-+-+",
           "  Couldn't save the tab file.",
           "  If the file doesn't exist anymore, restart the server",
           "+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+"
         );
         //noinspection CallToPrintStackTrace
         e.printStackTrace();
      }
   }
   
   public static List<String> getTab() {
      return Commands.getField(config, "tab");
   }
   
   public static TabRemovalType getType() {
      String type = config.getString("type", "blacklist");
      
      return
        type.equalsIgnoreCase("whitelist")
        ? TabRemovalType.WHITELIST
        : TabRemovalType.BLACKLIST;
   }
   
   public enum TabRemovalType {WHITELIST, BLACKLIST}
}
