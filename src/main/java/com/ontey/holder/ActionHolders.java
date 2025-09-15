package com.ontey.holder;

import com.ontey.CustomCommand;
import com.ontey.execution.Formattation;
import com.ontey.files.Commands;
import com.ontey.reload.Reload;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

import static com.ontey.files.Config.ah;

public class ActionHolders {
   private static List<ActionHolder> actionHolders(String[] args) {
      List<ActionHolder> list = new ArrayList<>();
      
      list.add(ActionHolder.prefix("msg", state -> {
         String content = state.msg.substring(ah("msg").length());
         state.sender.sendMessage(Formattation.formatMessage(content, state.sender, args));
      }));
      
      list.add(ActionHolder.exact("reload", state -> Reload.reload(state.sender)));
      list.add(ActionHolder.exact("reload-commands", state -> Reload.reloadCommands(state.sender)));
      list.add(ActionHolder.exact("reload-config", state -> Reload.reloadConfig(state.sender)));
      
      list.add(ActionHolder.prefix("reload-command", state -> {
         String name = state.msg.substring("reload-command".length());
         CustomCommand cmd = getCommand(name);
         if (cmd != null) cmd.loadMutable(true);
      }));
      
      return list;
   }
   
   
   private static CustomCommand getCommand(String name) {
      for (CustomCommand command : Commands.registeredCommands)
         if (command.name.equalsIgnoreCase(name))
            return command;
      return null;
   }
   
   private static Player getPlayer(String name) {
      try {
         return Bukkit.getPlayer(name);
      } catch (Exception ignored) {
         return null;
      }
   }
   
   public static String apply(CommandSender sender, String str, String[] args) {
      for (ActionHolder holder : actionHolders(args))
         if (holder.matches(str)) {
            holder.execute(sender, str);
            return "";
         }
      return str;
   }
   
}
