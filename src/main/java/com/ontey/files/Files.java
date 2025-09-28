package com.ontey.files;

import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class Files {
   public static List<String> getList(ConfigurationSection config, String path) {
      List<?> list = config.getList(path);
      if (list == null || list.isEmpty())
         return new ArrayList<>();
      
      List<String> out = new ArrayList<>();
      for (Object obj : list)
         out.add(obj == null ? "" : obj.toString());
      return out;
   }
   
   public static List<String> getField(ConfigurationSection config, String path) {
      return config.isString(path)
        ? Config.singletonList(config.getString(path, ""))
        : getList(config, path);
   }
   
   public static List<String> getField(ConfigurationSection config, String path, List<String> fallback) {
      if(!config.isSet(path))
         return fallback;
      return config.isString(path)
        ? Config.singletonList(config.getString(path, ""))
        : getList(config, path);
   }
   
   public static List<ConfigurationSection> getSections(ConfigurationSection config, String path) {
      if(path != null)
         config = config.getConfigurationSection(path);
      
      if(config == null)
         return new ArrayList<>(0);
      
      List<ConfigurationSection> out = new ArrayList<>();
      Set<String> keys = config.getKeys(false);
      for(String key : keys)
         if(config.isConfigurationSection(key))
            out.add(config.getConfigurationSection(key));
      
      return out;
   }
}
