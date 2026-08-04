package ontey.ccmd.command.translator.enums;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.*;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import ontey.api.config.ConfigSection;
import ontey.ccmd.command.argument.ArgumentTypeHolder;

import java.util.function.Function;

@AllArgsConstructor
public enum ArgumentPreset implements ArgumentTypeHolder {
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
	;
	
	@NonNull
	private final Function<ConfigSection, ArgumentType<?>> typeFunction;
	
	@NonNull
	public ArgumentType<?> argumentType(@NonNull ConfigSection section) {
		return typeFunction.apply(section);
	}
}
