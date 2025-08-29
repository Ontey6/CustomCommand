package com.ontey.execution;

import com.ontey.Main;
import com.ontey.files.Config;
import com.ontey.holder.Placeholders;
import com.ontey.types.AdvancedBroadcast;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Execution {
   
   public static void sendMessages(List<String> messages, CommandSender sender, String[] args) {
      if(!messages.isEmpty())
         for(String msg : messages)
            sender.sendMessage(formatMessage(replaceArgs(msg, args), sender));
   }
   
   public static void runCommands(List<String> commands, CommandSender sender, String[] args) {
      if(!commands.isEmpty())
         for(String str : commands)
            Bukkit.dispatchCommand(sender, formatCommand(sender, replaceArgs(str, args)));
   }
   
   public static void sendBroadcasts(List<String> broadcasts, CommandSender sender, String[] args) {
      if(!broadcasts.isEmpty())
         for(Player player : Bukkit.getOnlinePlayers())
            for(String msg : broadcasts)
               player.sendMessage(formatMessage(replaceArgs(msg, args), sender));
   }
   
   public static void sendAdvancedBroadcast(AdvancedBroadcast advancedBroadcast, CommandSender sender) {
      if (advancedBroadcast != null) {
         String permission = advancedBroadcast.permission;
         String condition = advancedBroadcast.condition;
         double range = advancedBroadcast.range;
         Location senderLoc = (sender instanceof Player p) ? p.getLocation() : null;
         
         for (Player player : Bukkit.getOnlinePlayers()) {
            if (permission != null && !player.hasPermission(permission))
               continue;
            if(!evalCondition(condition, sender))
               continue;
            if (range != -1 && senderLoc != null)
               if (!player.getWorld().equals(senderLoc.getWorld()) || player.getLocation().distance(senderLoc) > range)
                  continue;
            for(String msg : advancedBroadcast.broadcast)
               player.sendMessage(formatMessage(msg, sender));
         }
      }
   }
   
   // Helpers
   
   private static String replacePlaceholders(CommandSender sender, @NotNull String str) {
      return Placeholders.apply(sender, str);
   }
   
   private static String replaceArgs(@NotNull String str, String[] args) {
      List<String> list = Arrays.asList(args);
      int len = args.length;
      
      for (int i = 1; i <= len; i++) {
         str = str
           .replace(Config.ph("arg" + i), args[i - 1]) // argX
           .replace(Config.ph("arg" + i + ".."), join(list, i, len)) // argX..
           .replace(Config.ph("arg.." + i), join(list, 1, i)); // arg..X
         
         // argX..Y
         for (int j = i; j <= len; j++)
            str = str.replace(Config.ph("arg" + i + ".." + j), join(list, i, j));
      }
      return str;
   }
   
   private static String join(List<String> list, int start, int end) {
      return String.join(" ", list.subList(start - 1, end));
   }
   
   
   private static String formatMessage(String message, CommandSender sender) {
      if(message == null)
         return null;
      
      String str = translateColorCodes(message);
      
      if(str.startsWith(Config.ph("no replace")))
         return str.substring(Config.ph("no replace").length());
      
      str = replacePlaceholders(sender, str);
      str = replacePAPI(sender, str);
      
      return str;
   }
   
   private static String translateColorCodes(String input) {
      if(input == null)
         return null;
      return input.replaceAll("(?<!&)&([0-9a-fk-or])", "§$1") // replace &<c> with §<c>
        .replaceAll("&&([0-9a-fk-or])", "&$1");     // replace &&<c> with &<c>
   }
   
   private static String formatCommand(CommandSender sender, String str) {
      if(str == null)
         return "";
      str = translateColorCodes(str);
      if(str.startsWith(Config.ph("no replace")))
         return str.substring(Config.ph("no replace").length());
      str = replacePlaceholders(sender, str);
      
      return str;
   }
   
   private static String replacePAPI(CommandSender sender, String str) {
      if(Main.papi && sender instanceof final Player player)
         return PlaceholderAPI.setPlaceholders(player, str);
      return str;
   }
   
   public static boolean evalCondition(String str, CommandSender sender) {
      if (str == null || str.isBlank())
         return true;
      
      str = str.replace(" ", "");
      String replaced = Placeholders.apply(sender, str);
      
      // Operator check
      String[] ops = {"==", "=?=", "!=", "!?=", ">=", "<=", ">", "<"};
      for (String op : ops) {
         Pattern pattern = Pattern.compile(Pattern.quote(op));
         Matcher matcher = pattern.matcher(replaced);
         
         while (matcher.find()) {
            int idx = matcher.start();
            
            // count backslashes before operator
            int bs = 0;
            for (int i = idx - 1; i >= 0 && replaced.charAt(i) == '\\'; i--)
               bs++;
            
            // skip if odd number of backslashes (means escaped)
            if (bs % 2 == 1)
               continue;
            
            String left = replaced.substring(0, idx);
            String right = replaced.substring(idx + op.length());
            
            // remove escapes: turn "\==" into "=="
            left = left.replace("\\\\", "\\").replace("\\" + op, op);
            right = right.replace("\\\\", "\\").replace("\\" + op, op);
            
            return compare(left, right, op);
         }
      }
      
      
      
      // Direct boolean check
      String lower = replaced.toLowerCase();
      return Config.isTrue(lower);
   }
   
   private static boolean compare(String left, String right, String op) {
      boolean isNumber = isNumeric(left) && isNumeric(right);
      
      if (isNumber) {
         double l = Double.parseDouble(left);
         double r = Double.parseDouble(right);
         return switch (op) {
            case "==", "=?=" -> l == r;
            case "!=", "!?=" -> l != r;
            case ">"  -> l > r;
            case "<"  -> l < r;
            case ">=" -> l >= r;
            case "<=" -> l <= r;
            default -> false;
         };
      }
      return switch (op) {
         case "==" -> left.equals(right);
         case "!=" -> !left.equals(right);
         case "=?=" -> left.equalsIgnoreCase(right);
         case "!?=" -> !left.equalsIgnoreCase(right);
         default -> false;
      };
   }
   
   private static boolean isNumeric(String str) {
      if (str == null)
         return false;
      return str.matches("[+-]?\\d+(\\.\\d+)?");
   }
}
