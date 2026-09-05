package ontey.ccmd.js;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.resolvers.AngleResolver;
import io.papermc.paper.command.brigadier.argument.resolvers.ArgumentResolver;

public record Arguments(CommandContext<CommandSourceStack> ctx) {
	
	public Object get(String name) throws CommandSyntaxException {
		var argument = ctx.getArgument(name, Object.class);
		return switch(argument) {
			case ArgumentResolver<?> resolver -> resolver.resolve(ctx.getSource());
			case AngleResolver resolver -> resolver.resolve(ctx.getSource());
			default -> argument;
		};
	}
	
	public float resolveAngle(String name, CommandSourceStack source) throws CommandSyntaxException {
		return ctx.getArgument(name, AngleResolver.class).resolve(source);
	}
	
	public float resolveAngle(String name) throws CommandSyntaxException {
		return resolveAngle(name, ctx.getSource());
	}
	
	public Object resolve(String name, CommandSourceStack source) throws CommandSyntaxException {
		return ctx.getArgument(name, ArgumentResolver.class).resolve(source);
	}
	
	public Object resolve(String name) throws CommandSyntaxException {
		return resolve(name, ctx.getSource());
	}
}
