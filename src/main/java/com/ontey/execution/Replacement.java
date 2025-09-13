package com.ontey.execution;

import com.ontey.files.Config;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class Replacement {
   public static String replaceArgs(@NotNull String str, String[] args) {
      List<String> list = Arrays.asList(args);
      
      for (int i = 1; i <= args.length; i++)
         str = replaceArg(str, list, i, args);
      
      return str;
   }
   
   private static String replaceArg(String str, List<String> list, int i, String[] args) {
      str = str
        .replace(Config.ph("arg" + i), args[i - 1])
        .replace(Config.ph("arg" + i + ".."), join(list, i, args.length))
        .replace(Config.ph("arg.." + i), join(list, 1, i));
      
      for(int j = i; j <= args.length; j++)
         str = str.replace(Config.ph("arg" + i + ".." + j), join(list, i, j));
      
      return str;
   }
   
   private static String join(List<String> list, int start, int end) {
      return String.join(" ", list.subList(start - 1, end));
   }
}
