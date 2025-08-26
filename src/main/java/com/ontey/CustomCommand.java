package com.ontey;

import com.ontey.execution.Execution;
import com.ontey.files.Commands;
import com.ontey.files.Config;
import com.ontey.types.AdvancedBroadcast;
import com.ontey.types.Args;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.List;
public class CustomCommand {
   
   // On command
   
   public List<String> commands, messages, broadcasts;
   
   @Nullable
   public AdvancedBroadcast advancedBroadcast;
   
   public Args args;
   
   // On startup
   
   public final YamlConfiguration config;
   
   public final File file;
   
   public final List<String> aliases;
   
   public final String name;
   
   @Nullable
   public final String permission, description, usage;
   
   public CustomCommand(YamlConfiguration config, File file, String name) {
      this.config = config;
      this.file = file;
      this.name = name;
      this.aliases = Commands.getAliases(config, name);
      this.permission = Commands.getPermission(config, name);
      this.description = Commands.getDescription(config, name);
      this.usage = Commands.getUsage(config, name);
      loadMutable(false);
   }
   
   public void loadMutable(boolean reload) {
      if(reload)
         try {
            config.load(file);
         } catch(Exception e) {
            return;
         }
      this.messages = Commands.getMessages(config, name);
      this.broadcasts = Commands.getBroadcasts(config, name);
      this.advancedBroadcast = AdvancedBroadcast.of(config, name);
      this.commands = Commands.getCommands(config, name);
      this.args = Commands.getArgs(config, name);
   }
   
   public void execute(CommandSender sender, String label, String[] args) {
      if(!this.args.checkArgs(sender, args))
         return;
      if(Config.HOTSWAP)
         loadMutable(true);
      Execution.sendMessages(messages, sender, args);
      Execution.runCommands(commands, sender, args);
      Execution.sendBroadcasts(broadcasts, sender, args);
      Execution.sendAdvancedBroadcast(advancedBroadcast, sender);
   }
   
   public String toString() {
      return name;
   }
}