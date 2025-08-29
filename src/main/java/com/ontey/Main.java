package com.ontey;

import com.ontey.commands.MainCommand;
import com.ontey.files.Commands;
import com.ontey.files.Config;
import com.ontey.listeners.TabRemove;
import com.ontey.log.Log;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
   
   public static Main instance;
   
   public static boolean papi = false;
   
   public static String version = "0.1";
   
   @Override
   public void onEnable() {
      instance = this;
      load();
   }
   
   private void load() {
      loadFiles();
      loadPluginCommand();
      Startup.loadCommands();
      loadListeners();
      
      if(Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI"))
         papi = true;
   }
   
   private void loadPluginCommand() {
      //noinspection DataFlowIssue
      getCommand("customcommands").setExecutor(new MainCommand());
   }
   
   private void loadFiles() {
      Config.load();
      Commands.load();
   }
   
   private void loadListeners() {
      getServer().getPluginManager().registerEvents(new TabRemove(), this);
   }
   
   public static void disablePlugin() {
      Log.info("Disabling plugin");
      Bukkit.getPluginManager().disablePlugin(instance);
   }
}