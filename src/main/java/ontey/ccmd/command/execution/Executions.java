package ontey.ccmd.command.execution;

import lombok.NonNull;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import ontey.api.config.ConfigSection;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.command.execution.registry.ExecutionRegistry;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ontey.ccmd.command.execution.registry.ExecutionRegistry.register;

public final class Executions {
	
	private Executions() {
		throw new UnsupportedOperationException();
	}
	
	@SuppressWarnings("PatternValidation")
	@Nullable
	public static Execution parseExecution(@NonNull ParseContext context, @Nullable ConfigSection executesSection) {
		if(executesSection == null)
			return null;
		
		String type = executesSection.getString("type");
		
		if(type == null)
			throw context.newException("Execution type 'executes.type' not set");
		
		type = type.toLowerCase().replace(' ', '_').replace('-', '_');
		
		if(!type.matches("(?:([a-z0-9_\\-.]+:)?|:)[a-z0-9_\\-./]+"))
			throw context.newException("Execution type doesn't match required pattern!");
		
		var key = type.contains(":")
		  ? Key.key(type)
		  : Key.key("ccmd", type);
		
		var creator = ExecutionRegistry.get(key);
		
		if(creator == null)
			throw context.newException("Execution type '" + key.asString() + "' doesn't exist");
		
		return creator.createExecution(context, executesSection);
	}
	
	public static void registerDefaultExecutions() {
		register(key("actionbar"), ((context, baseSection) -> {
			var section = baseSection.getSection("actionbar");
			
			if(section == null)
				throw context.newException("Section 'executes.actionbar' not set");
			
			context = context.withSection("actionbar");
			
			var actionbar = section.getString("actionbar");
			
			if(actionbar == null)
				throw context.newException("String 'actionbar.actionbar' not set");
			
			boolean broadcast = section.getBoolean("broadcast", false);
			
			return new ActionbarExecution(actionbar, broadcast);
		}));
		
		register(key("boss_bar"), (context, baseSection) -> {
			var section = baseSection.getSection("boss-bar");
			
			if(section == null)
				throw context.newException("Section 'executes.boss-bar' not set");
			
			context = context.withSection("boss-bar");
			
			var rawName = section.getString("name");
			
			if(rawName == null)
				throw context.newException("String 'name' not set");
			
			var progress = section.getFloat("progress", 1.0f);
			
			if(progress < 0.0f || progress > 1.0f)
				throw context.newException("Float 'progress' not between 0.0 and 1.0. Currently " + progress);
			
			BossBar.Color color = section.getEnum("color", BossBar.Color.class);
			
			if(color == null)
				throw context.newException("Boss-Bar color 'color' not set");
			
			BossBar.Overlay overlay = section.getEnum("overlay", BossBar.Overlay.class, BossBar.Overlay.PROGRESS);
			Set<BossBar.Flag> flags = new HashSet<>(section.getEnumList("flags", BossBar.Flag.class));
			long stayTicks = section.getLong("stay-ticks");
			boolean broadcast = section.getBoolean("broadcast", false);
			
			return new BossBarExecution(rawName, progress, color, overlay, flags, stayTicks, broadcast);
		});
		
		register(key("command"), (context, baseSection) -> {
			var section = baseSection.getSection("command");
			
			if(section == null)
				throw context.newException("Section 'command' not set");
			
			List<String> commands;
			
			if(section.isList("commands"))
				commands = section.getStringList("commands");
			else if(section.isString("command"))
				//noinspection DataFlowIssue
				commands = List.of(section.getString("command"));
			else
				commands = List.of();
			
			boolean runAsConsole = section.getBoolean("run-as-console", false);
			boolean broadcast = section.getBoolean("broadcast", false);
			
			return new CommandExecution(commands, runAsConsole, broadcast);
		});
		
		register(key("javascript"), (context, baseSection) -> {
			var javascript = baseSection.getString("javascript");
			
			if(javascript == null)
				throw context.newException("String 'javascript' not set");
			
			return new JavascriptExecution(javascript);
		});
		
		register(key("javascript_reference"), (context, baseSection) -> {
			var javascriptFile = baseSection.getString("javascript-file");
			
			if(javascriptFile == null)
				throw context.newException("String 'javascript-file' not set");
			
			return new JavascriptReferenceExecution(javascriptFile);
		});
		
		register(key("message"), (context, baseSection) -> {
			var section = baseSection.getSection("message");
			
			if(section == null)
				throw context.newException("Section 'message' not set");
			
			context = context.withSection("message");
			
			var message = section.getString("message");
			
			if(message == null)
				throw context.newException("String 'message' not set");
			
			boolean broadcast = section.getBoolean("broadcast", false);
			
			return new MessageExecution(message, broadcast);
		});
		
		register(key("title"), (context, baseSection) -> {
			var section = baseSection.getSection("title");
			
			if(section == null)
				throw context.newException("Section 'title' not set");
			
			String title = section.getString("title");
			
			if(title == null)
				throw context.newException("String 'title.title' not set");
			
			String subtitle = section.getString("subtitle");
			
			var timesSection = section.getSection("times");
			
			TitleExecution.StringTimes times = TitleExecution.StringTimes.deserialize(timesSection == null ? Map.of() : timesSection.getValues(false));
			
			var broadcast = section.getBoolean("broadcast", false);
			
			return new TitleExecution(title, subtitle, times, broadcast);
		});
	}
	
	@NonNull
	private static Key key(@NonNull @KeyPattern.Value String value) {
		return Key.key("ccmd", value);
	}
}
