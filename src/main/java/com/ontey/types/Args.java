package com.ontey.types;

import com.ontey.files.Commands;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class Args {
   
   private final YamlConfiguration config;
   
   private final String path;
   
   public Args(YamlConfiguration config, String command) {
      this.config = config;
      this.path = command + ".args";
   }
   
   public int getLength() {
      int length = 0;
      while (config.isList(path + "." + (length + 1)) || config.isString(path + "." + (length + 1)))
         length++;
      return length;
   }
   
   public boolean checkArgs(CommandSender sender, String[] args) {
      if(getLength() != args.length) {
         sender.sendMessage("§cInvalid amount of arguments: " + args.length + ". Should be " + getLength());
         return false;
      }
      for(int i = 0; i < args.length; i++)
         if(!checkArg(sender, args[i], i))
            return false;
      return true;
   }
   
   private boolean checkArg(CommandSender sender, String arg, int i) {
      TypedArgsType type = TypedArgsType.getType(config.getString(path + "." + (i + 1)));
      if(type == null) {
         List<String> args = Commands.getList(config, path + "." + (i + 1));
         return checkListedArg(sender, args, arg, i);
      }
      return checkTypedArg(sender, type, arg, i);
   }
   
   private boolean checkTypedArg(CommandSender sender, @Nullable TypedArgsType type, String arg, int i) {
      if(type == null)
         return false;
      if(type.matches(arg))
         return true;
      sender.sendMessage("§cArgument " + (i + 1) + " should be of type §e" + type + "§c, but is not");
      return false;
   }
   
   private boolean checkListedArg(CommandSender sender, List<String> args, String arg, int i) {
      if(args.isEmpty() || args.contains(arg))
         return true;
      sender.sendMessage("§cArgument " + (i + 1) + " is not allowed");
      sender.sendMessage("§eAllowed args:");
      sender.sendMessage("-§e " + String.join("§r\n-§e ", args));
      return false;
   }
   
   private String str(Object obj) {
      return obj == null ? "" : obj.toString();
   }
   
   public enum TypedArgsType {
      STR,
      CHAR,
      NUM,
      INT_NUM,
      INT,
      DECIMAL,
      DOUBLE;
      
      public static TypedArgsType getType(@Nullable String str) {
         if(str == null)
            return null;
         return switch(str.toLowerCase()) {
            case "str", "string" -> STR;
            case "char", "character" -> CHAR;
            case "num", "number" -> NUM;
            case "int-num", "integer-number" -> INT_NUM;
            case "int", "integer" -> INT;
            case "decimal" -> DECIMAL;
            case "double" -> DOUBLE;
            default -> null;
         };
      }
      
      @SuppressWarnings("ResultOfMethodCallIgnored")
      public boolean matches(String str) {
         if(str == null)
            return false;
         return switch(this) {
            case STR -> true;
            case CHAR -> str.length() == 1;
            case NUM -> str.matches("[+-]?\\d+(\\.\\d+)?");
            case INT_NUM -> str.matches("[+-]?\\d+");
            case INT -> test(() -> Integer.parseInt(str));
            case DECIMAL -> str.matches("[+-]?\\d+\\.\\d+");
            case DOUBLE -> test(() -> Double.parseDouble(str));
         };
      }
      
      private boolean test(Runnable run) {
         try {
            run.run();
            return true;
         } catch (Exception e) {
            return false;
         }
      }
   }
}
