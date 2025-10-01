package com.ontey;

import com.ontey.commands.MainCommand;
import com.ontey.files.Commands;
import com.ontey.files.Config;
import com.ontey.log.Log;
import com.ontey.updater.UpdateMessageSender;
import com.ontey.updater.Updater;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
   
   public static Main instance;
   
   public static boolean papi = false;
   
   public static String version = "0.5.1";
   
   public static MiniMessage mm;
   
   public static PluginManager pm;
   
   @Override
   public void onEnable() {
      instance = this;
      mm = MiniMessage.miniMessage();
      pm = getServer().getPluginManager();
      load();
      if(Config.UPDATER)
         Updater.checkForUpdates(Bukkit.getConsoleSender());
   }
   
   private void load() {
      loadFiles();
      loadPluginCommand();
      Startup.loadCommands();
      pm.registerEvents(new UpdateMessageSender(), this);
      
      if(getServer().getPluginManager().isPluginEnabled("PlaceholderAPI"))
         papi = true;
   }
   
   @Deprecated
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