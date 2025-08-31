package com.ontey.files;

import com.ontey.CustomCommand;
import com.ontey.Main;
import com.ontey.execution.Execution;
import com.ontey.holder.CommandPaths;
import com.ontey.log.Log;
import com.ontey.types.AdvancedBroadcast;
import com.ontey.types.Args;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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
   public static List<String> getField(YamlConfiguration config, String command, String field) {
      return getField(config, command + "." + field);
   }
   
   @NotNull
   public static List<String> getField(YamlConfiguration config, String path) {
      if(config.isString(path) && config.getString(path) != null)
         // noinspection ConstantConditions
         return new ArrayList<>(List.of(config.getString(path))); // just in case
      return getList(config, path);
   }
   
   @NotNull
   public static List<String> getMessages(YamlConfiguration config, String command) {
      return getField(config, CommandPaths.message(command));
   }
   
   @NotNull
   public static List<String> getBroadcasts(YamlConfiguration config, String command) {
      return getField(config, CommandPaths.broadcast(command));
   }
   
   @NotNull
   public static List<String> getCommands(YamlConfiguration config, String command, CommandSender sender, String[] args) {
      String commandsPath = CommandPaths.Commands.section(command);
      if (!config.isConfigurationSection(commandsPath))
         return getField(config, commandsPath);
      
      return resolveCommandsSection(config, commandsPath, sender, args, command);
   }
   
   private static List<String> resolveCommandsSection(YamlConfiguration config, String path, CommandSender sender, String[] args, String command) {
      ConfigurationSection section = config.getConfigurationSection(path);
      if (section == null)
         return new ArrayList<>(0);
      
      String condition = config.getString(CommandPaths.Commands.condition(command), null);
      if (condition != null) {
         boolean result = Execution.evalCondition(condition, sender, args);
         String branchPath = result
           ? CommandPaths.Commands.conditionTrue(command)
           : CommandPaths.Commands.conditionFalse(command);
         
         if (config.isConfigurationSection(branchPath))
            return resolveCommandsSection(config, branchPath, sender, args, command);
         
         List<String> branchList = config.getStringList(branchPath);
         if (!branchList.isEmpty())
            return branchList;
         
         return new ArrayList<>(0);
      }
      
      List<String> list = config.getStringList(path);
      if (!list.isEmpty())
         return list;
      
      // TODO replace legacy way of only accepting commands as a name.
      int idx = path.indexOf(".commands");
      if (idx > 0)
         return getField(config, path.substring(0, idx), "commands");
      
      return new ArrayList<>(0);
   }
   
   @NotNull
   public static List<String> getAliases(YamlConfiguration config, String command) {
      return getField(config, CommandPaths.aliases(command));
   }
   
   public static Args getArgs(YamlConfiguration config, String command) {
      return new Args(config, command); // TODO
   }
   
   public static String getPermission(YamlConfiguration config, String command) {
      String value = config.getString(CommandPaths.permission(command));
      if(!requiresPermission(config, command))
         return null;
      if(value == null)
         return Config.defaultPerm(command);
      return value;
   }
   
   public static boolean requiresPermission(YamlConfiguration config, String command) {
      return config.getBoolean(CommandPaths.permissionRequired(command), true);
   }
   
   public static String getDescription(YamlConfiguration config, String command) {
      return String.join("\n", getField(config, CommandPaths.description(command)));
   }
   
   public static String getUsage(YamlConfiguration config, String command) {
      return String.join("\n", getField(config, CommandPaths.usage(command)));
   }
   
   public static AdvancedBroadcast advancedBroadcast(YamlConfiguration config, String command) {
      int range = config.getInt(CommandPaths.AdvancedBroadcast.range(command), -1);
      String permission = config.getString(CommandPaths.AdvancedBroadcast.permission(command));
      List<String> broadcast = getField(config, CommandPaths.AdvancedBroadcast.broadcast(command));
      String condition = config.getString(CommandPaths.AdvancedBroadcast.condition(command));
      
      return new AdvancedBroadcast(range, permission, condition, broadcast);
   }
}
