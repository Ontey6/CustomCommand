package com.ontey.types;

import com.ontey.files.Commands;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import java.util.List;

public class AdvancedBroadcast {
   
   public int range;
   
   public String permission;
   
   public List<String> messages;
   
   @SuppressWarnings("DeprecatedIsStillUsed")
   @Deprecated(since = "only use of()!")
   public AdvancedBroadcast(int range, String permission, List<String> messages) {
      this.range = range;
      this.permission = permission;
      this.messages = messages;
   }
   
   @Nullable
   public static AdvancedBroadcast of(YamlConfiguration config, String command) {
      if(!config.isConfigurationSection(command + ".broadcast"))
         return null;
      return Commands.advancedBroadcast(config, command);
   }
}