package com.ontey.types;

import com.ontey.CustomCommand;
import com.ontey.files.Config;
import com.ontey.files.Files;
import com.ontey.holder.Paths;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.net.*;
import java.util.ArrayList;
import java.util.List;

public class Args {
   
   public final YamlConfiguration config;
   
   public final String path;
   
   public Args(CustomCommand cmd) {
      this.config = cmd.config;
      this.path = Paths.args(cmd.name);
   }
   
   public int getLength() {
      int length = 0;
      while(config.isList(path + "." + (length + 1)) || config.isString(path + "." + (length + 1)))
         length++;
      return length;
   }
   
   public List<List<String>> getRaw() {
      List<List<String>> out = new ArrayList<>(getLength());
      int at = 1;
      
      while(config.isList(path + "." + at) || config.isString(path + "." + at)) {
         out.add(Files.getField(config, path + "." + at));
         at++;
      }
      
      return out;
   }
   
   public boolean checkArgs(CommandSender sender, String[] args) {
      //noinspection DataFlowIssue
      if(config.isList(path) && config.getList(path).isEmpty())
         return true;
      if(getLength() != args.length) {
         sender.sendMessage(Config.LANGUAGE.getArgsLengthError(args.length, getLength()));
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
         List<String> args = Files.getList(config, path + "." + (i + 1));
         return checkListedArg(sender, args, arg, i);
      }
      return checkTypedArg(sender, type, arg, i);
   }
   
   private boolean checkTypedArg(CommandSender sender, @Nullable TypedArgsType type, String arg, int i) {
      if(type == null)
         return false;
      if(type.matches(arg))
         return true;
      sender.sendMessage(Config.LANGUAGE.getTypedArgError(i, type.toString()));
      return false;
   }
   
   private boolean checkListedArg(CommandSender sender, List<String> args, String arg, int i) {
      if(args.isEmpty() || args.contains(arg))
         return true;
      sender.sendMessage(Config.LANGUAGE.getListedArgError(i + 1));
      if(Config.SEND_ARGS_SUGGESTIONS)
         for(String str : args)
            sender.sendMessage(Config.LANGUAGE.getListedArgSuggestionLine(str));
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
      DOUBLE,
      BOOLEAN,
      PLAYER,
      OFFLINE_PLAYER,
      SERVER_PLAYER;
      
      public static TypedArgsType getType(@Nullable String str) {
         if(str == null)
            return null;
         return switch(str.toLowerCase()) {
            case "str", "string", "any" -> STR;
            case "char", "character" -> CHAR;
            case "num", "number" -> NUM;
            case "int-num", "integer-number" -> INT_NUM;
            case "int", "integer" -> INT;
            case "decimal" -> DECIMAL;
            case "double" -> DOUBLE;
            case "bool", "boolean" -> BOOLEAN;
            case "player" -> PLAYER;
            case "offline-player", "off-player" -> OFFLINE_PLAYER;
            case "server-player" -> SERVER_PLAYER;
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
            case BOOLEAN -> str.equals("true") || str.equals("false");
            case PLAYER -> Bukkit.getPlayer(str) != null;
            case OFFLINE_PLAYER -> isValidOfflinePlayer(str);
            case SERVER_PLAYER -> isValidServerPlayer(str);
         };
      }
      
      private boolean test(Runnable run) {
         try {
            run.run();
            return true;
         } catch (Throwable e) {
            return false;
         }
      }
      
      private boolean isValidOfflinePlayer(String str) {
         OfflinePlayer player = Bukkit.getOfflinePlayer(str);
         if (player.hasPlayedBefore() || player.isOnline())
            return true;
         
         try {
            HttpURLConnection conn = (HttpURLConnection)
              new URI("https://api.mojang.com/users/profiles/minecraft/" + str).toURL().openConnection();
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            
            int code = conn.getResponseCode();
            return code == 200;
         } catch (Exception e) {
            return false;
         }
      }
      
      private boolean isValidServerPlayer(String str) {
         OfflinePlayer player = Bukkit.getOfflinePlayer(str);
         return player.hasPlayedBefore() || player.isOnline();
      }
   }
}
