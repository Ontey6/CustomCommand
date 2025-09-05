package com.ontey.listeners;

import com.ontey.files.Config;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

public class TabRemove implements Listener {
   
   @EventHandler
   public void onTabComplete(PlayerCommandSendEvent event) {
      if(Config.REMOVE_NAMESPACED_PLUGIN_COMMANDS)
         event.getCommands().removeIf(cmd -> cmd.startsWith(Config.DEFAULT_NAMESPACE + ":"));
      if(Config.REMOVE_NAMESPACED_COMMANDS)
         event.getCommands().removeIf(cmd -> cmd.contains(":"));
   }
}
