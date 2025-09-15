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
         case 2: {
            if(args[0].equals("reload")) {
               if(args[1].equals("all"))
                  Reload.reload(sender);
               if(args[1].equals("commands"))
                  Reload.reloadCommands(sender);
               if(args[1].equals("config"))
                  Reload.reloadConfig(sender);
            }
            break;
         }
         default: sender.sendMessage("§cUnrecognized Sub-Command");
      }
      return true;
   }
   
   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String [] args) {
      return switch(args.length) {
         case 1 -> List.of("reload");
         case 2 -> List.of("all", "commands", "config");
         default -> List.of();
      };
   }
}