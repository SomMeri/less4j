package com.github.sommeri.less4j.utils.debugonly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

//marked deprecated so I get a warning if it is referenced somewhere
@Deprecated
public class DebugSysout {
  
  private static boolean isOn = true;
  private static Map<Object, Integer> counts = new HashMap<Object, Integer>();

   public static void println(Object text) {
     if (isOn)
       System.out.println(text);
   }

   public static void print(Object text) {
     if (isOn)
       System.out.print(text);
   }

   public static void addAndCount(Object text) {
     if (counts.containsKey(text)) {
       counts.put(text, counts.get(text)+1);
       return ;
     }
     
     counts.put(text, Integer.valueOf(1));
   }

  public static void printCounts() {
    List<Entry<Object, Integer>> entries = new ArrayList<Map.Entry<Object,Integer>>(counts.entrySet());
    Collections.sort(entries, new Comparator<Entry<Object, Integer>>() {

      @Override
      public int compare(Entry<Object, Integer> o1, Entry<Object, Integer> o2) {
        return o1.getValue().compareTo(o2.getValue());
      }
    });
    
    for (int i=0; i<entries.size(); i++) {
      Entry<Object, Integer> entry = entries.get(i);
      System.out.println(entry.getValue() + " " + entry.getKey());
    }
    
  }
}
