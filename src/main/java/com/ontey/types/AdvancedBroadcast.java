package com.ontey.types;

import com.ontey.files.Commands;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class AdvancedBroadcast {
   
   public int range;
   
   public String permission;
   
   public List<String> broadcast;
   
   public String condition;
   
   public AdvancedBroadcast(int range, String permission, List<String> broadcast, String condition) {
      this.range = range;
      this.permission = permission;
      this.broadcast = broadcast;
      this.condition = condition;
   }
   
   @Nullable
   public static AdvancedBroadcast of(YamlConfiguration config, String command) {
      if(!config.isConfigurationSection(command + ".broadcast"))
         return null;
      return Commands.advancedBroadcast(config, command);
   }
}