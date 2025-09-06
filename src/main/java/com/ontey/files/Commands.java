package com.ontey.files;

import com.ontey.CustomCommand;
import com.ontey.Main;
import com.ontey.holder.Paths;
import com.ontey.log.Log;
import com.ontey.tab.Tab;
import com.ontey.types.AdvancedBroadcast;
import com.ontey.types.Args;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Commands {
   
   public static final @NotNull List<@NotNull CustomCommand> registeredCommands = new ArrayList<>();
   
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
   
   private static void loadExamples() {
      File examples = new File(Main.instance.getDataFolder(), "commands/examples.yml");
      
      if (examples.exists())
         return;
      
      Main.instance.saveResource("examples.yml", false);
      File rootFile = new File(Main.instance.getDataFolder(), "examples.yml");
      
      if (rootFile.exists() && !rootFile.renameTo(examples))
         Log.info("Couldn't copy the examples file");
   }
   
   public static @NotNull List<@NotNull String> getList(@NotNull YamlConfiguration config, @NotNull String path) {
      List<?> list = config.getList(path);
      if (list == null || list.isEmpty())
         return new ArrayList<>();
      
      List<String> out = new ArrayList<>();
      for (Object obj : list)
         out.add(obj == null ? "" : obj.toString());
      return out;
   }
   
   public static @NotNull List<@NotNull String> getField(@NotNull YamlConfiguration config, @NotNull String path) {
      return config.isString(path)
        ? Collections.singletonList(config.getString(path, ""))
        : getList(config, path);
   }
   
   public static @NotNull List<@NotNull String> getMessages(@NotNull YamlConfiguration config, @NotNull String command) {
      return getField(config, Paths.message(command));
   }
   
   public static @NotNull List<@NotNull String> getBroadcasts(@NotNull YamlConfiguration config, @NotNull String command) {
      return getField(config, Paths.broadcast(command));
   }
   
   public static @NotNull List<@NotNull String> getCommands(@NotNull YamlConfiguration config, @NotNull String command) {
      return getField(config, Paths.commands(command));
   }
   
   public static @NotNull List<@NotNull String> getAliases(@NotNull YamlConfiguration config, @NotNull String command) {
      return getField(config, Paths.aliases(command));
   }
   
   public static @NotNull Args getArgs(@NotNull YamlConfiguration config, @NotNull String command) {
      return new Args(config, command);
   }
   
   public static @Nullable String getPermission(@NotNull YamlConfiguration config, @NotNull String command) {
      if (!requiresPermission(config, command))
         return null;
      String value = config.getString(Paths.permission(command));
      return value != null ? value : Config.defaultPerm(command);
   }
   
   public static boolean requiresPermission(@NotNull YamlConfiguration config, @NotNull String command) {
      return config.getBoolean(Paths.permissionRequired(command), true);
   }
   
   public static @NotNull String getDescription(@NotNull YamlConfiguration config, @NotNull String command) {
      return String.join("\n", getField(config, Paths.description(command)));
   }
   
   public static @NotNull String getUsage(@NotNull YamlConfiguration config, @NotNull String command) {
      return String.join("\n", getField(config, Paths.usage(command)));
   }
   
   public static @NotNull AdvancedBroadcast getAdvancedBroadcast(@NotNull YamlConfiguration config, @NotNull String command) {
      String range = config.getString(Paths.AdvancedBroadcast.range(command), "-1");
      List<String> permission = getField(config, Paths.AdvancedBroadcast.permission(command));
      List<String> broadcast = getField(config, Paths.AdvancedBroadcast.broadcast(command));
      List<String> condition = getField(config, Paths.AdvancedBroadcast.condition(command));
      
      return new AdvancedBroadcast(range, permission, condition, broadcast);
   }
   
   public static @NotNull String getNamespace(@NotNull YamlConfiguration config, @NotNull String command) {
      return config.getString(Paths.namespace(command), Config.DEFAULT_NAMESPACE);
   }
   
   public static @NotNull List<@NotNull String> getNoTab(@NotNull YamlConfiguration config, @NotNull String command) {
      Config.NoTab noTabMode = Config.getNoTab(config.getString(Paths.noTab(command)));
      return switch (noTabMode) {
         case NONE -> List.of();
         case PLAYERS -> Tab.onlinePlayers();
         case null -> Config.noTab();
      };
   }
}
