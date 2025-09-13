package com.ontey.holder;

import com.ontey.CustomCommand;
import com.ontey.execution.Formattation;
import com.ontey.files.Commands;
import com.ontey.files.Config;
import com.ontey.log.Log;
import com.ontey.reload.Reload;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

public class ActionHolders {
   
   private static String ah(String str) {
      return Config.ah(str);
   }
   
   private static List<ActionHolder> actionholders(String[] args) {
      List<BiFunction<CommandSender, String, String>> out = new ArrayList<>();
      
      // send a message
      out.add((sender, msg) -> {
         if(!msg.startsWith(ah("msg")))
            return msg;
         msg = msg.substring(ah("msg").length());
         msg = Formattation.formatMessage(msg, sender, args);
         sender.sendMessage(msg);
         return "";
      });
      
      // send a broadcast
      out.add((sender, msg) -> {
         if(!msg.startsWith(ah("broadcast")))
            return msg;
         msg = msg.substring(ah("broadcast").length());
         msg = Formattation.formatMessage(msg, sender, args);
         for(Player p : Bukkit.getOnlinePlayers())
            p.sendMessage(msg);
         return "";
      });
      
      // reload config & all commands
      out.add((sender, msg) -> {
         if(!msg.equals(ah("reload")))
            return msg;
         Reload.reload(sender);
         return "";
      });
      
      // reload config
      out.add((sender, msg) -> {
         if(!msg.equals(ah("reload-config")))
            return msg;
         Reload.reloadConfig(sender);
         return "";
      });
      
      // reload all commands
      out.add((sender, msg) -> {
         if(!msg.equals(ah("reload-commands")))
            return msg;
         Reload.reloadCommands(sender);
         return "";
      });
      
      // reload single command
      out.add((sender, msg) -> {
         if(!msg.startsWith(ah("reload-command")))
            return msg;
         int len = ah("reload-command").length();
         if(len == msg.length())
            return "";
         msg = msg.substring(len);
         CustomCommand cmd = getCommand(msg);
         if(cmd == null) {
            if(sender.isOp())
               sender.sendMessage("actionholder reload-command couldn't find the specified command.\nChange it! '" + msg + "'");
            Log.info("actionholder reload-command couldn't find the specified command.\nChange it! '" + msg + "'");
            return "";
         }
         cmd.loadMutable(true);
         return "";
      });
      
      return convert(out, ActionHolder::new);
   }
   
   private static <T, R> List<R> convert(List<T> list, Function<T, R> converter) {
      List<R> out = new ArrayList<>();
      for(T t : list)
         out.add(converter.apply(t));
      return out;
   }
   
   private static CustomCommand getCommand(String name) {
      for(CustomCommand command : Commands.registeredCommands)
         if(command.name.equalsIgnoreCase(name))
            return command;
      return null;
   }
   
   public static String apply(CommandSender sender, String str, String[] args) {
      for(ActionHolder holder : actionholders(args))
         str = holder.apply(sender, str);
      return str;
   }
}
