package com.ontey.execution;

import com.ontey.Main;
import com.ontey.files.Config;
import com.ontey.holder.ActionHolders;
import com.ontey.holder.Placeholders;
import com.ontey.types.AdvancedBroadcast;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
      String permission = advancedBroadcast.permission;
      List<String> condition = advancedBroadcast.condition;
      double range = advancedBroadcast.range;
      Location senderLoc = (sender instanceof Player p) ? p.getLocation() : null;
      
      for (Player player : Bukkit.getOnlinePlayers()) {
         if (permission != null && !player.hasPermission(permission))
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
   
   private static List<String> resolveConditions(CommandSender sender, String[] args, List<String> commands) {
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
   
   private static String replacePlaceholders(CommandSender sender, @NotNull String str) {
      str = Placeholders.apply(sender, str);
      str = replacePAPI(sender, str);
      return ActionHolders.apply(sender, str);
   }
   
   private static String replaceArgs(@NotNull String str, String[] args) {
      List<String> list = Arrays.asList(args);
      
      str = str.replace(Config.ph("args-length"), str(args.length));
      
      for (int i = 1; i <= args.length; i++)
         str = replaceArg(str, list, i, args);
      
      return str;
   }
   
   private static String replaceArg(String str, List<String> list, int i, String[] args) {
      str = str
        .replace(Config.ph("arg" + i), args[i - 1])
        .replace(Config.ph("arg" + i + ".."), join(list, i, args.length))
        .replace(Config.ph("arg.." + i), join(list, 1, i));
      
      for(int j = i; j <= args.length; j++)
         str = str.replace(Config.ph("arg" + i + ".." + j), join(list, i, j));
      
      return str;
   }
   
   private static String join(List<String> list, int start, int end) {
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
   
   private static String translateColorCodes(String str) {
      if(str == null)
         return null;
      return str
        .replaceAll("(?<!&)&([0-9a-fk-or])", "§$1")
        .replaceAll("&&([0-9a-fk-or])", "&$1");
   }
   
   private static String removeColorCodes(String str) {
      return str
        .replaceAll("(?<!&)&([0-9a-fk-or])", "")
        .replaceAll("&&([0-9a-fk-or])", "&$1");
   }
   
   private static String formatCommand(CommandSender sender, String str) {
      if(str == null)
         return "";
      str = translateColorCodes(str);
      if(str.startsWith(Config.ph("no replace")))
         return str.substring(Config.ph("no replace").length());
      return replacePlaceholders(sender, str);
   }
   
   private static String replacePAPI(CommandSender sender, String str) {
      if(Main.papi && sender instanceof final Player player)
         return PlaceholderAPI.setPlaceholders(player, str);
      return str;
   }
   
   // === Condition evaluation stays the same ===
   
   public static boolean evalConditions(List<String> conditions, CommandSender sender, String[] args) {
      if (conditions.isEmpty())
         return true;
      for(String str : conditions)
         if(!evalCondition(str, sender, args))
            return false;
      return true;
   }
   
   public static boolean evalCondition(String str, CommandSender sender, String[] args) {
      if(str == null || str.isBlank())
         return true;
      
      str = str.replace(" ", "");
      String replaced = replacePlaceholders(sender, replaceArgs(str, args));
      
      List<String> parts = splitOrParts(replaced);
      if(parts.size() > 1) {
         for(String part : parts)
            if(evalCondition(part, sender, args))
               return true;
         return false;
      }
      return evalSingle(parts.getFirst());
   }
   
   private static List<String> splitOrParts(String str) {
      List<String> parts = new ArrayList<>();
      int last = 0;
      for(int i = 0; i < str.length() - 1; i++) {
         if(str.charAt(i) == '|' && str.charAt(i + 1) == '|') {
            int bs = 0;
            for (int j = i - 1; j >= 0 && str.charAt(j) == '\\'; j--)
               bs++;
            if(bs % 2 == 0) {
               parts.add(str.substring(last, i));
               last = i + 2;
               i++;
            }
         }
      }
      parts.add(str.substring(last));
      return parts;
   }
   
   private static boolean evalSingle(String expr) {
      String[] ops = {"==", "=?=", "!=", "!?=", ">=", "<=", ">", "<"};
      for(String op : ops) {
         Pattern pattern = Pattern.compile(Pattern.quote(op));
         Matcher matcher = pattern.matcher(expr);
         
         while(matcher.find()) {
            int idx = matcher.start();
            int bs = 0;
            for (int i = idx - 1; i >= 0 && expr.charAt(i) == '\\'; i--)
               bs++;
            if(bs % 2 == 1)
               continue;
            return findAndCompare(expr, idx, op);
         }
      }
      return Config.isTrue(expr.toLowerCase());
   }
   
   private static boolean findAndCompare(String expr, int idx, String op) {
      String left = expr.substring(0, idx);
      String right = expr.substring(idx + op.length());
      
      left = left.replace("\\\\" + op, op);
      right = right.replace("\\\\" + op, op);
      
      return compare(left, right, op);
   }
   
   private static boolean compare(String left, String right, String op) {
      boolean isNumber = isNumeric(left) && isNumeric(right);
      
      if(isNumber) {
         double l = Double.parseDouble(left);
         double r = Double.parseDouble(right);
         return switch(op) {
            case "==", "=?=" -> l == r;
            case "!=", "!?=" -> l != r;
            case ">" -> l > r;
            case "<" -> l < r;
            case ">=" -> l >= r;
            case "<=" -> l <= r;
            default -> false;
         };
      }
      return switch(op) {
         case "==" -> left.equals(right);
         case "!=" -> !left.equals(right);
         case "=?=" -> left.equalsIgnoreCase(right);
         case "!?=" -> !left.equalsIgnoreCase(right);
         default -> false;
      };
   }
   
   private static boolean isNumeric(String str) {
      if(str == null)
         return false;
      return str.matches("[+-]?\\d+(\\.\\d+)?");
   }
   
   private static String str(Object obj) {
      return obj == null ? "" : obj.toString();
   }
}
