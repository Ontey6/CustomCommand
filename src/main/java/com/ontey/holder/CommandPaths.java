package com.ontey.holder;

import static com.ontey.files.Config.getOrDefault;

/* Absolutely not Written by AI */
/* Yeah, I like to enslave it */

public class CommandPaths {
   
   // Methods
   
   public static String message(String command) {
      return replaced(MESSAGE, command);
   }
   
   public static String broadcast(String command) {
      return replaced(BROADCAST, command);
   }
   
   public static String aliases(String command) {
      return replaced(ALIASES, command);
   }
   
   public static String description(String command) {
      return replaced(DESCRIPTION, command);
   }
   
   public static String usage(String command) {
      return replaced(USAGE, command);
   }
   
   public static String permission(String command) {
      return replaced(PERMISSION, command);
   }
   
   public static String permissionRequired(String command) {
      return replaced(PERMISSION_REQUIRED, command);
   }
   
   public static String args(String command) {
      return replaced(ARGS, command);
   }
   
   public static String tab(String command) {
      return replaced(TAB, command);
   }
   
   public static String commands(String command) {
      return replaced(COMMANDS, command);
   }
   
   public static String namespace(String command) {
      return replaced(NAMESPACE, command);
   }
   
   public static String noTab(String command) {
      return replaced(NO_TAB, command);
   }
   
   // Constants
   
   private static String MESSAGE;
   private static String BROADCAST;
   private static String ALIASES;
   private static String DESCRIPTION;
   private static String USAGE;
   private static String PERMISSION;
   private static String PERMISSION_REQUIRED;
   private static String ARGS;
   private static String TAB;
   private static String COMMANDS;
   private static String NAMESPACE;
   private static String NO_TAB;
   
   // Load
   
   public static void load() {
      MESSAGE = getOrDefault("paths.message", "%cmd.message");
      BROADCAST = getOrDefault("paths.broadcast", "%cmd.broadcast");
      ALIASES = getOrDefault("paths.aliases", "%cmd.aliases");
      DESCRIPTION = getOrDefault("paths.description", "%cmd.description");
      USAGE = getOrDefault("paths.usage", "%cmd.usage");
      PERMISSION = getOrDefault("paths.permission", "%cmd.permission");
      PERMISSION_REQUIRED = getOrDefault("paths.permission-required", "%cmd.permission-required");
      ARGS = getOrDefault("paths.args", "%cmd.args");
      TAB = getOrDefault("paths.tab", "%cmd.tab");
      COMMANDS = getOrDefault("paths.commands", "%cmd.commands");
      NAMESPACE = getOrDefault("paths.namespace", "%cmd.namespace");
      NO_TAB = getOrDefault("paths.no-tab", "%cmd.no-tab");
      
      AdvancedBroadcast.load();
   }
   
   // Advanced Broadcast
   
   public static class AdvancedBroadcast {
      // Methods
      
      public static String section(String command) {
         return CommandPaths.replaced(ADVANCED_BROADCAST, command);
      }
      
      public static String range(String command) {
         return replaced(RANGE, command);
      }
      
      public static String permission(String command) {
         return replaced(PERMISSION, command);
      }
      
      public static String condition(String command) {
         return replaced(CONDITION, command);
      }
      
      public static String broadcast(String command) {
         return replaced(BROADCAST, command);
      }
      
      // Constants
      
      private static String ADVANCED_BROADCAST;
      private static String RANGE;
      private static String PERMISSION;
      private static String CONDITION;
      private static String BROADCAST;
      
      // Load Advanced Broadcast
      
      private static void load() {
         ADVANCED_BROADCAST = getOrDefault("paths.advanced-broadcast", "%cmd.broadcast");
         RANGE = getOrDefault("paths.advanced-broadcast-range", "%abc.range");
         PERMISSION = getOrDefault("paths.advanced-broadcast-permission", "%abc.permission");
         CONDITION = getOrDefault("paths.advanced-broadcast-condition", "%abc.condition");
         BROADCAST = getOrDefault("paths.advanced-broadcast-broadcast", "%abc.broadcast");
      }
      
      // Helper
      
      private static String replaced(String str, String command) {
         return str.replace("%abc", section(command)).replace("%cmd", command);
      }
   }
   
   // Helper
   
   private static String replaced(String str, String command) {
      return str.replace("%cmd", command);
   }
}
