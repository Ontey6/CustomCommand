package com.ontey.files;

import com.ontey.Main;
import com.ontey.holder.LanguageStorage;
import com.ontey.holder.Paths;
import com.ontey.log.Log;
import com.ontey.tab.Tab;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Config {
   
   public static File file;
   
   public static YamlConfiguration config;
   
   private Config() { }
   
   public static void load() {
      Main main = Main.instance;
      file = new File(main.getDataFolder(), "config.yml");
      
      if(!file.exists())
         main.saveResource("config.yml", false);
      
      config = new YamlConfiguration();
      config.options().parseComments(true);
      
      try {
         config.load(file);
      } catch(Exception e) {
         Log.error(
           "+-+-+-+-+-+-+-+-+-+-+-+-CCMD-+-+-+-+-+-+-+-+-+-+-+-+-+",
           "  Couldn't load the config file.",
           "  Look at the stack-trace below, so you can identify the error.",
           "  There is probably a syntax error in the yml.",
           "  Fix the error, then restart the server and it will work again.",
           "+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+"
         );
         //noinspection CallToPrintStackTrace
         e.printStackTrace();
         Main.disablePlugin();
         return;
      }
      
      loadConstants();
      Paths.load();
   }
   
   public static void save() {
      try {
         config.save(file);
      } catch(Exception e) {
         Log.error(
           "+-+-+-+-+-+-+-+-+-+-+-+-CCMD-+-+-+-+-+-+-+-+-+-+-+-+-+",
           "  Couldn't save the config file.",
           "  If the file doesn't exist anymore, restart the server",
           "+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+"
         );
         //noinspection CallToPrintStackTrace
         e.printStackTrace();
      }
   }
   
   public static void set(String path, Object value) {
      config.set(path, value);
      save();
   }
   
   // Get
   
   public static Object get(String path) {
      return config.get(path);
   }
   
   public static String getString(String path) {
      if(!isString(path))
         return null;
      return config.getString(path);
   }
   
   // Typechecks
   
   public static boolean isString(String path) {
      return config.isString(path);
   }
   
   // Helpers
   
   public static <T> T getOrDefault(String path, T fallback) {
      if(get(path) == null)
         return fallback;
      try {
         // noinspection unchecked
         return (T) config.get(path);
      } catch(ClassCastException e) {
         return fallback;
      }
   }
   
   public static <T> List<T> singletonList(T t) {
      List<T> out = new ArrayList<>();
      out.add(t);
      return out;
   }
   
   // Config-specific methods
   
   public static String ph(String str) {
      if(!isString("placeholder-names." + str))
         return PLACEHOLDER_FORMAT.replace("%ph", str);
      //noinspection DataFlowIssue
      return PLACEHOLDER_FORMAT.replace("%ph", getString("placeholder-names." + str));
   }
   
   @Deprecated
   public static String ah(String str) {
      if(!isString("actionholder-names." + str))
         return PLACEHOLDER_FORMAT.replace("%ah", str);
      //noinspection DataFlowIssue
      return PLACEHOLDER_FORMAT.replace("%ah", getString("actionholder-names." + str));
   }
   
   public static boolean isTrue(String str) {
      return BOOLEAN_TRUE.contains(str);
   }
   
   public static String defaultPerm(String command) {
      return DEFAULT_PERMISSION.replace("%cmd", command.replaceAll("\\s+", "-"));
   }
   
   public static NoTab getNoTab(String str) {
      if(str == null)
         return null;
      return switch(str.toLowerCase()) {
         case "none" -> NoTab.NONE;
         case "players" -> NoTab.PLAYERS;
         default -> null;
      };
   }
   
   public static List<String> noTab() {
      Config.NoTab noTabMode = getNoTab(config.getString("defaults.no-tab"));
      return switch(noTabMode) {
         case NONE -> new ArrayList<>(0);
         case PLAYERS -> Tab.onlinePlayers();
         case null -> Files.getField(config, "defaults.no-tab");
      };
   }
   
   // Constants
   
   public static void loadConstants() {
      PREFIX = getOrDefault("format.prefix", "[CCMD]");
      PLACEHOLDER_FORMAT = getOrDefault("format.placeholder-format", "<%ph>");
      ACTIONHOLDER_FORMAT = getOrDefault("format.actionholder-format", "<!%ah>");
      BOOLEAN_TRUE = getOrDefault("boolean-true", new ArrayList<>(List.of("true", "yes")));
      HOTSWAP = getOrDefault("dev.hotswap", false);
      DEFAULT_PERMISSION = getOrDefault("defaults.permission", "ccmd.command.%cmd");
      DEFAULT_USAGE = Files.getField(config, "defaults.usage", singletonList("/<command>"));
      DEFAULT_DESCRIPTION = Files.getField(config, "defaults.description", singletonList("Server Command"));
      DEFAULT_NAMESPACE = getOrDefault("defaults.namespace", "customcommand");
      REMOVE_NAMESPACED_PLUGIN_COMMANDS = getOrDefault("tab.remove-namespaced-plugin-commands", false);
      REMOVE_NAMESPACED_COMMANDS = getOrDefault("tab.remove-namespaced-commands", false);
      REMOVE_COLORS_IN_CONSOLE = getOrDefault("remove-colors-in-console", false);
      SEND_ARGS_SUGGESTIONS = getOrDefault("format.send-args-suggestions", true);
      UPDATER = getOrDefault("updater", true);
      LANGUAGE = new LanguageStorage(config.getConfigurationSection("language"));
      LANGUAGE.load();
   }
   
   public static String PREFIX;
   
   public static String PLACEHOLDER_FORMAT, ACTIONHOLDER_FORMAT;
   
   public static List<String> BOOLEAN_TRUE;
   
   public static String DEFAULT_PERMISSION, DEFAULT_NAMESPACE;
   
   public static List<String> DEFAULT_USAGE, DEFAULT_DESCRIPTION;
   
   public static boolean HOTSWAP;
   
   public static boolean REMOVE_NAMESPACED_PLUGIN_COMMANDS, REMOVE_NAMESPACED_COMMANDS;
   
   public static boolean REMOVE_COLORS_IN_CONSOLE, SEND_ARGS_SUGGESTIONS, UPDATER;
   
   public static LanguageStorage LANGUAGE;
   
   public enum NoTab { NONE, PLAYERS }
}
