package com.ontey.types;

import com.ontey.execution.Execution;
import com.ontey.files.Commands;
import com.ontey.holder.Paths;
import com.ontey.holder.Placeholders;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class AdvancedBroadcast {
   
   private final String range;
   
   public final List<String> permission;
   
   public final List<String> condition;
   
   public final List<String> broadcast;
   
   public AdvancedBroadcast(String range, List<String> permission, List<String> condition, List<String> broadcast) {
      this.range = range;
      this.permission = permission;
      this.broadcast = broadcast;
      this.condition = condition;
   }
   
   @Nullable
   public static AdvancedBroadcast of(YamlConfiguration config, String command) {
      if(!config.isConfigurationSection(Paths.AdvancedBroadcast.section(command)))
         return null;
      return Commands.getAdvancedBroadcast(config, command);
   }
   
   public double range(CommandSender sender, String[] args) {
      String str = Placeholders.apply(sender, Execution.replaceArgs(range, args));
      if(!str.matches("[+-]?\\d+(\\.\\d+)?"))
         return -1;
      return Double.parseDouble(str);
   }
}