package com.ontey.execution;

import com.ontey.CustomCommand;
import com.ontey.Main;
import com.ontey.files.Config;
import com.ontey.holder.PlaceholderStorage;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ontey.execution.ConditionParser.str; // idk why this is not in here...

public class Formattation {
   
   public Execution exe;
   
   public CustomCommand cmd;
   public PlaceholderStorage storage;
   public CommandSender sender;
   public String label;
   public String[] args;
   
   public Formattation(Execution exe) {
      this.exe = exe;
      
      this.cmd = exe.cmd;
      this.storage = cmd.storage;
      this.sender = exe.sender;
      this.label = exe.label;
      this.args = exe.args;
   }
   
   public void formatCommand() {
      formatCommands(cmd.commands);
      
      formatMessages(cmd.messages);
      formatMessages(cmd.broadcasts);
      formatMessages(cmd.conditions);
      formatMessages(cmd.conditionErrorMessage);
   }
   
   public static String formattedMessage(String msg, Execution exe) {
      return new Formattation(exe).formatMessage(msg);
   }
   
   public void formatMessages(List<String> messages) {
      modify(messages, this::formatMessage);
   }
   
   public void formatCommands(List<String> commands) {
      modify(commands, this::formatCommand);
      ConditionParser.resolveConditions(commands, exe);
      commands.removeIf(str -> str == null || str.isEmpty());
   }
   
   private String formatCommand(String cmd) {
      cmd = formatMessage(cmd);
      
      //return ActionHolders.apply(sender, cmd, label, args, storage);
      return cmd;
   }
   
   private String formatMessage(String msg) {
      if(msg == null)
         return "";
      
      if(msg.startsWith(Config.ph("no-replace")))
         return msg.substring(Config.ph("no-replace").length());
      
      msg = Replacement.replaceArgs(msg, args);
      msg = replacePlaceholders(msg);
      msg = replaceCommandInfo(msg);
      return msg;
   }
   
   // Helpers
   
   private String replaceCommandInfo(String str) {
      str = str
        .replace(Config.ph("label"), label)
        .replace(Config.ph("usage"), str(cmd.usage))
        .replace(Config.ph("description"), str(cmd.description))
        .replace(Config.ph("permission"), str(cmd.permission))
        .replace(Config.ph("name"), str(cmd.name))
        .replace(Config.ph("command"), str(cmd.command))
        .replace(Config.ph("condition-error"), str(cmd.conditionErrorMessage));
      return str;
   }
   
   private String replacePlaceholders(String str) {
      str = replacePAPI(str);
      str = str.replace(Config.ph("args-length"), str(args.length));
      str = storage.apply(str, sender);
      return new MacroStringParser(exe).replaceMacroStrings(str);
   }
   
   private String replacePAPI(String str) {
      if(Main.papi && sender instanceof final Player player)
         return PlaceholderAPI.setPlaceholders(player, str);
      return str;
   }
   
   // Helpers
   
   public static <T> void modify(List<T> list, Function<T, T> converter) {
      List<T> out = new ArrayList<>(list.size());
      
      for (T t : list)
         out.add(converter.apply(t));
      
      list.clear();
      list.addAll(out);
   }
   
   public static Component replaceMM(String msg) {
      return Main.mm.deserialize(replace(msg));
   }
   
