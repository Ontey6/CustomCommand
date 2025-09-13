package com.ontey.execution;

import com.ontey.Main;
import com.ontey.files.Config;
import com.ontey.holder.ActionHolders;
import com.ontey.holder.Placeholders;
import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import static com.ontey.execution.Evaluation.str;

public class Formattation {
   
   static String formatCommand(CommandSender sender, String str, String[] args) {
      if(str == null)
         return "";
      if(str.startsWith(Config.ph("no replace")))
         return str.substring(Config.ph("no replace").length());
      str = ActionHolders.apply(sender, str, args);
      str = replacePlaceholders(sender, str, args);
      return translateColorCodes(str);
   }
   
   public static String formatMessage(String message, CommandSender sender, String[] args) {
      if(message == null)
         return "";
      
      String str =
        sender instanceof ConsoleCommandSender && Config.REMOVE_COLORS_IN_CONSOLE
          ? removeColorCodes(message)
          : translateColorCodes(message);
      
      if(str.startsWith(Config.ph("no replace")))
         str = str.substring(Config.ph("no replace").length());
      
      str = replacePlaceholders(sender, str, args);
      return str;
   }
   
   // Helpers
   
   static String replacePlaceholders(CommandSender sender, @NotNull String str, String[] args) {
      str = replacePAPI(sender, str);
      str = Placeholders.apply(sender, str);
      str = str.replace(Config.ph("args-length"), str(args.length));
      return MacroStrings.replaceMacroStrings(str, sender, args);
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
   
   static String replacePAPI(CommandSender sender, String str) {
      if(Main.papi && sender instanceof final Player player)
         return PlaceholderAPI.setPlaceholders(player, str);
      return str;
   }
}
