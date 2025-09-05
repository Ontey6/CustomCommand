package com.ontey.holder;

import com.ontey.execution.Execution;
import com.ontey.files.Config;
import com.ontey.reload.Reload;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ActionHolders {
   
   private static String ah(String str) {
      return Config.ah(str);
   }
   
   public static String apply(CommandSender sender, String str) {
      if(str.startsWith(ah("msg"))) {
         str = str.substring(ah("msg").length());
         sender.sendMessage(Execution.formatMessage(str, sender));
         return "";
      }
      if(str.startsWith(ah("broadcast"))) {
         str = str.substring(ah("broadcast").length());
         for(Player player : Bukkit.getOnlinePlayers())
            player.sendMessage(Execution.formatMessage(str, sender));
         return "";
      }
      if(str.contains(ah("reload"))) {
         str = str.replaceFirst(ah("reload"), "");
         Reload.reload(sender);
      }
      
      return str;
   }
}
