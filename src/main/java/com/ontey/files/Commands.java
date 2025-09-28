package com.ontey.files;

import com.ontey.Main;
import com.ontey.log.Log;

import java.io.File;

public class Commands {
   
   private Commands() { }
   
   public static void load() {
      File dir = new File(Main.instance.getDataFolder(), "commands");
      
      if (!dir.exists()) {
         if (!dir.mkdirs()) {
            Log.info("Couldn't create the commands directory. Disabling plugin");
            Main.disablePlugin();
            return;
         }
         loadExamples();
      }
   }
   
   private static void loadExamples() {
      File examples = new File(Main.instance.getDataFolder(), "commands/examples.yml");
      
      if (examples.exists())
         return;
      
      Main.instance.saveResource("examples.yml", false);
      File rootFile = new File(Main.instance.getDataFolder(), "examples.yml");
      
      if (rootFile.exists() && !rootFile.renameTo(examples))
         Log.info("Couldn't copy the examples file");
   }
   
   // moved to CustomCommand
}
