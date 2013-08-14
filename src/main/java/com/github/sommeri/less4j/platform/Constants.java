package com.github.sommeri.less4j.platform;


public interface Constants {

  /**
   * less.js uses \n no matter what, so we will use it too
   */
  public static final String NEW_LINE = "\n";//System.getProperty("line.separator");

  public static final String FILE_SEPARATOR = System.getProperty("file.separator");
  

  public static final String CSS_SUFFIX = ".css";

  public static final String SOURCE_MAP_SUFFIX = ".map";

  public static final String FULL_SOURCE_MAP_SUFFIX = ".css.map";

}
