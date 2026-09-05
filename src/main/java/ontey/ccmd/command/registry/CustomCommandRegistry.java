package ontey.ccmd.command.registry;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.NonNull;
import ontey.api.config.yaml.file.YamlFile;
import ontey.api.filelog.FileLog;
import ontey.ccmd.command.CustomCommand;
import ontey.ccmd.command.CustomCommandNode;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.command.exception.ParseException;
import ontey.ccmd.command.parser.CustomCommandParser;
import ontey.ccmd.plugincommand.CustomCommandCommand;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.*;
import java.util.*;

import static ontey.ccmd.shared.SharedConstants.dataDirectory;
import static ontey.ccmd.shared.SharedConstants.logger;

public final class CustomCommandRegistry {
	
	@NonNull
	private static final Map<@NonNull CustomCommand, @NonNull CustomCommandNode> registeredCommands = new HashMap<>();
	
	@NonNull
	public static Map<@NonNull CustomCommand, @NonNull CustomCommandNode> getRegisteredCommands() {
		return Map.copyOf(registeredCommands);
	}
	
	public static Set<CustomCommand> getRegisteredCustomCommands() {
		return Set.copyOf(registeredCommands.keySet());
	}
	
	public static List<CustomCommandNode> getRegisteredCommandNodes() {
		return List.copyOf(registeredCommands.values());
	}
	
