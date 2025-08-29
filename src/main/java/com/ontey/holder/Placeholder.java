package com.ontey.holder;

import com.ontey.files.Config;

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
      return str.replace(format(), replacement);
   }
   
   public String format() {
      return Config.PLACEHOLDER_FORMAT.replace("%ph", placeholder);
   }
   
   public String replacement() {
      return replacement;
   }
}
