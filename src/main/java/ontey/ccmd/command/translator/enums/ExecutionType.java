package ontey.ccmd.command.translator.enums;

import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import me.clip.placeholderapi.PlaceholderAPI;
import net.kyori.adventure.bossbar.BossBar;
import ontey.api.command.argument.Arg;
import ontey.api.command.registry.CommandRegistry;
import ontey.api.config.ConfigSection;
import ontey.api.javascript.JavaScriptException;
import ontey.api.javascript.Javascript;
import ontey.ccmd.Main;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.command.translator.enums.lambda.Addition;
import ontey.ccmd.format.Formatter;
import ontey.ccmd.util.JavascriptUtil;
import ontey.ccmd.util.TitleUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.Set;
import java.util.function.Function;

import static ontey.api.command.Command.SUCCESS;
import static ontey.ccmd.format.Formatter.format;
import static ontey.ccmd.util.JavascriptUtil.addContextToJavascript;

@AllArgsConstructor
public enum ExecutionType {
	JAVASCRIPT((builder, section, context) -> {
		String code = section.getString("javascript");
		
		if(code == null)
			throw context.newException("Specifies the JAVASCRIPT execution type, but doesn't specify the javascript String ('executes.javascript is not set')");
		
		Javascript javascript = JavascriptUtil.createBaseJavascript();
		
		builder.executes(ctx -> {
			addContextToJavascript(ctx, javascript);
			
			try {
				var function = (Function<Object[], Object>) javascript.eval(code);
				
				if(function == null)
					throw context.newException("Javascript execution could not be evaluated (They should return an arrow function that optionally returns an integer like '() => {...}' or '() => 1')");
				
				var returned = (Integer) function.apply(new Object[0]);
				
				return returned == null ? SUCCESS : returned;
			} catch(JavaScriptException e) {
				throw context.newException("A javascript error occurred", e);
			} catch(ClassCastException e) {
				throw context.newException("Javascript execution doesn't return the right type (They should return an arrow function that optionally returns an integer like '() => {...}'  or '() => 1')");
			}
		});
	}),
	JAVASCRIPT_REFERENCE((builder, section, context) -> {
	
	}),
	COMMANDS((builder, section, context) -> {
		if(!section.isList("commands"))
			throw context.newException("Specifies COMMANDS execution type, but doesn't specify the commands ('executes.message' is not set)");
		
		var commands = section.getStringList("commands");
		
		builder.executes(ctx -> {
			
			//TODO Add own placeholders to commands
			//TODO add own actions to commands when prefixing with '@' like '@javascript player.sendPlainMessage("Hello, World!")'
			for(var command : commands) {
				if(Main.isPlaceholderApiEnabled() && ctx.getSource().getSender() instanceof Player player)
					PlaceholderAPI.setPlaceholders(player, command);
				
				CommandRegistry.getCommandDispatcher().execute(command, ctx.getSource());
			}
			
			//TODO custom return values. Try to intercept minecraft's /return command (best solution). If not possible, add '@return <int>' action and possibility in javascript action.
			return SUCCESS;
		});
	}),
	MESSAGE((builder, section, context) -> {
		var message = section.getString("message");
		
		if(message == null)
			throw context.newException("Specifies MESSAGE execution type, but doesn't specify the message ('executes.message' is not set)");
		
		builder.executes(ctx -> {
			var sender = ctx.getSource().getSender();
			sender.sendMessage(format(message, sender));
			
			return SUCCESS;
		});
	}),
	BROADCAST((builder, section, context) -> {
		var broadcast = section.getString("broadcast");
		
		if(broadcast == null)
			throw context.newException("Specifies the BROADCAST execution type, but doesn't specify the broadcast ('executes.broadcast' is not set)");
		
		builder.executes(_ -> {
			for(var player : Bukkit.getOnlinePlayers())
				player.sendMessage(format(broadcast, player));
			
			return SUCCESS;
		});
	}),
	TITLE((builder, section, context) -> {
		var title = section.getString("title");
		var subtitle = section.getString("subtitle");
		var times = TitleUtil.getTimes(section, context);
		
		if(title == null)
			throw context.newException("No title specified ('argument.title' is not set)");
		
		builder.executes(ctx -> Arg.requirePlayer(ctx, player -> TitleUtil.showTitle(player, title, subtitle, times)));
	}),
	BROADCAST_TITLE((builder, section, context) -> {
		var title = section.getString("title");
		var subtitle = section.getString("subtitle");
		var times = TitleUtil.getTimes(section, context);
		
		if(title == null)
			throw context.newException("No title specified ('argument.title' is not set)");
		
		builder.executes(_ -> {
			for(var player : Bukkit.getOnlinePlayers())
				TitleUtil.showTitle(player, title, subtitle, times);
			
			return SUCCESS;
		});
	}),
	ACTIONBAR((builder, section, _) -> {
		builder.executes(ctx -> {
			if(!(ctx.getSource().getExecutor() instanceof Player player))
				throw new SimpleCommandExceptionType(new LiteralMessage("Not a player")).create();
			
			var actionbar = Formatter.format(section.getString("actionbar"), player);
			
			player.sendActionBar(actionbar);
			
			return SUCCESS;
		});
	}),
	BROADCAST_ACTIONBAR((builder, section, _) -> {
		builder.executes(_ -> {
			for(var player : Bukkit.getOnlinePlayers()) {
				var actionbar = Formatter.format(section.getString("actionbar"), player);
				
				player.sendActionBar(actionbar);
			}
			
			return SUCCESS;
		});
	}),
	BOSS_BAR((builder, section, context) -> {
		var bossBarMeta = createBossBarMeta(section, context);
		
		builder.executes(ctx -> {
			if(!(ctx.getSource().getExecutor() instanceof Player player))
				throw new SimpleCommandExceptionType(new LiteralMessage("Not a player")).create();
			
			bossBarMeta.showToPlayer(player);
			
			return SUCCESS;
		});
	}),
	BROADCAST_BOSS_BAR((builder, section, context) -> {
		var bossBarMeta = createBossBarMeta(section, context);
		
		builder.executes(_ -> {
			for(var player : Bukkit.getOnlinePlayers())
				bossBarMeta.showToPlayer(player);
			
			return SUCCESS;
		});
	});
	
