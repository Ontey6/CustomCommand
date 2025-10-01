package com.ontey.updater;

import com.ontey.files.Config;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import static com.ontey.updater.Updater.LATEST;

public class UpdateMessageSender implements Listener {
   
   @EventHandler
   public void onJoin(PlayerJoinEvent event) {
      if(!event.getPlayer().isOp() || LATEST == null || !Config.UPDATER)
         return;
      Updater.sendUpdaterMessage(event.getPlayer());
   }
}