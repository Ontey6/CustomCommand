package com.ontey.holder;

import org.bukkit.command.CommandSender;

public class ExecutionStateActionholder {
   
   public String name;
   
   public CommandSender sender;
   
   public String msg;
   
   public ExecutionStateActionholder(String name, CommandSender sender, String msg) {
      this.name = name;
      this.sender = sender;
      this.msg = msg;
   }
}
