package com.ontey.holder;

import com.ontey.CustomCommand;
import org.bukkit.command.CommandSender;

import java.util.function.Consumer;
import java.util.function.Predicate;

import static com.ontey.files.Config.ah;

public class ActionHolder {
   
   private final String name;
   private final Predicate<String> matcher;
   private final Consumer<ExecutionStateActionholder> executor;
   
   public ActionHolder(String name, Predicate<String> matcher, Consumer<ExecutionStateActionholder> executor) {
      this.name = name;
      this.matcher = matcher;
      this.executor = executor;
   }
   
   public boolean matches(String input) {
      return matcher.test(input);
   }
   
   public void execute(CommandSender sender, String commandLine) {
      executor.accept(new ExecutionStateActionholder(name, sender, commandLine));
   }
   
   public static ActionHolder exact(String keyword, Consumer<ExecutionStateActionholder> exec) {
      return new ActionHolder(keyword, msg -> msg.equalsIgnoreCase(ah(keyword)), exec);
   }
   
   public static ActionHolder prefix(String keyword, Consumer<ExecutionStateActionholder> exec) {
      return new ActionHolder(keyword, msg -> msg.toLowerCase().startsWith(ah(keyword.toLowerCase())), exec);
   }
}