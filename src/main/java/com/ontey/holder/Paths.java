package com.ontey.holder;

import static com.ontey.files.Config.getOrDefault;

public class Paths {
   
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
   
   public static String command(String command) {
      return replaced(COMMAND, command);
   }
   
   public static String conditions(String command) {
      return replaced(CONDITIONS, command);
   }
   
   public static String urlCall(String command) {
      return replaced(URL_CALL, command);
   }
   
   public static String conditionErrorMessage(String command) {
      return replaced(CONDITION_ERROR_MESSAGE, command);
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
   private static String COMMAND;
   private static String CONDITIONS;
   private static String URL_CALL;
   private static String CONDITION_ERROR_MESSAGE;
   
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
      COMMAND = getOrDefault("paths.command", "%cmd.command");
      CONDITIONS = getOrDefault("paths.condition", "%cmd.condition");
      URL_CALL = getOrDefault("paths.url-call", "%cmd.url-call");
      CONDITION_ERROR_MESSAGE = getOrDefault("paths.condition-error", "%cmd.condition-error");
      
      AdvancedBroadcast.load();
      UrlCall.load();
   }
   
   // Advanced Broadcast
   
   public static class AdvancedBroadcast {
      // Methods
      
      public static String section(String command) {
         return Paths.replaced(ADVANCED_BROADCAST, command);
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
      
      public static String includeConsole(String command) {
         return replaced(INCLUDE_CONSOLE, command);
      }
      
      public static String includeSender(String command) {
         return replaced(INCLUDE_SENDER, command);
      }
      
      // Constants
      
      private static String ADVANCED_BROADCAST;
      private static String RANGE;
      private static String PERMISSION;
      private static String CONDITION;
      private static String BROADCAST;
      private static String INCLUDE_CONSOLE;
      private static String INCLUDE_SENDER;
      
      // Load Advanced Broadcast
      
      private static void load() {
         ADVANCED_BROADCAST = getOrDefault("paths.advanced-broadcast", "%cmd.broadcast");
         RANGE = getOrDefault("paths.advanced-broadcast-range", "%abc.range");
         PERMISSION = getOrDefault("paths.advanced-broadcast-permission", "%abc.permission");
         CONDITION = getOrDefault("paths.advanced-broadcast-condition", "%abc.condition");
         BROADCAST = getOrDefault("paths.advanced-broadcast-broadcast", "%abc.broadcast");
         INCLUDE_CONSOLE = getOrDefault("paths.advanced-broadcast-include-console", "%abc.include-console");
         INCLUDE_SENDER = getOrDefault("paths.advanced-broadcast-include-sender", "%abc.include-sender");
      }
      
      // Helper
      
      private static String replaced(String str, String command) {
         return str.replace("%abc", section(command)).replace("%cmd", command);
      }
   }
   
   public static class UrlCall {
      public static String section(String command, String name) {
         return Paths.replaced(URL_CALL + "." + name, command);
      }
      
      public static String url(String command, String name) {
         return replaced(URL, command, name);
      }
      
      public static String userAgent(String command, String name) {
         return replaced(USER_AGENT, command, name);
      }
      
      public static String jsonPath(String command, String name) {
         return replaced(JSON_PATH, command, name);
      }
      
      public static String after(String command, String name) {
         return replaced(AFTER, command, name);
      }
      
      private static String URL_CALL;
      private static String URL;
      private static String USER_AGENT;
      private static String JSON_PATH;
      private static String AFTER;
      
      private static void load() {
         URL_CALL = getOrDefault("paths.url-call", "%cmd.url-call");
         URL = getOrDefault("paths.url-call-url", "%url.url");
         USER_AGENT = getOrDefault("paths.url-call-user-agent", "%url.user-agent");
         JSON_PATH = getOrDefault("paths.url-call-json-path", "%url.json-path");
         AFTER = getOrDefault("paths.url-call-after", "%url.after");
      }
      
      private static String replaced(String str, String command, String name) {
         return Paths.replaced(str.replace("%url", section(command, name)), command);
      }
   }
   
   // Helper
   
   private static String replaced(String str, String command) {
      return str.replace("%cmd", command);
   }
}
