package ontey.ccmd.command.execution;

import com.mojang.brigadier.Command;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.NonNull;
import ontey.api.command.argument.Arg;
import ontey.api.serialization.CombinedConfigSerializable;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.util.TitleUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

import static ontey.api.command.Command.SUCCESS;

public record TitleExecution(@NonNull String title, @Nullable String subtitle, @NonNull StringTimes stringTimes,
                             boolean broadcast) implements Execution {
	
	@Override
	public @NonNull Command<CommandSourceStack> parseExecution(@NonNull ParseContext context) {
		var times = TitleUtil.getTimes(stringTimes.fadeIn, stringTimes.stay, stringTimes.fadeOut, context);
		
		return ctx -> {
			if(broadcast) {
				for(var player : Bukkit.getOnlinePlayers())
					TitleUtil.showTitle(player, title, subtitle, times);
			} else {
				if(!(ctx.getSource().getExecutor() instanceof Player player))
					throw Arg.simpleException("Can't show title to non-player entity or console");
				
				TitleUtil.showTitle(player, title, subtitle, times);
			}
			
			return SUCCESS;
		};
	}
	
	@Override
	public @NotNull Map<String, Object> serialize() {
		var titleMap = new java.util.HashMap<>(Map.of(
		  "title", title,
		  "times", stringTimes.serialize(),
		  "broadcast", broadcast
		));
		
		if(subtitle != null)
			titleMap.put("subtitle", subtitle);
		
		return Map.of(
		  "type", "title",
		  "title", titleMap
		);
	}
	
	public record StringTimes(String fadeIn, String stay, String fadeOut) implements CombinedConfigSerializable {
		
		@NonNull
		public static StringTimes deserialize(@NonNull Map<String, Object> map) {
			var fadeIn = map.get("fade-in");
			var stay = map.get("stay");
			var fadeOut = map.get("fade-out");
			
			return new StringTimes(
			  fadeIn == null ? "10t" : fadeIn.toString(),
			  stay == null ? "70t" : stay.toString(),
			  fadeOut == null ? "20t" : fadeOut.toString()
			);
		}
		
		@Override
		public @NotNull Map<String, Object> serialize() {
			return Map.of(
			  "fade-in", fadeIn,
			  "stay", stay,
			  "fade-out", fadeOut
			);
		}
	}
}