   private static String replace(String str) {
      str = deserializeRgbToHex(str);
      return str
        // Legacy
        .replaceAll("§([0-9a-fk-or])", "&$1")
        .replaceAll("(?<![&\\\\])&(/)?0", "<$1black>")
        .replaceAll("(?<![&\\\\])&(/)?1", "<$1dark_blue>")
        .replaceAll("(?<![&\\\\])&(/)?2", "<$1dark_green>")
        .replaceAll("(?<![&\\\\])&(/)?3", "<$1dark_aqua>")
        .replaceAll("(?<![&\\\\])&(/)?4", "<$1dark_red>")
        .replaceAll("(?<![&\\\\])&(/)?5", "<$1dark_purple>")
        .replaceAll("(?<![&\\\\])&(/)?6", "<$1gold>")
        .replaceAll("(?<![&\\\\])&(/)?7", "<$1gray>")
        .replaceAll("(?<![&\\\\])&(/)?8", "<$1dark_gray>")
        .replaceAll("(?<![&\\\\])&(/)?9", "<$1blue>")
        .replaceAll("(?<![&\\\\])&(/)?a", "<$1green>")
        .replaceAll("(?<![&\\\\])&(/)?b", "<$1aqua>")
        .replaceAll("(?<![&\\\\])&(/)?c", "<$1red>")
        .replaceAll("(?<![&\\\\])&(/)?d", "<$1light_purple>")
        .replaceAll("(?<![&\\\\])&(/)?e", "<$1yellow>")
        .replaceAll("(?<![&\\\\])&(/)?f", "<$1white>")
        
        .replaceAll("(?<![&\\\\])&(/)?k", "<$1obfuscated>")
        .replaceAll("(?<![&\\\\])&(/)?l", "<$1bold>")
        .replaceAll("(?<![&\\\\])&(/)?m", "<$1strikethrough>")
        .replaceAll("(?<![&\\\\])&(/)?n", "<$1underlined>")
        .replaceAll("(?<![&\\\\])&(/)?o", "<$1italic>")
        .replaceAll("(?<![&\\\\])&(/)?r", "<$1reset>")
        
        // Legacy Hex
        .replaceAll("(?<![&\\\\])(?<!&)&(/)?#([A-Fa-f0-9]{6})", "<$1#$2>")
        .replaceAll("(?<![&\\\\])(?<!§)§(/)?#([A-Fa-f0-9]{6})", "<$1#$2>")
        
        // Mini-Message abbreviations
        .replaceAll("(?<!\\\\)<cmd[+:;,=-]?([\"'])([^>]+)\\1>", "<click:run_command:$1$2$1>")
        .replaceAll("(?<!\\\\)<suggest[+:;,=-]?([\"'])([^>]+)\\1>", "<click:suggest_command:$1$2$1>")
        .replaceAll("(?<!\\\\)<copy[+:;,=-]?([\"'])([^>]+)\\1>", "<click:copy_to_clipboard:$1$2$1>")
        .replaceAll("(?<!\\\\)<url[+:;,=-]?([\"'])([^>]+)\\1>", "<click:open_url:$1$2$1>")
        .replaceAll("(?<!\\\\)<uurl[+:;,=-]?([\"'])([^>]+)\\1>", "<u><click:open_url:$1$2$1>")
        
        .replaceAll("(?<!\\\\)<cmd[+:;,=-]([^>\\s]+)>", "<click:run_command:$1>")
        .replaceAll("(?<!\\\\)<suggest[+:;,=-]([^>\\s]+)>", "<click:suggest_command:$1>")
        .replaceAll("(?<!\\\\)<copy[+:;,=-]([^>\\s]+)>", "<click:copy_to_clipboard:$1>")
        .replaceAll("(?<!\\\\)<url[+:;,=-]([^>\\s]+)>", "<click:open_url:$1>")
        .replaceAll("(?<!\\\\)<uurl[+:;,=-]([^>\\s]+)>", "<u><click:open_url:$1>")
        
        .replaceAll("(?<!\\\\)</cmd>", "</click:run_command>")
        .replaceAll("(?<!\\\\)</suggest>", "</click:suggest_command>")
        .replaceAll("(?<!\\\\)</copy>", "</click:copy_to_clipboard>")
        .replaceAll("(?<!\\\\)</url>", "</click:open_url>")
        .replaceAll("(?<!\\\\)</uurl>", "</click:open_url></u>")
      
        // Escaped
        .replaceAll("[&\\\\]&([0-9a-fk-or])", "&$1")
        .replaceAll("[&\\\\]&/([0-9a-fk-or])", "&/$1")
        
        .replaceAll("[&\\\\]&(/)?#([A-Fa-f0-9]{6})", "&$1#$2")
        .replaceAll("[§\\\\]§(/)?#([A-Fa-f0-9]{6})", "§$1#$2")
        
        .replaceAll("\\\\<(cmd|suggest|copy|url|uurl)([+:;,=-])?([\"'])([^>]+)\\1>", "<$1$2$3$4$3>")
        .replaceAll("\\\\<(cmd|suggest|copy|url|uurl)([+:;,=-])([^>\\s]+)>", "<$1$2$3>")
        .replaceAll("\\\\</(cmd|suggest|copy|url|uurl)>", "</$1>");
   }
   
   private static String deserializeRgbToHex(String str) {
      Pattern p = Pattern.compile("(?<!\\\\)<(\\s)?(/)?(\\s)??(\\d{1,3})[-,](\\s)?(\\d{1,3})[-,](\\s)?(\\d{1,3})(\\s)?>");
      Matcher m = p.matcher(str);
      StringBuilder sb = new StringBuilder();
      
      while(m.find()) {
         int r = Integer.parseInt(m.group(4));
         int g = Integer.parseInt(m.group(6));
         int b = Integer.parseInt(m.group(8));
         
         if(r >= 0 && r <= 255 && g >= 0 && g <= 255 && b >= 0 && b <= 255) {
            String hex = String.format("<$2#%02x%02x%02x>", r, g, b);
            m.appendReplacement(sb, hex);
            continue;
         }
         
         // Out of bounds ( >255 )
         m.appendReplacement(sb, Matcher.quoteReplacement(m.group()));
      }
      
      m.appendTail(sb);
      return sb.toString();
   }
}
