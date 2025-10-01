package com.ontey.execution;

import com.ontey.CustomCommand;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

import static com.ontey.execution.ConditionParser.*;
import static com.ontey.execution.Formattation.replaceMM;

public class Execution {
   
   public CustomCommand cmd;
   
   public CommandSender sender;
   public String label;
   public String[] args;
   
   public Execution(CustomCommand cmd, CommandSender sender, String label, String[] args) {
      this.cmd = cmd;
      
      this.sender = sender;
      this.label = label;
      this.args = args;
   }
   
   public void sendMessages() {
      sendMessages(cmd.messages, sender);
   }
   
   public void runCommands() {
      for(String cmd : cmd.commands) {
         //ActionHolders.apply(sender, cmd, label, args, this.cmd.storage);
         if(cmd != null && !cmd.isEmpty())
            Bukkit.dispatchCommand(sender, cmd);
      }
   }
   
   public void sendBroadcasts() {
      for(Player player : Bukkit.getOnlinePlayers())
         sendMessages(cmd.broadcasts, player);
   }
   
   public void sendAdvancedBroadcast() {
      var abc = cmd.advancedBroadcast;
      if(abc == null)
         return;
      new Formattation(this).formatMessages(abc.broadcast);
      List<String> permission = abc.permission;
      List<String> condition = abc.condition;
      double range = abc.range(this);
      Location senderLoc = (sender instanceof Player p) ? p.getLocation() : null;
      
      for(Player player : Bukkit.getOnlinePlayers()) {
         if(abc.includeConsole)
            sendMessages(abc.broadcast, Bukkit.getConsoleSender());
         if(!abc.includeSender && player.getName().equals(sender.getName()))
            continue;
         if(permission != null && !hasPermissions(player, permission))
            continue;
         if(!evalConditions(condition, this))
            continue;
         if(range != -1 && senderLoc != null)
            if(!player.getWorld().equals(senderLoc.getWorld()) || player.getLocation().distance(senderLoc) > range)
               continue;
         sendMessages(abc.broadcast, sender);
      }
   }
   
   public static void sendMessages(List<String> messages, CommandSender sender) {
      for(String msg : messages)
         sender.sendMessage(replaceMM(msg));
   }
   
   // Helpers
   
   private boolean hasPermissions(Player player, List<String> permissions) {
      for(String perm : permissions)
         if(!player.hasPermission(perm))
            return false;
      return true;
   }
}
