package ontey.ccmd.command.registry;

import lombok.NonNull;
import ontey.api.command.registry.RegistryCommand;
import ontey.api.config.yaml.file.YamlFile;
import ontey.ccmd.command.CustomCommand;
import ontey.ccmd.command.translator.CustomCommandTranslator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static ontey.ccmd.Main.plugin;

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
	
	public static void registerCustomCommands() {
		registeredCustomCommands.clear();
		registeredCommands.clear();
		
		Set<String> registeredCommandsCache = new HashSet<>();
		
		for(var cmd : getCommands()) {
			if(!registeredCommandsCache.add(cmd.getName())) {
				plugin.getLog().warn("The command '" + cmd.getName() + "' is a duplicate and will not be registered.");
				continue;
			}
			
			var registryCommand = cmd.buildCommand().build();
			plugin.registerCommand(registryCommand);
			
			registeredCommands.add(registryCommand);
			registeredCustomCommands.add(cmd);
		}
	}
	
	private static List<CustomCommand> getCommands() {
		List<CustomCommand> out = new ArrayList<>();
		
		for(File file : getCommandFiles())
			addCommands(file, out);
		
		return out;
	}
	
	public static File[] getCommandFiles() {
		File dir = new File(plugin.getDataFolder(), "commands");
		
		if(!dir.exists()) {
			if(!dir.mkdirs())
				plugin.getLogger().warning("Could not create commands directory");
			try {
				Files.copy(plugin.getResource("examples.yml"), dir.toPath().resolve("examples.yml"));
			} catch(IOException e) {
				plugin.getLog().warn("Couldn't create example commands file examples.yml");
				plugin.getFileLog().saveStackTrace(e);
			}
		}
		
		return getFiles(dir);
	}
	
	private static void addCommands(File file, List<CustomCommand> out) {
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
			plugin.getLog().error(
			  "+-+-+-+-+-+-+-+-+-+-+-+-CCMD-+-+-+-+-+-+-+-+-+-+-+-+-+",
			  "  Couldn't load the commands file named '" + file.getName() + "'.",
			  "  Look at the stack-trace below, so you can identify the error.",
			  "  There is probably a syntax error in the yml.",
			  "  Fix the error, then restart the server and it will work again.",
			  "  The plugin will just continue without this file.",
			  "+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+"
			);
			//noinspection CallToPrintStackTrace
			e.printStackTrace();
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
