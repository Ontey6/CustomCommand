package com.ontey.reload;

import com.ontey.CustomCommand;
import com.ontey.files.Commands;
import com.ontey.files.Config;
import org.bukkit.command.CommandSender;

public class Reload {
   public static void reload(CommandSender sender) {
      reloadConfig(sender);
      reloadCommands(sender);
   }
   
   public static void reloadConfig(CommandSender sender) {
      try {
         Config.config.load(Config.file);
         Config.loadConstants();
      } catch(Exception e) {
         sender.sendMessage("§Couldn't reload the config. Using old configuration");
         return;
      }
      sender.sendMessage("§aReloaded the config");
   }
   
   public static void reloadCommands(CommandSender sender) {
      for(CustomCommand cmd : Commands.registeredCommands)
         cmd.loadMutable(true);
      sender.sendMessage("§aReloaded the Commands");
   }
}
