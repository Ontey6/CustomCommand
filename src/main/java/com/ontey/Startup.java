package com.ontey;

import com.ontey.files.Commands;
import com.ontey.files.Config;
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
   
   @SuppressWarnings("DataFlowIssue")
   public static void loadCommands() {
      Commands.registeredCommands.addAll(getCommands());
      CommandMap commandMap = getCommandMap();
      
      for(CustomCommand command : Commands.registeredCommands) {
         
         BukkitCommand cmd = new BukkitCommand(command.name) {
            public boolean execute(@NotNull CommandSender sender, @NotNull String label, String [] args) {
               command.execute(sender, label, args);
               return true;
            }
         };
         
         cmd.setAliases(command.aliases);
         cmd.setDescription(Config.getOrDefault(command.description, Config.DEFAULT_DESCRIPTION));
         cmd.setUsage(Config.getOrDefault(command.usage, Config.DEFAULT_USAGE));
         cmd.setPermission(Config.perm(command));
         
         commandMap.register(Config.COMMAND_PREFIX, cmd);
      }
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
      
      File[] files = dir.listFiles((d, name) -> name.endsWith(".yml"));
      
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
