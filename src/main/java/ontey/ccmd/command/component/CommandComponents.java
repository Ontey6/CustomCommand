package ontey.ccmd.command.component;

import com.mojang.brigadier.arguments.*;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import lombok.NonNull;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.key.KeyPattern;
import ontey.api.config.ConfigSection;
import ontey.ccmd.command.component.registry.ArgumentTypeRegistry;
import ontey.ccmd.command.context.ParseContext;
import ontey.ccmd.command.execution.Executions;
import ontey.ccmd.command.requirement.Requirements;
import ontey.ccmd.command.suggestion.Suggestions;

import static ontey.ccmd.command.component.registry.ArgumentTypeRegistry.register;

public final class CommandComponents {
	
	private CommandComponents() {
		throw new UnsupportedOperationException();
	}
	
	@SuppressWarnings("PatternValidation")
	public static CommandComponent parseCommandComponent(ParseContext context, ConfigSection baseSection) {
		var executesSection = baseSection.getSection("executes");
		var requiresSection = baseSection.getSection("requires");
		
		var sectionName = baseSection.getName();
		var execution = Executions.parseExecution(context.withSection("executes"), executesSection);
		var requirement = Requirements.parseRequirement(context.withSection("requires"), requiresSection);
		
		if(sectionName.startsWith("literal:") || context.isRoot()) {
			var name = context.isRoot() ? sectionName : sectionName.substring("literal:".length());
			
			return new LiteralCommandComponent(name, execution, requirement);
		} else if(sectionName.startsWith("argument:")) {
			var name = sectionName.substring("argument:".length());
			var suggestsSection = baseSection.getSection("suggests");
			var suggestions = Suggestions.parseSuggestions(context.withSection("suggests"), suggestsSection);
			
			var type = baseSection.getString("type");
			
			if(type == null)
				throw context.newException("Argument type 'type' not set");
			
			type = type
			  .toLowerCase()
			  .replace('-', '_')
			  .replace(' ', '_');
			
			if(!type.matches("(?:([a-z0-9_\\-.]+:)?|:)[a-z0-9_\\-./]+"))
				throw context.newException("Execution type doesn't match required pattern!");
			
			var key = type.contains(":")
			  ? Key.key(type)
			  : Key.key("ccmd", type);
			
			var creator = ArgumentTypeRegistry.get(key);
			
			if(creator == null)
				throw context.newException("Argument type '" + key.asString() + "' doesn't exist");
			
			var argumentSection = baseSection.getSection("argument");
			
			var argumentType = creator.createArgumentType(argumentSection);
			
			return new ArgumentCommandComponent(name, execution, requirement, suggestions, argumentType);
		} else {
			throw context.newException("Can't parse command section " + sectionName + " as it starts with neither 'literal:' nor 'argument:'");
		}
	}
	
	public static void registerDefaultArgumentTypes() {
		register(key("word"), StringArgumentType::word);
		register(key("string"), StringArgumentType::string);
		register(key("varargs_string"), StringArgumentType::greedyString);
		register(key("boolean"), BoolArgumentType::bool);
		register(key("integer"), section -> {
			if(section == null || !section.contains("min")) {
				return IntegerArgumentType.integer();
			} else {
				int min = section.getInt("min", Integer.MIN_VALUE);
				
				if(section.contains("max"))
					return IntegerArgumentType.integer(min, section.getInt("max", Integer.MAX_VALUE));
				else
					return IntegerArgumentType.integer(min);
			}
		});
		register(key("long"), section -> {
			if(section == null || !section.contains("min")) {
				return LongArgumentType.longArg();
			} else {
				long min = section.getLong("min", Long.MIN_VALUE);
				
				if(section.contains("max"))
					return LongArgumentType.longArg(min, section.getLong("max", Long.MAX_VALUE));
				else
					return LongArgumentType.longArg(min);
			}
		});
		register(key("float"), section -> {
			if(section == null || !section.contains("min")) {
				return FloatArgumentType.floatArg();
			} else {
				float min = section.getFloat("min", Float.MIN_VALUE);
				
				if(section.contains("max"))
					return FloatArgumentType.floatArg(min, section.getFloat("max", Float.MAX_VALUE));
				else
					return FloatArgumentType.floatArg(min);
			}
		});
		register(key("double"), section -> {
			if(section == null || !section.contains("min")) {
				return DoubleArgumentType.doubleArg();
			} else {
				double min = section.getDouble("min", Double.MIN_VALUE);
				
				if(section.contains("max"))
					return DoubleArgumentType.doubleArg(min, section.getDouble("max", Double.MAX_VALUE));
				else
					return DoubleArgumentType.doubleArg(min);
			}
		});
		register(key("entity"), ArgumentTypes::entity);
		register(key("entities"), ArgumentTypes::entities);
		register(key("player"), ArgumentTypes::player);
		register(key("players"), ArgumentTypes::players);
		register(key("player_profiles"), ArgumentTypes::playerProfiles);
		register(key("block_position"), ArgumentTypes::blockPosition);
		register(key("column_block_position"), ArgumentTypes::columnBlockPosition);
		register(key("block_in_world_predicate"), ArgumentTypes::blockInWorldPredicate);
		register(key("fine_position"), section -> {
			if(section == null)
				return ArgumentTypes.finePosition();
			else
				return ArgumentTypes.finePosition(section.getBoolean("center-integers", false));
		});
		register(key("column_fine_position"), section -> {
			if(section == null)
				return ArgumentTypes.columnFinePosition();
			else
				return ArgumentTypes.columnFinePosition(section.getBoolean("center-integers", false));
		});
		register(key("rotation"), ArgumentTypes::rotation);
		register(key("angle"), ArgumentTypes::angle);
		register(key("axes"), ArgumentTypes::axes);
		register(key("block_state"), ArgumentTypes::blockState);
		register(key("item_stack"), ArgumentTypes::itemStack);
		register(key("item_predicate"), ArgumentTypes::itemPredicate);
		register(key("named_color"), ArgumentTypes::namedColor);
		register(key("hex_color"), ArgumentTypes::hexColor);
		register(key("component"), ArgumentTypes::component);
		register(key("style"), ArgumentTypes::style);
		register(key("signed_message"), ArgumentTypes::signedMessage);
		register(key("scoreboard_display_slot"), ArgumentTypes::scoreboardDisplaySlot);
		register(key("namespaced_key"), ArgumentTypes::namespacedKey);
		register(key("key"), ArgumentTypes::key);
		register(key("integer_range"), ArgumentTypes::integerRange);
		register(key("double_range"), ArgumentTypes::doubleRange);
		register(key("world"), ArgumentTypes::world);
		register(key("game_mode"), ArgumentTypes::gameMode);
		register(key("height_map"), ArgumentTypes::heightMap);
		register(key("uuid"), ArgumentTypes::uuid);
		register(key("objective_criteria"), ArgumentTypes::objectiveCriteria);
		register(key("entity_anchor"), ArgumentTypes::entityAnchor);
		register(key("time"), section -> {
			if(section == null)
				return ArgumentTypes.time();
			else
				return ArgumentTypes.time(section.getInt("min-time", 0));
		});
		register(key("template_mirror"), ArgumentTypes::templateMirror);
		register(key("template_rotation"), ArgumentTypes::templateRotation);
	}
	
	@NonNull
	private static Key key(@NonNull @KeyPattern.Value String value) {
		return Key.key("ccmd", value);
	}
}
