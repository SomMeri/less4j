package com.github.sommeri.less4j.utils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PerformanceUtil {
  
  private Map<String, Long> starts = new HashMap<String, Long>();
  
  public void start(String key) {
    starts.put(key, System.currentTimeMillis());
  }
  
  public long measure(String key) {
    if (!starts.containsKey(key))
      return -1;

    long now = System.currentTimeMillis();
    return now - starts.get(key);
  }

  public String measureAsString(String key) {
    return format(measure(key));
  }

  public String format(long millis) {
    long second = (millis / 1000) % 60;
    long minute = (millis / (1000 * 60)) % 60;
    long hour = (millis / (1000 * 60 * 60)) % 24;

    return String.format("%02d:%02d:%02d:%d", hour, minute, second, millis);
  }
}
