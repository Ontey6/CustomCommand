package com.ontey;

import com.ontey.execution.ConditionParser;
import com.ontey.execution.Execution;
import com.ontey.execution.Formattation;
import com.ontey.files.Config;
import com.ontey.files.Files;
import com.ontey.holder.Paths;
import com.ontey.holder.PlaceholderStorage;
import com.ontey.log.Log;
import com.ontey.types.UrlCall;
import com.ontey.tab.Tab;
import com.ontey.types.AdvancedBroadcast;
import com.ontey.types.Args;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static com.ontey.files.Files.getField;

public class CustomCommand {
   
   public static final List<CustomCommand> registeredCommands = new ArrayList<>();
   
   // On command (mutable)
   
   public List<String> commands, messages, broadcasts, conditions, conditionErrorMessage;
   
   @Nullable
   public String permission, description, usage;
   
   @Nullable
   public AdvancedBroadcast advancedBroadcast;
   
   public Args args;
   
   public Tab tab;
   
   public List<UrlCall> urlCalls;
   
   public PlaceholderStorage storage;
   
   // On startup (immutable)
   
   public final String name, command, namespace;
   
   public final List<String> aliases;
   
   public final YamlConfiguration config;
   
   public final File file;
   
   public CustomCommand(YamlConfiguration config, File file, String name) {
      this.config = config;
      this.file = file;
      this.name = name;
      this.command = getCommandName(name);
      this.namespace = getNamespace();
      this.aliases = getAliases();
      load(false);
   }
   
   public void execute(CommandSender sender, String label, String[] args) {
      var exe = new Execution(this, sender, label, args);
      var formattation = new Formattation(exe);
      
      if(Config.HOTSWAP)
         load(true);
      
      formattation.formatCommand();
      
      if(
        !this.args.checkArgs(sender, args)
        || !ConditionParser.evalCommandConditions(exe)
      ) return;
      
      Log.info(this.args.getRaw().toString());
      
      exe.runCommands();
      exe.sendMessages();
      exe.sendBroadcasts();
      exe.sendAdvancedBroadcast();
      executeUrlCalls(exe);
   }
   
   public boolean load(boolean reload) {
      if(reload && !reloadFile())
          return false; // don't waste resources
      this.permission = getPermission();
      this.description = getDescription();
      this.usage = getUsage();
      this.messages = getMessages();
      this.broadcasts = getBroadcasts();
      this.advancedBroadcast = getAdvancedBroadcast();
      this.commands = getCommands();
      this.args = getArgs();
      this.tab = new Tab(this);
      this.conditions = getConditions();
      this.conditionErrorMessage = getConditionErrorMessage();
      this.storage = new PlaceholderStorage();
      this.urlCalls = getUrlCalls();
      return true;
   }
   
   private boolean reloadFile() {
      try {
         config.load(file);
         return true;
      }
      catch(Exception ignored) {
         return false;
      }
   }
   
   private void executeUrlCalls(Execution exe) {
      urlCalls.forEach(call -> call.execute(exe));
   }
   
   public String toString() {
      return name;
   }
   
   // Getters
   
   public String getCommandName(String sectionName) {
      String setName = config.getString(Paths.command(sectionName));
      if(setName != null)
         return setName;
      return sectionName;
   }
   
   public List<String> getMessages() {
      return getField(config, Paths.message(name));
   }
   
   public List<String> getBroadcasts() {
      return getField(config, Paths.broadcast(name));
   }
   
   public List<String> getCommands() {
      return getField(config, Paths.commands(name));
   }
   
   public List<String> getAliases() {
      return getField(config, Paths.aliases(name));
   }
   
   public Args getArgs() {
      return new Args(this);
   }
   
   @Nullable
   public String getPermission() {
      if (!requiresPermission())
         return null;
      String value = config.getString(Paths.permission(name));
      return value != null ? value : Config.defaultPerm(name);
   }
   
   private boolean requiresPermission() {
      return config.getBoolean(Paths.permissionRequired(name), true);
   }
   
   public String getDescription() {
      List<String> description = getField(config, Paths.description(name));
      if(description.isEmpty())
         description.addAll(Config.DEFAULT_DESCRIPTION);
      return String.join("\n", description);
   }
   
   public String getUsage() {
      List<String> usage = getField(config, Paths.usage(name));
      if(usage.isEmpty())
         usage.addAll(Config.DEFAULT_USAGE);
      return String.join("\n", usage);
   }
   
   public AdvancedBroadcast getAdvancedBroadcast() {
      String range = config.getString(Paths.AdvancedBroadcast.range(name), "-1");
      boolean includeConsole = config.getBoolean(Paths.AdvancedBroadcast.includeConsole(name), false);
      boolean includeSender = config.getBoolean(Paths.AdvancedBroadcast.includeSender(name), true);
      List<String> permission = getField(config, Paths.AdvancedBroadcast.permission(name));
      List<String> broadcast = getField(config, Paths.AdvancedBroadcast.broadcast(name));
      List<String> condition = getField(config, Paths.AdvancedBroadcast.condition(name));
      
      return new AdvancedBroadcast(range, permission, condition, broadcast, includeConsole, includeSender);
   }
   
   public String getNamespace() {
      return config.getString(Paths.namespace(name), Config.DEFAULT_NAMESPACE);
   }
   
   public List<String> getNoTab() {
      Config.NoTab noTabMode = Config.getNoTab(config.getString(Paths.noTab(name)));
      return switch (noTabMode) {
         case NONE -> new ArrayList<>(0);
         case PLAYERS -> Tab.onlinePlayers();
         case null -> Config.noTab();
      };
   }
   
   public List<String> getConditions() {
      return getField(config, Paths.conditions(name));
   }
   
   public List<String> getConditionErrorMessage() {
      return getField(
        config,
        Paths.conditionErrorMessage(name),
        Config.singletonList(Config.LANGUAGE.COMMAND_CONDITION_ERROR)
      );
   }
   
   public List<UrlCall> getUrlCalls() {
      List<ConfigurationSection> sections = Files.getSections(config, Paths.urlCall(name));
      List<UrlCall> out = new ArrayList<>();
      
      sections.forEach(
        s -> out.add(getUrlCall(s.getName()))
      );
      
      return out;
   }
   
   public UrlCall getUrlCall(String name) {
      String url = config.getString(Paths.UrlCall.url(this.name, name));
      String userAgent = config.getString(Paths.UrlCall.userAgent(this.name, name), "CustomCommandUser");
      String jsonPath = config.getString(Paths.UrlCall.jsonPath(this.name, name));
      return new UrlCall(this.name, config, file, name, url, userAgent, jsonPath);
   }
}
