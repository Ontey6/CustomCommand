package com.ontey.execution;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MacroStrings {
   
   public static String replaceMacroStrings(String str) {
      // escapable with \ in front
      // ')' escapable
      Pattern pattern = Pattern.compile("(?<!\\\\)\\$\\((.*?)\\)");
      Matcher matcher = pattern.matcher(str);
      
      while (matcher.find()) {
         str = str.substring(0, matcher.start()) + evalMacroStrings(matcher.group(1)) + str.substring(matcher.end());
      }
      return removeEscapedMacroStrings(str);
   }
   
   private static String removeEscapedMacroStrings(String str) {
      return str.replaceAll("\\\\(\\$\\((.*?)\\))", "$1");
   }
   
   private static String evalMacroStrings(String str) {
      // num op num
      if(str.matches("[+-]?\\d+(\\.\\d+)?[*/%+^-][+-]?\\d+(\\.\\d+)?"))
         return evalCalculation(str.replace(" ", ""));
      return str;
   }
   
   private static String evalCalculation(String str) {
      int idx = index(str, new char[]{'+', '*', '-', '/', '%', '^'});
      if(idx < 0)
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
   
   private static int index(String str, char[] chars) {
      for(int i = 1; i < str.length(); i++) {
         char c = str.charAt(i);
         for(char aChar : chars)
            if(aChar == c)
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
