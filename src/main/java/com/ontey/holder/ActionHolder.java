package com.ontey.holder;

import org.bukkit.command.CommandSender;

import java.util.function.BiFunction;

public class ActionHolder {
   
   // get:
   // the sender, the command line
   // return:
   // the new command line / if not made for that purpose, "".
   public BiFunction<CommandSender, String, String> action;
   
   ActionHolder(BiFunction<CommandSender, String, String> action) {
      this.action = action;
   }
   
   public String apply(CommandSender sender, String str) {
      return action.apply(sender, str);
   }
}