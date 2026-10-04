package ontey.ccmd.shared;

import io.papermc.paper.plugin.configuration.PluginMeta;
import net.kyori.adventure.text.logger.slf4j.ComponentLogger;
import ontey.api.config.yaml.file.YamlFile;
import ontey.api.filelog.FileLog;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Path;

public class SharedConstants {
	
	public static PluginMeta pluginMeta;
	
	public static ComponentLogger logger;
	
	public static Path dataDirectory;
	
	public static Path pluginsDirectory;
	
	public static FileLog fileLog;
	
	@Nullable
	public static YamlFile config;
	
	public static YamlFile cooldownStorage;
}
