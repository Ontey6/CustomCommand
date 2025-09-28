package com.ontey.tab;

import com.ontey.CustomCommand;
import com.ontey.execution.Formattation;
import com.ontey.files.Files;
import com.ontey.holder.Paths;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

import static com.ontey.files.Config.ph;

public class Tab {
   
   private final CustomCommand cmd;
   
   private final YamlConfiguration config;
   
   private final String path;
   
   public Tab(CustomCommand cmd) {
      this.cmd = cmd;
      this.config = cmd.config;
      this.path = Paths.tab(cmd.name);
   }
   
   private Map<Integer, List<String>> rawTabCompleter() {
      Map<Integer, List<String>> out = new HashMap<>();
      ConfigurationSection section = config.getConfigurationSection(path);
      if (section == null)
         return out;
      
      Set<String> keys = section.getKeys(false);
      keys.removeIf(key -> !key.matches("\\d+"));
      
      AtomicInteger i = new AtomicInteger();
      keys.forEach(key ->
        out.put(Integer.parseInt(key), evalEscapes(key, section, (i.getAndIncrement())))
      );
      
      return out;
   }
   
   public List<String> getTabCompleter(String[] args) {
      if(config.getString(path, "").equals("args"))
         return dynamic(args.length <= cmd.args.getRaw().size()
           ? cmd.args.getRaw().get(args.length - 1)
           : cmd.getNoTab()
           , args
         );
      
      Map<Integer, List<String>> rawTab = rawTabCompleter();
      if(!rawTab.containsKey(args.length))
         return cmd.getNoTab();
      
      return dynamic(rawTab.get(args.length), args);
   }
   
   public List<String> dynamic(List<String> available, String[] args) {
      List<String> list = new ArrayList<>(available);
      list.removeIf(
        str ->
          !str.startsWith(ph("static"))
            && !str.toLowerCase().startsWith(args[args.length - 1].toLowerCase())
      );
      Formattation.modify(list, this::evalStatic);
      
      return list;
   }
   
   private List<String> evalEscapes(String key, ConfigurationSection section, int i) {
      String str = section.getString(key, "");
      if(str.equals("players"))
         return onlinePlayers();
      if(str.equals("args"))
         return cmd.args.getRaw().get(i);
      return Files.getField(section, key);
   }
   
   private String evalStatic(String str) {
      return str.startsWith(ph("static"))
        ? str.substring(ph("static").length())
        : str.startsWith("\\" + ph("static"))
        ? str.substring(1)
        : str;
   }
   
   private static String str(Object obj) {
      return obj.toString();
   }
   
   public static List<String> onlinePlayers() {
      List<String> out = new ArrayList<>();
      for(Player player : Bukkit.getOnlinePlayers())
         out.add(player.getName());
      return out;
   }
}
