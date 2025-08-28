package com.ontey.files;

import com.ontey.CustomCommand;
import com.ontey.Main;
import com.ontey.execution.Execution;
import com.ontey.log.Log;
import com.ontey.types.AdvancedBroadcast;
import com.ontey.types.Args;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public class Commands {
   
   public static final List<CustomCommand> registeredCommands = new ArrayList<>();
   
   private Commands() { }
   
   public static void load() {
      File dir = new File(Main.instance.getDataFolder(), "commands");
      
      if (!dir.exists()) {
         if (!dir.mkdirs()) {
            Main.disablePlugin();
            return;
         }
         loadExamples();
      }
   }
   
   public static void loadExamples() {
      File examples = new File(Main.instance.getDataFolder(), "commands/examples.yml");
      
      if (examples.exists())
         return;
      // save to root first
      Main.instance.saveResource("examples.yml", false);
      
      File rootFile = new File(Main.instance.getDataFolder(), "examples.yml");
      if (rootFile.exists())
         if(!rootFile.renameTo(examples))
            Log.info("Couldn't copy the examples file");
   }
   
   public static List<String> getList(YamlConfiguration config, String path) {
      List<String> out = new ArrayList<>();
      List<?> list = config.getList(path);
      if (list == null || list.isEmpty())
         return out;
      for(Object obj : list)
         out.add(obj == null ? "" : obj.toString());
      return out;
   }
   
   // This returns a list from either a String or a list
   
   @NotNull
   private static List<String> getField(YamlConfiguration config, String command, String field) {
      String path = command + "." + field;
      if(config.isString(path) && config.getString(path) != null)
         // noinspection ConstantConditions
         return new ArrayList<>(List.of(config.getString(path))); // just in case
      return getList(config, path);
   }
   
   @NotNull
   public static List<String> getMessages(YamlConfiguration config, String command) {
      return getField(config, command, "message");
   }
   
   @NotNull
   public static List<String> getBroadcasts(YamlConfiguration config, String command) {
      return getField(config, command, "broadcast");
   }
   
   @NotNull
   public static List<String> getCommands(YamlConfiguration config, String command) {
      return getField(config, command, "commands");
   }
   
   // TODO
   public static List<String> getCommands(YamlConfiguration config, String command, CommandSender sender) {
      ConfigurationSection section = config.getConfigurationSection(command + ".commands");
      if (section != null) {
            String conditionPath = ".condition";
            if (!section.isString(conditionPath) || Execution.evalCondition(section.getString(conditionPath), sender))
               return getCommands(config, command + ".commands.true", sender);
            return getCommands(config, command + ".commands.false", sender);
      }
      return getField(config, command, "commands");
   }
   
   @NotNull
   public static List<String> getAliases(YamlConfiguration config, String command) {
      return getField(config, command, "aliases");
   }
   
   public static Args getArgs(YamlConfiguration config, String command) {
      return new Args(config, command);
   }
   
   public static String getPermission(YamlConfiguration config, String command) {
      return config.getString(command + ".permission");
   }
   
   public static String getDescription(YamlConfiguration config, String command) {
      return config.getString(command + ".description");
   }
   
   public static String getUsage(YamlConfiguration config, String command) {
      return config.getString(command + ".usage");
   }
   
   public static AdvancedBroadcast advancedBroadcast(YamlConfiguration config, String command) {
      int range = config.getInt(command + ".broadcast.range", -1);
      String permission = config.getString(command + ".broadcast.permission");
      List<String> messages = getField(config, command, "broadcast");
      String condition = config.getString(command + ".broadcast.condition");
      return new AdvancedBroadcast(range, permission, messages, condition);
   }
}
