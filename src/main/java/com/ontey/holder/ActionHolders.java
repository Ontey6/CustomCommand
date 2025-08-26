package com.ontey.holder;

import com.ontey.files.Config;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ActionHolders {
   
   private static String ah(String str) {
      return Config.ah(str);
   }
   
   // TODO
   public static String apply(CommandSender sender, String str) {
      if(str.startsWith(ah("msg"))) {
         str = str.substring(ah("msg").length());
         sender.sendMessage(str);
         return str;
      }
      if(str.startsWith(ah("broadcast"))) {
         str = str.substring(ah("broadcast").length());
         for(Player player : Bukkit.getOnlinePlayers())
            player.sendMessage(str);
         return str;
      }
      
      return str;
   }
}
