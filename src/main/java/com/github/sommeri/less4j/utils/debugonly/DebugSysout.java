package com.github.sommeri.less4j.utils.debugonly;

//marked deprecated so I get a warning if it is referenced somewhere
@Deprecated
public class DebugSysout {
  
  private static boolean isOn = true;

   public static void println(Object text) {
     if (isOn)
       System.out.println(text);
   }

   public static void print(Object text) {
     if (isOn)
       System.out.print(text);
   }
}
