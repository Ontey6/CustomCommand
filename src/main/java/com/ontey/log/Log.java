package com.ontey.log;

import com.ontey.files.Config;
import java.util.logging.Logger;

public class Log {
   
   private static final Logger log = Logger.getLogger("Minecraft");
   
   public static void info(String message) {
      log.info(Config.PREFIX + " " + message);
   }
   
   public static void info(String... messages) {
      for(String msg : messages)
         info(msg);
   }
   
   public static void warning(String message) {
      log.warning(Config.PREFIX + " " + message);
   }
   
   public static void warning(String... messages) {
      for(String msg : messages)
         warning(msg);
   }
   
   public static void error(String message) {
      log.severe(Config.PREFIX + " " + message);
   }
   
   public static void error(String... messages) {
      for(String msg : messages)
         error(msg);
   }
}
