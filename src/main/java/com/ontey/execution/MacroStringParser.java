package com.ontey.execution;

import com.ontey.files.Config;
import lombok.AllArgsConstructor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@AllArgsConstructor
public class MacroStringParser {
   
   public Execution exe;
   
   public String replaceMacroStrings(String str) {
      Pattern pattern = Pattern.compile("(?<!\\\\)\\$\\((.*?)(?<!\\\\)\\)");
      Matcher matcher = pattern.matcher(str);
      StringBuilder sb = new StringBuilder();
      
      while (matcher.find()) {
         String rep = evalMacroStrings(matcher.group(1));
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
   
   private String evalMacroStrings(String str) {
      // num op num
      if(str.replace(" ", "").matches("[+-]?\\d+(\\.\\d+)?[*/%+^-][+-]?\\d+(\\.\\d+)?"))
         return evalCalculation(str.replace(" ", ""));
      if(str.matches("(.*?)\\?(.*?):(.*?)"))
         return evalTernary(str);
      return ConditionParser.evalCondition(str, exe) + "";
   }
   
   private String evalTernary(String str) {
      int idx = str.indexOf('?');
      String condition = str.substring(0, idx);
      String _true = str.substring(idx + 1, str.indexOf(':', idx));
      String _false = str.substring(str.indexOf(':', idx) + 1);
      
      boolean result = ConditionParser.evalCondition(condition, exe);
      
      return result ? tnSpace(_true.trim()) : tnSpace(_false.trim());
   }
   
   private String evalCalculation(String str) {
      int idx = indexOfOp(str, "*/%+^-");
      if(idx == -1)
         return str;
      double left = Double.parseDouble(str.substring(0, idx));
      double right = Double.parseDouble(str.substring(idx + 1));
      char op = str.charAt(idx);
      
      return trimDecimal(calculate(left, right, op));
   }
   
   private String tnSpace(String str) {
      return str.replace(Config.ph("tn.space"), " ");
   }
   
   private double calculate(double left, double right, char op) {
      return switch(op) {
         case '+' -> left + right;
         case '*' -> left * right;
         case '-' -> left - right;
         case '/' -> left / right;
         case '%' -> left % right;
         case '^' -> Math.pow(left, right);
         default  -> throw new IllegalStateException("A number might be too high: " + left + op + right);
      };
   }
   
   // Should only be used for operators
   private int indexOfOp(String str, String chars) {
      // char at 0 has to be a digit -> no op there
      for(int i = 1; i < str.length(); i++) {
         char c = str.charAt(i);
         for(char aChar : chars.toCharArray())
            if(aChar == c)
               return i;
      }
      return -1;
   }
   
   private String trimDecimal(double d) {
      String str = String.valueOf(d);
      if(str.endsWith(".0"))
         return str.substring(0, str.length() - 2);
      return str;
   }
}