	public static void registerCustomCommands(@Nullable LifecycleEventManager<?> lifecycleManager, boolean useNMS) {
		assert useNMS || lifecycleManager != null;
		
		registeredCommands.clear();
		
		Set<String> registeredCommandNameCache = new HashSet<>();
		
		FileLog fileLog = new FileLog(logger, dataDirectory.resolve("logs").toFile());
		
		try {
			for(var cmd : getCommands(fileLog)) {
				if(!registeredCommandNameCache.add(cmd.name())) {
					logger.warn("The command '{}' is a duplicate and will not be registered.", cmd.name());
					continue;
				}
				
				var parseContext = new ParseContext(cmd.name(), true);
				
				var root = cmd.build(parseContext);
				
				registeredCommands.put(cmd, root);
			}
			
			if(useNMS) {
				var commands = ((CraftServer) Bukkit.getServer()).getServer().getCommands();
				var dispatcher = commands.getDispatcher();
				var rootNode = dispatcher.getRoot();
				
				for(var command : registeredCommands.values())
					rootNode.addChild((LiteralCommandNode) command);
				
				rootNode.addChild((LiteralCommandNode) new CustomCommandCommand().build().root());
			} else {
				lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
					for(var command : registeredCommands.values())
						event.registrar().register(command);
					
					event.registrar().register(new CustomCommandCommand().build().root());
				});
			}
			
		} catch(ParseException e) {
			logger.error(e.getMessage());
			fileLog.saveStackTrace(e);
		} catch (Exception e) {
			logger.error("An unexpected exception occurred");
			fileLog.saveStackTrace(e);
		}
	}
	
	/// Reloads all commands.
	///
	/// All the steps it takes:
	/// - Unregister all custom commands (Remove all `CustomCommandNode`s from the root node)
	/// - Clear [#registeredCommands]
	/// - Parse the YAML and create `CustomCommand`s (see [#getCommands(FileLog)])
	/// - Register new nodes to the root node
	/// - Send packets to update clients
	///
	
	public static void reloadCommands() {
		List<CustomCommandNode> oldCommands = new ArrayList<>(registeredCommands.size());
		var commands = ((CraftServer) Bukkit.getServer()).getServer().getCommands();
		var dispatcher = commands.getDispatcher();
		var rootNode = dispatcher.getRoot();
		
		rootNode.getChildren().removeIf(node -> {
			if(CustomCommandNode.class.isAssignableFrom(node.getClass())) {
				oldCommands.add(CustomCommandNode.class.cast(node));
				return true;
			}
			return false;
		});
		
		registeredCommands.clear();
		
		registerCustomCommands(null, true);
		
		for(var player : Bukkit.getOnlinePlayers())
			commands.sendCommands(((CraftPlayer) player).getHandle());
	}
	
	private static List<CustomCommand> getCommands(FileLog fileLog) {
		List<CustomCommand> out = new ArrayList<>();
		
		for(File file : getCommandFiles(fileLog))
			addCommands(file, out, fileLog);
		
		return out;
	}
	
	public static File[] getCommandFiles(FileLog fileLog) {
		File dir = new File(dataDirectory.toFile(), "commands");
		
		if(!dir.exists())
			createCommandsDirectoryAndExamples(dir, fileLog);
		
		return getFiles(dir);
	}
	
	private static void createCommandsDirectoryAndExamples(File commandsDirectory, FileLog fileLog) {
		if(!commandsDirectory.mkdirs())
			throw new IllegalStateException("Could not create commands directory");
		
		try {
			copyExamples(commandsDirectory.toPath());
			dataDirectory.toFile().mkdir();
			copyMessageJavascript();
		} catch(Exception e) {
			logger.warn("Couldn't create examples (examples.yml or message.js)");
			fileLog.saveStackTrace(e);
		}
	}
	
	private static void copyExamples(Path targetDirectory) throws IOException {
		URL url = CustomCommandRegistry.class.getClassLoader().getResource("examples.yml");
		
		if(url == null)
			throw new FileNotFoundException("Could not find the " + "examples.yml" + " resource in the JAR, not copying it");
		
		URLConnection connection = url.openConnection();
		connection.setUseCaches(false);
		var resourcePath = targetDirectory.resolve("examples.yml");
		
		if(resourcePath.toFile().exists())
			throw new FileAlreadyExistsException("Target file for " + "examples.yml" + " already exists");
		
		Files.copy(connection.getInputStream(), resourcePath);
	}
	
	private static void copyMessageJavascript() throws IOException {
		URL url = CustomCommandRegistry.class.getClassLoader().getResource("javascript/message.js");
		
		if(url == null)
			throw new FileNotFoundException("Could not find the javascript/message.js resource in the JAR, not copying it");
		
		URLConnection connection = url.openConnection();
		connection.setUseCaches(false);
		var resourcePath = dataDirectory.resolve("javascript/message.js");
		
		dataDirectory.resolve("javascript").toFile().mkdir();
		
		if(resourcePath.toFile().exists())
			throw new FileAlreadyExistsException("Target file for javascript/message.js already exists");
		
		Files.copy(connection.getInputStream(), resourcePath);
	}
	
	private static void addCommands(File file, List<CustomCommand> out, FileLog fileLog) {
		
		var config = new YamlFile(file);
		
		try {
			config.loadWithComments();
		} catch(IOException e) {
			logger.error("Couldn't load command file {}. The plugin will continue without this file.", file.getName());
			fileLog.saveStackTrace(e);
		}
		
		for(String name : config.getKeys(false)) {
			if(name == null) {
				logger.warn("Encountered a null key in file '{}', skipping it. This is not normal behavior.", file.getName());
				continue;
			}
			
			var section = config.getSection(name);
			
			if(section == null) {
				logger.warn("Encountered a null section with key '{}' in file '{}', skipping it. This is not normal behavior.", name, file.getName());
				continue;
			}
			
			CustomCommand cmd;
			
			try {
				cmd = CustomCommandParser.parseYaml(section, file);
			} catch(ParseException e) {
				// DO NOT CHANGE 'this command' TO 'it'. IT CHANGES CONTEXT.
				logger.warn("Encountered an exception while parsing command '{}' in file '{}'. Skipping this command.", name, file.getName());
				fileLog.saveStackTrace(e);
				continue;
			}
			
			out.add(cmd);
		}
	}
	
	private static File[] getFiles(File dir) {
		File[] yamlFiles = dir.listFiles((_, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
		File[] dirs = dir.listFiles(File::isDirectory);
		
		if(dirs == null || dirs.length == 0)
			return yamlFiles != null ? yamlFiles : new File[0];
		
		File[] out = yamlFiles != null ? yamlFiles : new File[0];
		
		for(File directory : dirs)
			out = concat(out, getFiles(directory));
		
		return out;
	}
	
	private static File[] concat(File[] first, File[] second) {
		int len = first.length + second.length;
		File[] out = new File[len];
		
		System.arraycopy(first, 0, out, 0, first.length);
		System.arraycopy(second, 0, out, first.length, second.length);
		
		return out;
	}
}
