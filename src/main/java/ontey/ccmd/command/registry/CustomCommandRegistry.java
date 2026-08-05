package ontey.ccmd.command.registry;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import lombok.NonNull;
import ontey.api.command.registry.CommandRegistry;
import ontey.api.command.registry.RegistryCommand;
import ontey.api.config.yaml.file.YamlFile;
import ontey.api.filelog.FileLog;
import ontey.ccmd.command.CustomCommand;
import ontey.ccmd.command.translator.CustomCommandTranslator;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class CustomCommandRegistry {
	
	@NonNull // Not a set as CustomCommands are mutable
	private static final List<@NonNull CustomCommand> registeredCustomCommands = new ArrayList<>();
	
	@NonNull
	private static final Set<@NonNull RegistryCommand> registeredCommands = new HashSet<>();
	
	@NonNull
	public static List<@NonNull CustomCommand> getRegisteredCustomCommands() {
		return List.copyOf(registeredCustomCommands);
	}
	
	@NonNull
	public static Set<@NonNull RegistryCommand> getRegisteredCommands() {
		return Set.copyOf(registeredCommands);
	}
	
	public static void registerCustomCommands(BootstrapContext context) {
		try {
			registeredCustomCommands.clear();
			registeredCommands.clear();
			
			Set<String> registeredCommandsCache = new HashSet<>();
			
			CommandRegistry registry = new CommandRegistry(context.getLifecycleManager());
			FileLog fileLog = new FileLog("CustomCommand", context.getDataDirectory().toFile());
			
			for(var cmd : getCommands(context, fileLog)) {
				if(!registeredCommandsCache.add(cmd.getName())) {
					context.getLogger().warn("The command '{}' is a duplicate and will not be registered.", cmd.getName());
					continue;
				}
				
				var registryCommand = cmd.buildCommand().build();
				registry.register(registryCommand);
				
				registeredCommands.add(registryCommand);
				registeredCustomCommands.add(cmd);
			}
		} catch(Exception e) {
			context.getLogger().error("An unexpected exception occurred", e);
		}
	}
	
	private static List<CustomCommand> getCommands(BootstrapContext context, FileLog fileLog) {
		List<CustomCommand> out = new ArrayList<>();
		
		for(File file : getCommandFiles(context, fileLog))
			addCommands(file, out, context, fileLog);
		
		return out;
	}
	
	public static File[] getCommandFiles(BootstrapContext context, FileLog fileLog) {
		File dir = new File(context.getDataDirectory().toFile(), "commands");
		
		if(!dir.exists())
			createCommandsDirectoryAndExamples(dir, context, fileLog);
		
		return getFiles(dir);
	}
	
	private static void createCommandsDirectoryAndExamples(File dir, BootstrapContext context, FileLog fileLog) {
		if(!dir.mkdirs())
			context.getLogger().warn("Could not create commands directory");
		try {
			URL url = CustomCommandRegistry.class.getClassLoader().getResource("examples.yml");
			
			if(url == null) {
				context.getLogger().warn("Could not find examples.yml in the JAR, not copying it");
				return;
			}
			
			URLConnection connection = url.openConnection();
			connection.setUseCaches(false);
			
			Files.copy(connection.getInputStream(), dir.toPath().resolve("examples.yml"));
		} catch(IOException e) {
			context.getLogger().warn("Couldn't create example commands file examples.yml");
			fileLog.saveStackTrace(e);
		}
	}
	
	private static void addCommands(File file, List<CustomCommand> out, BootstrapContext context, FileLog fileLog) {
		try {
			var config = new YamlFile(file);
			
			config.load();
			
			for(String name : config.getKeys(false)) {
				if(name == null)
					continue;
				
				var section = config.getSection(name);
				
				if(section == null)
					continue;
				
				var cmd = CustomCommandTranslator.translateYaml(section);
				
				out.add(cmd);
			}
		} catch(Exception e) {
			context.getLogger().error("Couldn't load command file {}. The plugin will continue without this file.", file.getName());
			fileLog.saveStackTrace(e);
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
