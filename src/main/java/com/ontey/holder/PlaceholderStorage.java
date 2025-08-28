package com.ontey.holder;

import com.ontey.Main;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.List;

import static com.ontey.holder.Placeholders.*;

class PlaceholderStorage {
   
   static List<Placeholder> getPlayerPlaceholders(Player p) {
      List<Placeholder> out = new ArrayList<>();
      out.add(ph("player.isOp", p.isOp()));
      out.add(ph("player.isSleeping", p.isSleeping()));
      out.add(ph("player.hasEmptyInventory", p.getInventory().isEmpty()));
      
      out.add(ph("player.xp-levels", p.getLevel()));
      out.add(ph("player.xp-cooldown", p.getExpCooldown()));
      out.add(ph("player.xp-to-level", p.getExpToLevel()));
      out.add(ph("player.xp-total", p.getTotalExperience()));
      out.add(ph("player.xp", p.getExp()));
      out.add(ph("player.health", p.getHealth()));
      //noinspection DataFlowIssue
      out.add(ph("player.max-health", safe(() -> p.getAttribute(Attribute.MAX_HEALTH).getValue())));
      out.add(ph("player.uuid", p.getUniqueId().toString()));
      out.add(ph("player.saturation", p.getSaturation()));
      out.add(ph("player.exhaustion", p.getExhaustion()));
      out.add(ph("player.world", p.getWorld().getName()));
      out.add(ph("player.biome", p.getWorld().getBiome(p.getLocation()).toString()));
      
      out.add(ph("player.flySpeed", p.getFlySpeed()));
      out.add(ph("player.flyingSpeed", p.getFlySpeed()));
      out.add(ph("player.walkSpeed", p.getWalkSpeed()));
      out.add(ph("player.walkingSpeed", p.getWalkSpeed()));
      out.add(ph("player.world-uuid", p.getWorld().getUID().toString()));
      
      out.add(ph("player.x-precise", p.getX()));
      out.add(ph("player.y-precise", p.getY()));
      out.add(ph("player.z-precise", p.getZ()));
      out.add(ph("player.x", (int) p.getX()));
      out.add(ph("player.y", (int) p.getY()));
      out.add(ph("player.z", (int) p.getZ()));
      
      Block target = p.getTargetBlockExact(100);
      if(target != null) {
         out.add(ph("player.target-x", target.getX()));
         out.add(ph("player.target-y", target.getY()));
         out.add(ph("player.target-z", target.getZ()));
         out.add(ph("player.target-name", target.getType().name()));
      } else { // null
         out.add(ph("player.target-x", "none"));
         out.add(ph("player.target-y", "none"));
         out.add(ph("player.target-z", "none"));
         out.add(ph("player.target-name", "AIR"));
      }
      
      out.add(ph("player.pitch", p.getPitch()));
      out.add(ph("player.yaw", p.getYaw()));
      out.add(ph("player.body-yaw", p.getBodyYaw()));
      out.add(ph("player.direction", getDirection(p.getYaw())));
      out.add(ph("player", p.getName()));
      
      out.add(ph("sender.isPlayer", "true"));
      out.add(ph("sender.isConsole", "false"));
      out.add(ph("sender.isCommandBlock", "false"));
      out.add(ph("players-online-count", Bukkit.getOnlinePlayers().size()));
      out.add(ph("players-online", onlinePlayers(", ")));
      out.add(ph("server-ip", p.getServer().getIp()));
      out.add(ph("server", p.getServer().getName()));
      out.add(ph("can-replace-papi", Main.papi));
      
      addCommonPlaceholders(out);
      
      return out;
   }
   
   static List<Placeholder> getConsolePlaceholders(CommandSender sender) {
      List<Placeholder> out = new ArrayList<>();
      out.add(ph("player.isOp", "true"));
      out.add(ph("server-ip", sender.getServer().getIp()));
      out.add(ph("server", sender.getServer().getName()));
      out.add(ph("sender.isPlayer", "false"));
      out.add(ph("sender.isCommandBlock", sender instanceof CommandBlock));
      out.add(ph("sender.isConsole", sender instanceof ConsoleCommandSender));
      out.add(ph("player", "console"));
      out.add(ph("can-replace-papi", "false"));
      addCommonPlaceholders(out);
      return out;
   }
   
   static void addCommonPlaceholders(List<Placeholder> out) {
      out.add(ph("char.space", " "));
      out.add(ph("char.circumflex", "^"));
      out.add(ph("char.degrees", "°"));
      out.add(ph("char.tab", "\t"));
      out.add(ph("char.backslash", "\\"));
   }
}
