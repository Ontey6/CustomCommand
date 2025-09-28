package com.ontey.execution;

import com.ontey.files.Config;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConditionParser {
   
   public static boolean evalCommandConditions(Execution exe) {
      if(!ConditionParser.evalConditions(exe.cmd.conditions, exe)) {
         sendConditionErrorMessage(exe);
         return false;
      }
      return true;
   }
   
   private static void sendConditionErrorMessage(Execution exe) {
      if(exe.cmd.conditionErrorMessage.isEmpty())
         return;
      Execution.sendMessages(exe.cmd.conditionErrorMessage, exe.sender);
   }
   
   public static boolean evalConditions(List<String> conditions, Execution exe) {
      if (conditions.isEmpty())
         return true;
      for(String str : conditions)
         if(!evalCondition(str, exe))
            return false;
      return true;
   }
   
   public static boolean evalCondition(String str, Execution exe) {
      if(str == null || str.isBlank())
         return true;
      
      str = str.replace(" ", "");
      str = Replacement.replaceArgs(str, exe.args);
      str = Formattation.formattedMessage(str, exe);
      
      List<String> parts = splitOrParts(str);
      if(parts.size() > 1) {
         for(String part : parts)
            if(evalCondition(part, exe))
               return true;
         return false;
      }
      return evalSingle(parts.getFirst());
   }
   
   private static List<String> splitOrParts(String str) {
      List<String> parts = new ArrayList<>();
      int last = 0;
      for(int i = 0; i < str.length() - 1; i++) {
         if(str.charAt(i) == '|' && str.charAt(i + 1) == '|') {
            int bs = 0;
            for (int j = i - 1; j >= 0 && str.charAt(j) == '\\'; j--)
               bs++;
            if(bs % 2 == 0) {
               parts.add(str.substring(last, i));
               last = i + 2;
               i++;
            }
         }
      }
      parts.add(str.substring(last));
      return parts;
   }
   
   private static boolean evalSingle(String expr) {
      String[] ops = {"==", "=?=", "!=", "!?=", ">=", "<=", ">", "<"};
      for(String op : ops) {
         Pattern pattern = Pattern.compile(Pattern.quote(op));
         Matcher matcher = pattern.matcher(expr);
         
         while(matcher.find()) {
            int idx = matcher.start();
            int bs = 0;
            for (int i = idx - 1; i >= 0 && expr.charAt(i) == '\\'; i--)
               bs++;
            if(bs % 2 == 1)
               continue;
            return findAndCompare(expr, idx, op);
         }
      }
      return resolveInverted(expr.toLowerCase());
   }
   
   private static boolean resolveInverted(String expr) {
      if(expr.startsWith("\\!"))
         return Config.isTrue(expr.substring(1));
      if(expr.startsWith("!"))
         return !Config.isTrue(expr.substring(1));
      return Config.isTrue(expr);
   }
   
   private static boolean findAndCompare(String expr, int idx, String op) {
      String left = expr.substring(0, idx);
      String right = expr.substring(idx + op.length());
      
      left = left.replace("\\\\" + op, op);
      right = right.replace("\\\\" + op, op);
      
      return compare(left, right, op);
   }
   
   private static boolean compare(String left, String right, String op) {
      boolean isNumber = isNumeric(left) && isNumeric(right);
      
      if(isNumber) {
         double l = Double.parseDouble(left);
         double r = Double.parseDouble(right);
         return switch(op) {
            case "==", "=?=" -> l == r;
            case "!=", "!?=" -> l != r;
            case ">" -> l > r;
            case "<" -> l < r;
            case ">=" -> l >= r;
            case "<=" -> l <= r;
            default -> false;
         };
      }
      return switch(op) {
         case "==" -> left.equals(right);
         case "!=" -> !left.equals(right);
         case "=?=" -> left.equalsIgnoreCase(right);
         case "!?=" -> !left.equalsIgnoreCase(right);
         default -> false;
      };
   }
   
   static List<String> resolveConditions(List<String> commands, Execution exe) {
      if(commands.isEmpty())
         return commands;
      String prefix = Config.ph("condition");
      
      for(int i = 0; i < commands.size(); i++) {
         String line = commands.get(i);
         
         if(line.startsWith(prefix)) {
            int start = i;
            boolean allTrue = true;
            
            while(i < commands.size() && commands.get(i).startsWith(prefix)) {
               String condLine = commands.get(i).substring(prefix.length());
               boolean result = evalCondition(condLine, exe);
               
               commands.remove(i);
               if(!result)
                  allTrue = false;
            }
            
            if(!allTrue && i < commands.size())
               commands.remove(i);
            
            i = start - 1;
            continue;
         }
         
         if(line.startsWith("\\" + prefix))
            commands.set(i, line.substring(1));
      }
      return commands;
   }
   
   static String str(Object obj) {
      return obj == null ? "" : obj.toString();
   }
   
   private static boolean isNumeric(String str) {
      if(str == null)
         return false;
      return str.matches("[+-]?\\d+(\\.\\d+)?");
   }
}
