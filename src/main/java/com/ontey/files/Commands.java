package com.ontey.files;

import com.ontey.CustomCommand;
import com.ontey.Main;
import com.ontey.holder.CommandPaths;
import com.ontey.log.Log;
import com.ontey.tab.Tab;
import com.ontey.types.AdvancedBroadcast;
import com.ontey.types.Args;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
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
            Log.info("Couldn't create the commands directory. Disabling plugin");
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
   @Deprecated(since = "2.0")
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
   public static List<String> getCommands(YamlConfiguration config, String command) {
      return getField(config, CommandPaths.commands(command));
   }
   
   public static List<String> getAliases(YamlConfiguration config, String command) {
      return getField(config, CommandPaths.aliases(command));
   }
   
   public static Args getArgs(YamlConfiguration config, String command) {
      return new Args(config, command);
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
   
   public static AdvancedBroadcast getAdvancedBroadcast(YamlConfiguration config, String command) {
      int range = config.getInt(CommandPaths.AdvancedBroadcast.range(command), -1);
      String permission = config.getString(CommandPaths.AdvancedBroadcast.permission(command));
      List<String> broadcast = getField(config, CommandPaths.AdvancedBroadcast.broadcast(command));
      List<String> condition = getField(config, CommandPaths.AdvancedBroadcast.condition(command));
      
      return new AdvancedBroadcast(range, permission, condition, broadcast);
   }
   
   public static String getNamespace(YamlConfiguration config, String command) {
      return config.getString(CommandPaths.namespace(command), Config.DEFAULT_NAMESPACE);
   }
   
   public static List<String> getNoTab(YamlConfiguration config, String command) {
      Config.NoTab noTabMode = Config.getNoTab(config.getString(CommandPaths.noTab(command)));
      return switch(noTabMode) {
         case NONE -> List.of();
         case PLAYERS -> Tab.onlinePlayers();
         case null -> Config.noTab();
      };
   }
}
