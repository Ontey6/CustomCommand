package com.ontey.types;

import com.ontey.execution.Execution;
import com.ontey.execution.Formattation;
import lombok.AllArgsConstructor;

import java.util.List;

@AllArgsConstructor
public class AdvancedBroadcast {
   
   private String range;
   
   public List<String> permission;
   
   public List<String> condition;
   
   public List<String> broadcast;
   
   public boolean includeConsole, includeSender;
   
   public double range(Execution exe) {
      String str = Formattation.formattedMessage(range, exe);
      if(!str.matches("[+-]?\\d+(\\.\\d+)?"))
         return -1;
      return Double.parseDouble(str);
   }
}