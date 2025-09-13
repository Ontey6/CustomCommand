package com.ontey.holder;

import com.ontey.files.Config;

import java.util.regex.Pattern;

public class Placeholder {
   
   // Fields
   
   private final String placeholder;
   
   private final String replacement;
   
   // Constructor
   
   Placeholder(String placeholder, String replacement) {
      this.placeholder = placeholder;
      this.replacement = replacement;
   }
   
   // methods
   
   public String apply(String str) {
      str = str.replaceAll("(?<!\\\\)" + Pattern.quote(format()), replacement);
      return str.replace("\\" + format(), format());
   }
   
   public String format() {
      return Config.ph(placeholder);
   }
   
   public String replacement() {
      return replacement;
   }
}
