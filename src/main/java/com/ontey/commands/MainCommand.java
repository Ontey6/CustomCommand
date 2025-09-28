package com.ontey.commands;

import com.ontey.Main;
import com.ontey.reload.Reload;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
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
            if(isReload(args[0]))
               Reload.reload(sender);
            if(args[0].equals("wiki"))
               sender.sendMessage(Main.mm.deserialize("Wiki on <u><click:open_url:'https://github.com/Ontey6/CustomCommand/wiki/Getting-Started'>Github</click></u>"));
            break;
         }
         case 2: {
            if(isReload(args[0])) {
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
   
   public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
      return switch(args.length) {
         case 1 -> dynamic(args, "reload", "wiki");
         case 2 -> isReload(args[0]) ? dynamic(args, "all", "commands", "config") : List.of();
         default -> List.of();
      };
   }
   
   private static List<String> dynamic(String[] args, String... completions) {
      List<String> list = new ArrayList<>(Arrays.asList(completions));
      
      list.removeIf(str -> !str.startsWith(args[args.length - 1]));
      
      return list;
   }
   
   private static boolean isReload(String str) {
      return str.equals("reload") || str.equals("rel");
   }
}