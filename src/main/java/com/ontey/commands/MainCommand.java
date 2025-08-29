package com.ontey.commands;

import com.ontey.Main;
import com.ontey.reload.Reload;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class MainCommand implements TabExecutor {
   
   public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
      switch(args.length) {
         case 0: {
            sender.sendMessage("§bRunning CustomCommand v.§3" + Main.version);
            sender.sendMessage("§bSub-Commands:");
            sender.sendMessage("- §ereload");
            break;
         }
         case 1: {
            if(args[0].equals("reload"))
               Reload.reload(sender);
            break;
         }
         default: sender.sendMessage("§cUnrecognized Sub-Command");
      }
      return true;
   }
   
   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String [] args) {
      if(args.length == 1)
         return List.of("reload");
      return List.of();
   }
}