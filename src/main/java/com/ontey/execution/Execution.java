package com.ontey.execution;

import com.ontey.Main;
import com.ontey.files.Config;
import com.ontey.holder.ActionHolders;
import com.ontey.holder.Placeholders;
import com.ontey.log.Log;
import com.ontey.types.AdvancedBroadcast;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

import static com.ontey.execution.Evaluation.*;

public class Execution {
   
   public static void sendMessages(List<String> messages, CommandSender sender, String[] args) {
      if(messages.isEmpty())
         return;
      for(String msg : messages)
         if(!msg.isEmpty())
            sender.sendMessage(formatMessage(replaceArgs(msg, args), sender));
   }
   
   public static void runCommands(List<String> commands, CommandSender sender, String[] args) {
      resolveConditions(sender, args, commands);
      if(commands.isEmpty())
         return;
      for(String cmd : commands) {
         String formatted = formatCommand(sender, replaceArgs(cmd, args));
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
               player.sendMessage(formatMessage(replaceArgs(bc, args), sender));
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
            player.sendMessage(formatMessage(msg, sender));
      }
   }
   
   // === Helpers ===
   
   static boolean hasPermissions(Player player, List<String> permissions) {
      for(String perm : permissions)
         if(!player.hasPermission(perm))
            return false;
      return true;
   }
   
   static List<String> resolveConditions(CommandSender sender, String[] args, List<String> commands) {
      if(commands.isEmpty())
         return commands;
      
      for(int i = 0; i < commands.size(); i++) {
         String line = commands.get(i);
         
         if(line.startsWith(Config.ah("condition"))) {
            int start = i;
            boolean allTrue = true;
            
            while(i < commands.size() && commands.get(i).startsWith(Config.ah("condition"))) {
               String condLine = commands.get(i).substring(Config.ah("condition").length());
               boolean result = evalCondition(condLine, sender, args);
               
               commands.remove(i);
               if(!result)
                  allTrue = false;
            }
            
            if(!allTrue && i < commands.size())
               commands.remove(i);
            
            i = start - 1;
            continue;
         }
         
         if(line.startsWith("\\" + Config.ah("condition")))
            commands.set(i, line.substring(1));
      }
      return commands;
   }
   
   static String replacePlaceholders(CommandSender sender, @NotNull String str) {
      str = ActionHolders.apply(sender, str);
      str = replacePAPI(sender, str);
      return Placeholders.apply(sender, str);
   }
   
    public static String replaceArgs(@NotNull String str, String[] args) {
      List<String> list = Arrays.asList(args);
      
      str = str.replace(Config.ph("args-length"), str(args.length));
      
      for (int i = 1; i <= args.length; i++)
         str = replaceArg(str, list, i, args);
      
      return str;
   }
   
   static String replaceArg(String str, List<String> list, int i, String[] args) {
      str = str
        .replace(Config.ph("arg" + i), args[i - 1])
        .replace(Config.ph("arg" + i + ".."), join(list, i, args.length))
        .replace(Config.ph("arg.." + i), join(list, 1, i));
      
      for(int j = i; j <= args.length; j++)
         str = str.replace(Config.ph("arg" + i + ".." + j), join(list, i, j));
      
      return str;
   }
   
   static String join(List<String> list, int start, int end) {
      return String.join(" ", list.subList(start - 1, end));
   }
   
   public static String formatMessage(String message, CommandSender sender) {
      if(message == null)
         return "";
      
      String str =
        sender instanceof ConsoleCommandSender && Config.REMOVE_COLORS_IN_CONSOLE
          ? removeColorCodes(message)
          : translateColorCodes(message);
      
      if(str.startsWith(Config.ph("no replace")))
         str = str.substring(Config.ph("no replace").length());
      
      str = replacePlaceholders(sender, str);
      return str;
   }
   
   static String translateColorCodes(String str) {
      if(str == null)
         return null;
      return str
        .replaceAll("(?<!&)&([0-9a-fk-or])", "§$1")
        .replaceAll("&&([0-9a-fk-or])", "&$1");
   }
   
   static String removeColorCodes(String str) {
      return str
        .replaceAll("(?<!&)&([0-9a-fk-or])", "")
        .replaceAll("&&([0-9a-fk-or])", "&$1");
   }
   
   static String formatCommand(CommandSender sender, String str) {
      if(str == null)
         return "";
      if(str.startsWith(Config.ph("no replace")))
         return str.substring(Config.ph("no replace").length());
      str = replacePlaceholders(sender, str);
      return translateColorCodes(str);
   }
   
   static String replacePAPI(CommandSender sender, String str) {
      if(Main.papi && sender instanceof final Player player)
         return PlaceholderAPI.setPlaceholders(player, str);
      return str;
   }
}
