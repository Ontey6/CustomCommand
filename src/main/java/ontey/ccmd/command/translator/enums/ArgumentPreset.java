package ontey.ccmd.command.translator.enums;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.arguments.ArgumentType;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import ontey.api.config.ConfigSection;

import java.util.function.Function;

@AllArgsConstructor
public enum ArgumentPreset {
	WORD(_ -> StringArgumentType.word()),
	STRING(_ -> StringArgumentType.string()),
	VARARGS_STRING(_ -> StringArgumentType.greedyString()),
	BOOLEAN(_ -> BoolArgumentType.bool()),
	INTEGER(section -> {
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
	ENTITY(_ -> ArgumentTypes.entity()),
	ENTITIES(_ -> ArgumentTypes.entities()),
	PLAYER(_ -> ArgumentTypes.player()),
	PLAYERS(_ -> ArgumentTypes.players()),
	PLAYER_PROFILES(_ -> ArgumentTypes.playerProfiles()),
	BLOCK_POSITION(_ -> ArgumentTypes.blockPosition()),
	COLUMN_BLOCK_POSITION(_ -> ArgumentTypes.columnBlockPosition()),
	BLOCK_IN_WORLD_PREDICATE(_ -> ArgumentTypes.blockInWorldPredicate()),
	FINE_POSITION(section -> ArgumentTypes.finePosition(section.getBoolean("center-integers", false))),
	COLUMN_FINE_POSITION(section -> ArgumentTypes.columnFinePosition(section.getBoolean("center-integers", false))),
	ROTATION(_ -> ArgumentTypes.rotation()),
	ANGLE(_ -> ArgumentTypes.angle()),
	AXES(_ -> ArgumentTypes.axes()),
	BLOCK_STATE(_ -> ArgumentTypes.blockState()),
	ITEM_STACK(_ -> ArgumentTypes.itemStack()),
	ITEM_PREDICATE(_ -> ArgumentTypes.itemPredicate()),
	NAMED_COLOR(_ -> ArgumentTypes.namedColor()),
	HEX_COLOR(_ -> ArgumentTypes.hexColor()),
	COMPONENT(_ -> ArgumentTypes.component()),
	STYLE(_ -> ArgumentTypes.style()),
	SIGNED_MESSAGE(_ -> ArgumentTypes.signedMessage()),
	SCOREBOARD_DISPLAY_SLOT(_ -> ArgumentTypes.scoreboardDisplaySlot()),
	NAMESPACED_KEY(_ -> ArgumentTypes.namespacedKey()),
	KEY(_ -> ArgumentTypes.key()),
	INTEGER_RANGE(_ -> ArgumentTypes.integerRange()),
	DOUBLE_RANGE(_ -> ArgumentTypes.doubleRange()),
	WORLD(_ -> ArgumentTypes.world()),
	GAME_MODE(_ -> ArgumentTypes.gameMode()),
	HEIGHT_MAP(_ -> ArgumentTypes.heightMap()),
	UUID(_ -> ArgumentTypes.uuid()),
	OBJECTIVE_CRITERIA(_ -> ArgumentTypes.objectiveCriteria()),
	ENTITY_ANCHOR(_ -> ArgumentTypes.entityAnchor()),
	TIME(section -> ArgumentTypes.time(section.getInt("min-time", 0))),
	TEMPLATE_MIRROR(_ -> ArgumentTypes.templateMirror()),
	TEMPLATE_ROTATION(_ -> ArgumentTypes.templateRotation()),
	// too complex, would require a whole system
	//RESOURCE(section -> ),
	//RESOURCE_KEY(section -> ),
	;
	
	@NonNull
	private final Function<ConfigSection, ArgumentType<?>> typeFunction;
	
	@NonNull
	public ArgumentType<?> argumentType(@NonNull ConfigSection section) {
		return typeFunction.apply(section);
	}
}
