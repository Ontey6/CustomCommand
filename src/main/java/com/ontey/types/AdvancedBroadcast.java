package com.ontey.types;

import com.ontey.files.Commands;
import com.ontey.holder.CommandPaths;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class AdvancedBroadcast {
   
   public int range;
   
   public String permission;
   
   public String condition;
   
   public List<String> broadcast;
   
   public AdvancedBroadcast(int range, String permission, String condition, List<String> broadcast) {
      this.range = range;
      this.permission = permission;
      this.broadcast = broadcast;
      this.condition = condition;
   }
   
   @Nullable
   public static AdvancedBroadcast of(YamlConfiguration config, String command) {
      if(!config.isConfigurationSection(CommandPaths.AdvancedBroadcast.section(command)))
         return null;
      return Commands.advancedBroadcast(config, command);
   }
}