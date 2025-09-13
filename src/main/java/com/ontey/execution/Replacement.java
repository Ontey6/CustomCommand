package com.ontey.execution;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import static com.ontey.files.Config.ph;

public class Replacement {
   public static String replaceArgs(@NotNull String str, String[] args) {
      List<String> list = Arrays.asList(args);
      
      for (int i = 1; i <= args.length; i++)
         str = replaceArg(str, list, i, args);
      
      return str;
   }
   
   private static String replaceArg(String str, List<String> list, int i, String[] args) {
      str = str
        .replace(ph("arg" + i), args[i - 1])
        .replace(ph("arg" + i + ".."), join(list, i, args.length))
        .replace(ph("arg.." + i), join(list, 1, i));
      
      for(int j = i; j <= args.length; j++)
         str = str.replace(ph("arg" + i + ".." + j), join(list, i, j));
      
      return str;
   }
   
   private static String join(List<String> list, int start, int end) {
      return String.join(" ", list.subList(start - 1, end));
   }
}
