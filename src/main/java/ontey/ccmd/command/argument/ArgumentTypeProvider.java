package ontey.ccmd.command.argument;

import com.mojang.brigadier.arguments.*;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import ontey.api.config.ConfigSection;

import java.util.function.Function;
import java.util.function.Supplier;

@AllArgsConstructor
public enum ArgumentTypeProvider {
	WORD(StringArgumentType::word),
	STRING(StringArgumentType::string),
	VARARGS_STRING(StringArgumentType::greedyString),
	BOOLEAN(BoolArgumentType::bool),
	INTEGER(section -> {
		if(section == null)
			return IntegerArgumentType.integer();
		
		if(section.contains("min")) {
			int min = section.getInt("min", Integer.MIN_VALUE);
			
			if(section.contains("max"))
				return IntegerArgumentType.integer(min, section.getInt("max", Integer.MAX_VALUE));
			else
				return IntegerArgumentType.integer(min);
		} else {
			return IntegerArgumentType.integer();
		}
	}),
	LONG(section -> {
		if(section == null)
			return LongArgumentType.longArg();
		
		if(section.contains("min")) {
			long min = section.getLong("min", Long.MIN_VALUE);
			
			if(section.contains("max"))
				return LongArgumentType.longArg(min, section.getLong("max", Long.MAX_VALUE));
			else
				return LongArgumentType.longArg(min);
		} else {
			return LongArgumentType.longArg();
		}
	}),
	FLOAT(section -> {
		if(section == null)
			return FloatArgumentType.floatArg();
		
		if(section.contains("min")) {
			float min = (float) section.getDouble("min", Float.MIN_VALUE);
			
			if(section.contains("max"))
				return FloatArgumentType.floatArg(min, (float) section.getDouble("max", Float.MAX_VALUE));
			else
				return FloatArgumentType.floatArg(min);
		} else {
			return FloatArgumentType.floatArg();
		}
	}),
	DOUBLE(section -> {
		if(section == null)
			return DoubleArgumentType.doubleArg();
		
		if(section.contains("min")) {
			double min = section.getDouble("min", Double.MIN_VALUE);
			
			if(section.contains("max"))
				return DoubleArgumentType.doubleArg(min, section.getDouble("max", Double.MAX_VALUE));
			else
				return DoubleArgumentType.doubleArg(min);
		} else {
			return DoubleArgumentType.doubleArg();
		}
	}),
	ENTITY(ArgumentTypes::entity),
	ENTITIES(ArgumentTypes::entities),
	PLAYER(ArgumentTypes::player),
	PLAYERS(ArgumentTypes::players),
	PLAYER_PROFILES(ArgumentTypes::playerProfiles),
	BLOCK_POSITION(ArgumentTypes::blockPosition),
	COLUMN_BLOCK_POSITION(ArgumentTypes::columnBlockPosition),
	BLOCK_IN_WORLD_PREDICATE(ArgumentTypes::blockInWorldPredicate),
	FINE_POSITION(section -> {
		if(section == null)
			return ArgumentTypes.finePosition();
		else
			return ArgumentTypes.finePosition(section.getBoolean("center-integers", false));
	}),
	COLUMN_FINE_POSITION(section -> {
		if(section == null)
			return ArgumentTypes.columnFinePosition();
		else
			return ArgumentTypes.columnFinePosition(section.getBoolean("center-integers", false));
	}),
	ROTATION(ArgumentTypes::rotation),
	ANGLE(ArgumentTypes::angle),
	AXES(ArgumentTypes::axes),
	BLOCK_STATE(ArgumentTypes::blockState),
	ITEM_STACK(ArgumentTypes::itemStack),
	ITEM_PREDICATE(ArgumentTypes::itemPredicate),
	NAMED_COLOR(ArgumentTypes::namedColor),
	HEX_COLOR(ArgumentTypes::hexColor),
	COMPONENT(ArgumentTypes::component),
	STYLE(ArgumentTypes::style),
	SIGNED_MESSAGE(ArgumentTypes::signedMessage),
	SCOREBOARD_DISPLAY_SLOT(ArgumentTypes::scoreboardDisplaySlot),
	NAMESPACED_KEY(ArgumentTypes::namespacedKey),
	KEY(ArgumentTypes::key),
	INTEGER_RANGE(ArgumentTypes::integerRange),
	DOUBLE_RANGE(ArgumentTypes::doubleRange),
	WORLD(ArgumentTypes::world),
	GAME_MODE(ArgumentTypes::gameMode),
	HEIGHT_MAP(ArgumentTypes::heightMap),
	UUID(ArgumentTypes::uuid),
	OBJECTIVE_CRITERIA(ArgumentTypes::objectiveCriteria),
	ENTITY_ANCHOR(ArgumentTypes::entityAnchor),
	TIME(section -> ArgumentTypes.time(section.getInt("min-time", 0))),
	TEMPLATE_MIRROR(ArgumentTypes::templateMirror),
	TEMPLATE_ROTATION(ArgumentTypes::templateRotation),
	// too complex, would require a whole system
	//RESOURCE(section -> ),
	//RESOURCE_KEY(section -> ),
	CUSTOM(section -> {
		//TODO custom arguments
		throw new UnsupportedOperationException("Custom arguments are not yet supported");
	});
	
	@NonNull
	private final Function<ConfigSection, ArgumentType<?>> typeFunction;
	
	ArgumentTypeProvider(Supplier<ArgumentType<?>> typeSupplier) {
		this.typeFunction = _ -> typeSupplier.get();
	}
	
	@NonNull
	public ArgumentType<?> argumentType(@NonNull ConfigSection argumentSection) {
		return typeFunction.apply(argumentSection);
	}
}
