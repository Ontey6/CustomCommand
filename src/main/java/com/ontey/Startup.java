package com.ontey;

import com.ontey.log.Log;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandMap;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Startup {
   
   public static void loadCommands() {
      CustomCommand.registeredCommands.addAll(getCommands());
      CommandMap commandMap = getCommandMap();
      
      for (CustomCommand command : CustomCommand.registeredCommands) {
         loadCommand(commandMap, command);
      }
   }
   
   private static void loadCommand(CommandMap commandMap, CustomCommand command) {
      BukkitCommand cmd = new BukkitCommand(command.command) {
         @Override
         public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
            command.execute(sender, label, args);
            return true;
         }
         
         @Override
         public @NotNull List<String> tabComplete(@NotNull CommandSender sender, @NotNull String label, @NotNull String[] args) {
            return command.tab.getTabCompleter(args);
         }
      };
      
      cmd.setAliases(command.aliases);
      cmd.setDescription(command.description);
      cmd.setUsage(command.usage);
      cmd.setPermission(command.getPermission());
      
      commandMap.register(command.namespace, cmd);
   }
   
   
   public static CommandMap getCommandMap() {
      try {
         return (CommandMap) Bukkit.getServer().getClass().getMethod("getCommandMap").invoke(Bukkit.getServer());
      } catch (Exception e) {
         throw new RuntimeException("Unable to get CommandMap, disabling plugin", e);
      }
   }
   
   private static List<CustomCommand> getCommands() {
      List<CustomCommand> out = new ArrayList<>();
      File dir = new File(Main.instance.getDataFolder(), "commands");
      
      if (!dir.exists() || !dir.isDirectory())
         return out;
      
      File[] files = dir.listFiles((d, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
      
      if (files == null)
         return out;
      
      for (File file : files) {
         YamlConfiguration config = new YamlConfiguration();
         try {
            config.load(file);
            for (String name : config.getKeys(false))
               if (name != null)
                  out.add(new CustomCommand(config, file, name));
         } catch (Exception e) {
            Log.error(
              "+-+-+-+-+-+-+-+-+-+-+-+-CCMD-+-+-+-+-+-+-+-+-+-+-+-+-+",
              "  Couldn't load the commands file named '" + file.getName() + "'.",
              "  Look at the stack-trace below, so you can identify the error.",
              "  There is probably a syntax error in the yml.",
              "  Fix the error, then restart the server and it will work again.",
              "  The plugin will just continue without this file.",
              "+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+"
            );
            //noinspection CallToPrintStackTrace
            e.printStackTrace();
         }
      }
      
      return out;
   }
   
}
