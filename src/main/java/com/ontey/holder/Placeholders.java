package com.ontey.holder;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.function.Supplier;

public class Placeholders {
   
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
   
   static String onlinePlayers(String delimiter) {
      StringBuilder out = new StringBuilder();
      for (Player player : Bukkit.getOnlinePlayers())
         out.append(delimiter).append(player.getName());
      
      return out.isEmpty() ? "" : out.substring(delimiter.length());
   }
   
   static Placeholder ph(String placeholder, Object replacement) {
      return new Placeholder(placeholder, String.valueOf(replacement));
   }
}
