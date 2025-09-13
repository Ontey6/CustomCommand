package com.ontey.execution;

import com.ontey.types.AdvancedBroadcast;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.List;

import static com.ontey.execution.Evaluation.*;
import static com.ontey.execution.Formation.*;
import static com.ontey.execution.Replacement.replaceArgs;

public class Execution {
   
   public static void sendMessages(List<String> messages, CommandSender sender, String[] args) {
      if(messages.isEmpty())
         return;
      for(String msg : messages)
         if(!msg.isEmpty())
            sender.sendMessage(formatMessage(replaceArgs(msg, args), sender, args));
   }
   
   public static void runCommands(List<String> commands, CommandSender sender, String[] args) {
      resolveConditions(sender, args, commands);
      if(commands.isEmpty())
         return;
      for(String cmd : commands) {
         String formatted = formatCommand(sender, replaceArgs(cmd, args), args);
         if(!formatted.isEmpty())
            Bukkit.dispatchCommand(sender, formatted);
      }
   }
   
   public static void sendBroadcasts(List<String> broadcasts, CommandSender sender, String[] args) {
      if(broadcasts.isEmpty())
         return;
      for(Player player : Bukkit.getOnlinePlayers())
         for(String bc : broadcasts)
            if(!bc.isEmpty())
               player.sendMessage(formatMessage(replaceArgs(bc, args), sender, args));
   }
   
   public static void sendAdvancedBroadcast(AdvancedBroadcast advancedBroadcast, CommandSender sender, String[] args) {
      if(advancedBroadcast == null)
         return;
      List<String> permission = advancedBroadcast.permission;
      List<String> condition = advancedBroadcast.condition;
      double range = advancedBroadcast.range(sender, args);
      Location senderLoc = (sender instanceof Player p) ? p.getLocation() : null;
      
      for (Player player : Bukkit.getOnlinePlayers()) {
         if (permission != null && !hasPermissions(player, permission))
            continue;
         if (!evalConditions(condition, sender, args))
            continue;
         if (range != -1 && senderLoc != null)
            if (!player.getWorld().equals(senderLoc.getWorld()) || player.getLocation().distance(senderLoc) > range)
               continue;
         for(String msg : advancedBroadcast.broadcast)
            player.sendMessage(formatMessage(msg, sender, args));
      }
   }
   
   // === Helpers ===
   
   static boolean hasPermissions(Player player, List<String> permissions) {
      for(String perm : permissions)
         if(!player.hasPermission(perm))
            return false;
      return true;
   }
}
