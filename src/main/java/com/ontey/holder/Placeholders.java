package com.ontey.holder;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.function.Supplier;

public class Placeholders {
   
   // get
   
   public static String apply(CommandSender sender, String str) {
      if (sender instanceof final Player p) {
         for(Placeholder ph : PlaceholderStorage.getPlayerPlaceholders(p))
            str = ph.apply(str);
         return str;
      }
      
      for(Placeholder ph : PlaceholderStorage.getConsolePlaceholders(sender))
         str = ph.apply(str);
      return str;
   }
   
   static Object safe(Supplier<Double> getter) {
      try {
         return getter.get();
      } catch (Exception e) {
         return "none";
      }
   }
   
   
   // stolen from emmerrei / ivanfromitaly
   // rewrote the massive if statement
   static String getDirection(float yaw) {
      String[] directions = {"West", "North West", "North", "North East", "East", "South East", "South", "South West"};
      double dir = (yaw - 90) % 360;
      if (dir < 0)
         dir += 360;
      
      int index = (int) ((dir + 22.5) / 45) % 8;
      return directions[index];
   }
   
   static String onlinePlayers(@SuppressWarnings("SameParameterValue") String delimiter) {
      StringBuilder out = new StringBuilder();
      
      for(Player player : Bukkit.getOnlinePlayers())
         out.append(delimiter).append(player.getName());
      
      return out.substring(delimiter.length());
   }
}
