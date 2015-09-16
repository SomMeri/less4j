package com.github.sommeri.less4j.utils;

import java.util.HashMap;
import java.util.Map;

public class TestConfigParser {
  
  private final String configFile;

  public TestConfigParser(String configFile) {
    super();
    this.configFile = configFile;
  }

  public Map<String, String> externalVariables() {
    Map<String, String> result = new HashMap<String, String>();
    String[] split = configFile.split("\n");
    for (String string : split) {
      string = string.trim();
      if (!string.isEmpty() && string.charAt(0)!='#') {
        if (string.endsWith(";"))
          string = string.substring(0, string.length()-1);
        String[] nameValue = string.split(":", 2);
        result.put(nameValue[0].trim(), nameValue[1].trim());
      }
    }
    
    return result;
  }

}
