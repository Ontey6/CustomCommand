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
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Execution {
   
   public static void sendMessages(List<String> messages, CommandSender sender, String[] args) {
      if(!messages.isEmpty())
         for(String msg : messages)
            if(!msg.isEmpty())
               sender.sendMessage(formatMessage(replaceArgs(msg, args), sender));
   }
   
   public static void runCommands(List<String> commands, CommandSender sender, String[] args) {
      if(!commands.isEmpty())
         for(String cmd : commands)
            if(!formatCommand(sender, replaceArgs(cmd, args)).isEmpty())
               Bukkit.dispatchCommand(sender, formatCommand(sender, replaceArgs(cmd, args)));
   }
   
   public static void sendBroadcasts(List<String> broadcasts, CommandSender sender, String[] args) {
      if(!broadcasts.isEmpty())
         for(Player player : Bukkit.getOnlinePlayers())
            for(String bc : broadcasts)
               if(!bc.isEmpty())
                  player.sendMessage(formatMessage(replaceArgs(bc, args), sender));
   }
   
   public static void sendAdvancedBroadcast(AdvancedBroadcast advancedBroadcast, CommandSender sender, String[] args) {
      if (advancedBroadcast == null)
         return;
      String permission = advancedBroadcast.permission;
      String condition = advancedBroadcast.condition;
      double range = advancedBroadcast.range;
      Location senderLoc = (sender instanceof Player p) ? p.getLocation() : null;
      
      for (Player player : Bukkit.getOnlinePlayers()) {
         if (permission != null && !player.hasPermission(permission))
            continue;
         if(!evalCondition(condition, sender, args))
            continue;
         if (range != -1 && senderLoc != null)
            if (!player.getWorld().equals(senderLoc.getWorld()) || player.getLocation().distance(senderLoc) > range)
               continue;
         for(String msg : advancedBroadcast.broadcast)
            player.sendMessage(formatMessage(msg, sender));
      }
   }
   
   // Helpers
   
   private static String replacePlaceholders(CommandSender sender, @NotNull String str) {
      str = Placeholders.apply(sender, str);
      str = replacePAPI(sender, str);
      return ActionHolders.apply(sender, str);
   }
   
   private static String replaceArgs(@NotNull String str, String[] args) {
      List<String> list = Arrays.asList(args);
      
      for (int i = 1; i <= args.length; i++)
         str = replaceArg(str, list, i, args);
      
      return str;
   }
   
   private static String replaceArg(String str, List<String> list, int i, String[] args) {
      str = str
        .replace(Config.ph("arg" + i), args[i - 1])                // argX
        .replace(Config.ph("arg" + i + ".."), join(list, i, args.length)) //  argX..
        .replace(Config.ph("arg.." + i), join(list, 1, i));      //   arg..X
      
      for (int j = i; j <= args.length; j++)
         str = str.replace(Config.ph("arg" + i + ".." + j), join(list, i, j)); // argX..Y
      
      return str;
   }
   
   
   private static String join(List<String> list, int start, int end) {
      return String.join(" ", list.subList(start - 1, end));
   }
   
   
   private static String formatMessage(String message, CommandSender sender) {
      if(message == null)
         return null;
      
      String str =
        sender instanceof ConsoleCommandSender && Config.REMOVE_COLORS_IN_CONSOLE
          ? removeColorCodes(message)
          : translateColorCodes(message);
      
      if(str.startsWith(Config.ph("no replace")))
         return str.substring(Config.ph("no replace").length());
      
      str = replacePlaceholders(sender, str);
      
      return str;
   }
   
   private static String translateColorCodes(String str) {
      if(str == null)
         return null;
      return str
        .replaceAll("(?<!&)&([0-9a-fk-or])", "§$1") // replace &<c> with §<c>
        .replaceAll("&&([0-9a-fk-or])", "&$1");     // replace &&<c> with &<c>
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
      str = replacePlaceholders(sender, str);
      
      return str;
   }
   
   private static String replacePAPI(CommandSender sender, String str) {
      if(Main.papi && sender instanceof final Player player)
         return PlaceholderAPI.setPlaceholders(player, str);
      return str;
   }
   
   // evaluation
   
   public static boolean evalCondition(String str, CommandSender sender, String[] args) {
      if (str == null || str.isBlank())
         return true;
      
      str = str.replace(" ", "");
      String replaced = replacePlaceholders(sender, replaceArgs(str, args)); // needed here
      
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
