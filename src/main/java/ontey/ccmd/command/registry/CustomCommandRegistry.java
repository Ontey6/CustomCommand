package ontey.ccmd.command.registry;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import lombok.Getter;
import lombok.NonNull;
import ontey.api.config.yaml.file.YamlFile;
import ontey.api.filelog.FileLog;
import ontey.ccmd.command.CommandSectionLike;
import ontey.ccmd.command.CustomCommand;
import ontey.ccmd.command.CustomCommandNode;
import ontey.ccmd.command.RootCustomCommandNode;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.command.data.CommandData;
import ontey.ccmd.command.exception.ParseException;
import ontey.ccmd.command.parser.CustomCommandParser;
import ontey.ccmd.plugincommand.CustomCommandCommand;
import ontey.ccmd.shared.SharedConstants;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ontey.ccmd.shared.SharedConstants.dataDirectory;
import static ontey.ccmd.shared.SharedConstants.logger;

public final class CustomCommandRegistry {
	
	@NonNull
	private static final List<CustomCommandNode> registeredCommands = new ArrayList<>();
	
	@Getter
	private static final RootCustomCommandNode rootNode = new RootCustomCommandNode();
	
	public static List<CustomCommandNode> getRegisteredCommandNodes() {
		return List.copyOf(registeredCommands);
	}
	
	public static List<CustomCommand> getRegisteredCommands() {
		return registeredCommands.stream().map(CustomCommandNode::getCustomCommand).toList();
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
				
				registeredCommands.add(root);
				rootNode.addChild(root);
				createData(cmd, cmd, null);
			}
			
			if(useNMS) { //TODO dependency injection via enum
				var commands = ((CraftServer) Bukkit.getServer()).getServer().getCommands();
				var dispatcher = commands.getDispatcher();
				var rootNode = dispatcher.getRoot();
				
				for(var command : registeredCommands)
					if(command.getCustomCommand().values().enabled())
						rootNode.addChild((LiteralCommandNode) command);
				
				rootNode.addChild((LiteralCommandNode) new CustomCommandCommand().build().root());
			} else {
				lifecycleManager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
					for(var command : registeredCommands) {
						var values = command.getCustomCommand().values();
						if(values.enabled()) {
							var description = values.description();
							var aliases = values.aliases();
							
							event.registrar().register(command, description, aliases);
						}
					}
					
					event.registrar().register(new CustomCommandCommand().build().root());
				});
			}
		} catch(ParseException e) {
			logger.error(e.getMessage());
			fileLog.saveStackTrace(e);
		} catch(Exception e) {
			logger.error("An unexpected exception occurred");
			fileLog.saveStackTrace(e);
		}
	}
	
	/// Recursively creates [CommandData] for the section
	///
	/// @param base The base section everything is relative to
	
	private static void createData(@NonNull CommandSectionLike base, @NonNull CustomCommand root, @Nullable CommandSectionLike parent) {
		base.data().setRoot(root);
		base.data().setParent(parent);
		
		for(var child : base.children())
			createData(child, root, base);
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
		List<CustomCommandNode> oldCommands = new ArrayList<>(registeredCommands.size()); //TODO add changes system to show what commands were changed
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
		File dir = dataDirectory.resolve("commands").toFile();
		
		if(!dir.exists())
			createCommandsDirectoryAndExamples(dir, fileLog);
		
		if(!dataDirectory.resolve("cooldowns.yml").toFile().exists()) {
			try {
				copyResource("cooldowns.yml", "cooldowns.yml");
			} catch(IOException e) {
				logger.warn("Couldn't create cooldowns.yml");
				fileLog.saveStackTrace(e);
			}
		}
		SharedConstants.cooldownStorage = new YamlFile(dataDirectory.resolve("cooldowns.yml").toFile());
		
		return getFiles(dir);
	}
	
	private static void createCommandsDirectoryAndExamples(File commandsDirectory, FileLog fileLog) {
		if(!commandsDirectory.mkdirs())
			throw new IllegalStateException("Could not create commands directory");
		
		try {
			copyResource("examples.yml", "commands/examples.yml");
			copyResource("message.js", "javascript/message.js");
		} catch(Exception e) {
			logger.warn("Couldn't create examples (examples.yml or message.js)");
			fileLog.saveStackTrace(e);
		}
	}
	
	private static void copyResource(String resourcePath, String outputPath) throws IOException {
		URL url = CustomCommandRegistry.class.getClassLoader().getResource(resourcePath);
		
		if(url == null)
			throw new FileNotFoundException("Could not find the " + resourcePath + " resource in the JAR, not copying it");
		
		URLConnection connection = url.openConnection();
		connection.setUseCaches(false);
		var resource = dataDirectory.resolve(outputPath);
		resource.getParent().toFile().mkdirs();
		
		if(resource.toFile().exists())
			throw new FileAlreadyExistsException("Target file for " + resourcePath + " already exists");
		
		Files.copy(connection.getInputStream(), resource);
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
