package ontey.ccmd.command.translator.enums;

import com.mojang.brigadier.builder.ArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import lombok.AllArgsConstructor;
import lombok.Getter;
import ontey.api.command.argument.Arg;
import ontey.ccmd.command.translator.enums.lambda.ContextFunction;

@AllArgsConstructor
public enum CommandComponentType {
	LITERAL((_, _, context) -> Arg.literal(context.name())),
	ARGUMENT((_, section, context) -> {
		var argumentSection = section.getSection("argument");
		
		if(argumentSection == null)
			throw context.newException("No argument declaration was specified (The 'argument' section is missing)");
		
		com.mojang.brigadier.arguments.ArgumentType<?> argumentType;
		
		var type = argumentSection.getEnum("type", ArgumentTypeProvider.class);
		
		if(type == null)
			throw context.newException("Doesn't specify a type ('argument.type' is not set)");
		
		argumentType = type.argumentType(section);
		
		var argument = Arg.of(context.name(), argumentType);
		
		var suggestsSection = section.getSection("suggests");
		if(suggestsSection != null)
			SuggestionType.addSuggestions(argument, suggestsSection, context);
		
		return argument;
	});
	
	@Getter
	private final ContextFunction<ArgumentBuilder<CommandSourceStack, ?>> action;
}
