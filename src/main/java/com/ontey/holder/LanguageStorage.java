package com.ontey.holder;

import com.ontey.files.Config;
import com.ontey.files.Files;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import static com.ontey.execution.Formattation.replaceMM;

public class LanguageStorage {
   public ConfigurationSection section;
   
   public LanguageStorage(ConfigurationSection section) {
      this.section = section;
   }
   
   public String COMMAND_CONDITION_ERROR;
   
   private String TYPED_ARG_ERROR;
   private String LISTED_ARG_ERROR;
   private String ARGS_LENGTH_ERROR;
   private String LISTED_ARG_SUGGESTION_LINE;
   
   public Component getTypedArgError(int arg, String type) {
      return replaceMM(TYPED_ARG_ERROR.replace("%arg", arg + "").replace("%type", type));
   }
   
   public Component getListedArgError(int arg) {
      return replaceMM(LISTED_ARG_ERROR.replace("%arg", arg + ""));
   }
   
   public Component getArgsLengthError(int length, int required) {
      return replaceMM(ARGS_LENGTH_ERROR.replace("%length", length + "").replace("%required", required + ""));
   }
   
   public Component getListedArgSuggestionLine(String suggestion) {
      return replaceMM(LISTED_ARG_SUGGESTION_LINE.replace("%suggestion", suggestion));
   }
   
   public void load() {
      COMMAND_CONDITION_ERROR = joined("command-condition-error", "&cCondition is not fulfilled, not running command");
      TYPED_ARG_ERROR = joined("typed-arg-error", "&cArgument %arg should be of type &e%type&c but is not!");
      LISTED_ARG_ERROR = joined("listed-arg-error", "Arg %arg is invalid. Valid args:");
      ARGS_LENGTH_ERROR = joined("args-length-error", "&cInvalid amount of arguments: &e%length&c. Should be &e%required");
      LISTED_ARG_SUGGESTION_LINE = joined("listed-args-suggestion-line", "- &e%suggestion");
   }
   
   private String joined(String path, String fallback) {
      return String.join("\n", Files.getField(section, path, Config.singletonList(fallback)));
   }
}