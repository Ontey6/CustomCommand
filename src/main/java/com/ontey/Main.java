package com.ontey;

import com.ontey.commands.MainCommand;
import com.ontey.files.Commands;
import com.ontey.files.Config;
import com.ontey.log.Log;
import com.ontey.updater.Updater;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
   
   public static Main instance;
   
   public static boolean papi = false;
   
   public static String version = "0.5";
   
   public static MiniMessage mm;
   
   @Override
   public void onEnable() {
      instance = this;
      mm = MiniMessage.miniMessage();
      load();
      if(Config.UPDATER)
         Updater.checkForUpdates();
   }
   
   private void load() {
      loadFiles();
      loadPluginCommand();
      Startup.loadCommands();
      
      if(getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"))
         papi = true;
   }
   
   private void loadPluginCommand() {
      // noinspection DataFlowIssue
      getCommand("customcommands").setExecutor(new MainCommand());
   }
   
   private void loadFiles() {
      Config.load();
      Commands.load();
   }
   
   public static void disablePlugin() {
      Log.info("Disabling plugin");
      instance.getServer().getPluginManager().disablePlugin(instance);
   }
}