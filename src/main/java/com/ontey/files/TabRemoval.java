package com.ontey.files;

import static com.ontey.Main.instance;
import com.ontey.log.Log;
import org.bukkit.configuration.file.YamlConfiguration;
import java.util.List;
import java.io.File;

// Absolutely Not copied from Config

public class TabRemoval {
   
   public static File file;
   
   public static YamlConfiguration config;
   
   private TabRemoval() { }
   
   public static void load() {
      file = new File(instance.getDataFolder(), "tab.yml");
      
      if(!file.exists())
         instance.saveResource("tab.yml", false);
      
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
   
   public enum TabRemovalType { WHITELIST, BLACKLIST }
}
