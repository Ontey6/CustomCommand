package com.ontey.execution;

import org.bukkit.command.CommandSender;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MacroStrings {
   
   public static String replaceMacroStrings(String str, CommandSender sender, String[] args) {
      Pattern pattern = Pattern.compile("(?<!\\\\)\\$\\((.*?)(?<!\\\\)\\)");
      Matcher matcher = pattern.matcher(str);
      StringBuilder sb = new StringBuilder();
      
      while (matcher.find()) {
         String rep = evalMacroStrings(matcher.group(1), sender, args);
         rep = rep == null ? "" : rep;
         rep = rep.replace("\\)", ")");
         matcher.appendReplacement(sb, Matcher.quoteReplacement(rep));
      }
      matcher.appendTail(sb);
      return removeEscapedMacroStrings(sb.toString());
   }
   
   private static String removeEscapedMacroStrings(String str) {
      return str.replaceAll("\\\\(\\$\\((.*?)\\))", "$1");
   }
   
   private static String evalMacroStrings(String str, CommandSender sender, String[] args) {
      // num op num
      if(str.replace(" ", "").matches("[+-]?\\d+(\\.\\d+)?[*/%+^-][+-]?\\d+(\\.\\d+)?"))
         return evalCalculation(str.replace(" ", ""));
      if(str.matches("(.*?)\\?(.*?):(.*?)"))
         return evalTernary(str, sender, args);
      return "$(" + str + ")";
   }
   
   private static String evalTernary(String str, CommandSender sender, String[] args) {
      int idx = str.indexOf('?');
      String condition = str.substring(0, idx);
      String _true = str.substring(idx + 1, str.indexOf(':', idx));
      String _false = str.substring(str.indexOf(':', idx));
      
      boolean result = Evaluation.evalCondition(condition, sender, args);
      
      return result ? _true.trim() : _false.trim();
   }
   
   private static String evalCalculation(String str) {
      int idx = index(str, "*/%+^-");
      if(idx == -1)
         return str;
      double left = Double.parseDouble(str.substring(0, idx));
      double right = Double.parseDouble(str.substring(idx + 1));
      char op = str.charAt(idx);
      
      return trim(calculate(left, right, op));
   }
   
   
   private static double calculate(double left, double right, char op) {
      return switch(op) {
         case '+' -> left + right;
         case '*' -> left * right;
         case '-' -> left - right;
         case '/' -> left / right;
         case '%' -> left % right;
         case '^' -> Math.pow(left, right);
         default -> throw new IllegalStateException("A number might be too high: " + left + op + right);
      };
   }
   
   private static int index(String str, String chars) {
      // char at 0 has to be a digit -> no op there
      for(int i = 1; i < str.length(); i++) {
         char c = str.charAt(i);
         for(char aChar : chars.toCharArray())
            if(aChar == c && str.charAt(i - 1) != '\\')
               return i;
      }
      return -1;
   }
   
   private static String trim(double d) {
      String str = String.valueOf(d);
      if(str.endsWith(".0"))
         return str.substring(0, str.length() - 2);
      return str;
   }
}
