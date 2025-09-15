package com.ontey.listeners;

import com.ontey.files.Config;
import com.ontey.files.TabRemoval;
import com.ontey.reload.Reload;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandSendEvent;

import java.util.Collection;

public class TabRemovalListener implements Listener {
   
   private Collection<String> commands;
   
   @EventHandler
   public void onTabComplete(PlayerCommandSendEvent event) {
      commands = event.getCommands();
      
      handleNamespacedRemoval();
      handleTabRemoval();
   }
   
   private void handleNamespacedRemoval() {
      if(Config.REMOVE_NAMESPACED_PLUGIN_COMMANDS)
         removeNamespacedPluginCommands();
      if(Config.REMOVE_NAMESPACED_COMMANDS)
         removeNamespacedCommands();
   }
   
   private void handleTabRemoval() {
      if(TabRemoval.getType() == TabRemoval.TabRemovalType.BLACKLIST)
         removeTabRemovalBlacklist();
      if(TabRemoval.getType() == TabRemoval.TabRemovalType.WHITELIST)
         setTabRemovalWhitelist();
   }
   
   // Helpers
   
   private void removeNamespacedPluginCommands() {
      commands.removeIf(cmd -> cmd.startsWith(Config.DEFAULT_NAMESPACE + ":"));
   }
   
   private void removeNamespacedCommands() {
      commands.removeIf(cmd -> cmd.contains(":"));
   }
   
   private void setTabRemovalWhitelist() {
      commands.clear();
      commands.addAll(TabRemoval.getTab());
   }
   
   private void removeTabRemovalBlacklist() {
      commands.removeAll(TabRemoval.getTab());
   }
}