	@Getter
	private final Addition action;
	
	public static void addExecution(ArgumentBuilder<CommandSourceStack, ?> builder, ConfigSection section, ParseContext context) {
		ExecutionType executionType = section.getEnum("type", ExecutionType.class);
		
		if(executionType == null)
			throw context.newException("Missing execution type ('executes.type' is not set)");
		
		executionType.getAction().addTo(builder, section, context);
	}
	
	private static BossBarMeta createBossBarMeta(ConfigSection section, ParseContext context) {
		var bossBarSection = section.getSection("boss-bar");
		
		if(bossBarSection == null)
			throw context.newException("No boss-bar declaration specified ('argument.boss-bar' is not set)");
		
		var rawName = bossBarSection.getString("name");
		
		if(rawName == null)
			throw context.newException("No boss-bar name specified");
		
		var progress = (float) bossBarSection.getDouble("progress");
		
		if(progress < 0.0f || progress > 1.0f)
			throw context.newException("Boss-bar progress is not between 0.0 and 1.0. Currently " + progress);
		
		BossBar.Color color = bossBarSection.getEnum("color", BossBar.Color.class);
		
		if(color == null)
			throw context.newException("No boss-bar color specified");
		
		BossBar.Overlay overlay = bossBarSection.getEnum("overlay", BossBar.Overlay.class, BossBar.Overlay.PROGRESS);
		Set<BossBar.Flag> flags = bossBarSection.getEnumSet("flags", BossBar.Flag.class);
		
		return new BossBarMeta(rawName, progress, color, overlay, flags);
	}
	
	private record BossBarMeta(String rawName, float progress, BossBar.Color color, BossBar.Overlay overlay, Set<BossBar.Flag> flags) {
		
		public void showToPlayer(Player player) {
			var name = Formatter.format(rawName, player);
			var bossBar = BossBar.bossBar(name, progress, color, overlay, flags);
			
			player.showBossBar(bossBar);
		}
	}
}
