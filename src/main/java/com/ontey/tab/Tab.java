package com.ontey.tab;

import com.ontey.files.Commands;
import com.ontey.holder.CommandPaths;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.util.ArrayList;
import java.util.List;

public class Tab {
   
   private final YamlConfiguration config;
   
   private final String command;
   
   private final String path;
   
   public Tab(YamlConfiguration config, String command) {
      this.config = config;
      this.command = command;
      this.path = CommandPaths.tab(command);
   }
   
   public int length() {
      int i = 1;
      ConfigurationSection section = config.getConfigurationSection(path);
      if (section == null)
         return 0;
      
      while (section.isList(str(i)) || section.isString(str(i)))
         i++;
      return i - 1;
   }
   
   public List<List<String>> getTabCompleter() {
      List<List<String>> out = new ArrayList<>();
      ConfigurationSection section = config.getConfigurationSection(path);
      if (section == null)
         return out;
      
      int i = 1;
      while (section.isList(str(i)) || section.isString(str(i))) {
         List<String> options = Commands.getField(config, path + "." + str(i));
         out.add(options);
         i++;
      }
      return out;
   }
   
   public List<String> getApplicableTabCompleter(String[] args) {
      if(args.length > length())
         return List.of();
      return getTabCompleter().get(args.length - 1);
   }
   
   private String str(Object obj) {
      return obj == null ? "" : obj.toString();
   }
}
