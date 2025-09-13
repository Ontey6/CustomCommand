package com.ontey;

import com.ontey.commands.MainCommand;
import com.ontey.files.Commands;
import com.ontey.files.Config;
import com.ontey.files.TabRemoval;
import com.ontey.listeners.TabRemovalListener;
import com.ontey.log.Log;
import com.ontey.updater.Updater;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
   
   public static Main instance;
   
   public static boolean papi = false;
   
   public static String version = "0.3";
   
   @Override
   public void onEnable() {
      instance = this;
      load();
      Updater.checkForUpdates();
   }
   
   private void load() {
      loadFiles();
      loadPluginCommand();
      Startup.loadCommands();
      loadListeners();
      TabRemoval.load();
      
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
   
   private void loadListeners() {
      getServer().getPluginManager().registerEvents(new TabRemovalListener(), this);
   }
   
   public static void disablePlugin() {
      Log.info("Disabling plugin");
      instance.getServer().getPluginManager().disablePlugin(instance);
   }
}