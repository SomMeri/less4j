package com.github.sommeri.less4j.nodemime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.github.sommeri.less4j.core.problems.BugHappened;

public class NodeMime {

  private static final String FALLBACK_SUFFIX = "bin";
  private Map<String, String> typeDatabase = null;

  public NodeMime() {
  }

  //FIXME !!!!!!!!!!! test with dot at the end
  public String lookupMime(String path) {
    if (path==null)
      return defaultType();
    
    int indx = Math.max(Math.max(path.lastIndexOf("."), path.lastIndexOf("\\")), path.lastIndexOf("/"));
    String suffix = path.substring(indx + 1, path.length());
    return lookupBySuffix(suffix);
  }

  private String lookupBySuffix(String suffix) {
    Map<String, String> typeDatabase = getTypeDatabase();
    String result = typeDatabase.get(suffix);
    return result != null ? result : defaultType();
  }

  private String defaultType() {
    return typeDatabase.get(FALLBACK_SUFFIX);
  }

  private Map<String, String> getTypeDatabase() {
    if (typeDatabase == null) {
      typeDatabase = new HashMap<String, String>();
      load("/mimetypes/mime.types");
      load("/mimetypes/node.types");
    }
    return typeDatabase;
  }

  private void load(String path) {
    InputStream stream = path.getClass().getResourceAsStream(path);
    if (stream == null)
      return;

    BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
    String line = null;
    try {
      while ((line = reader.readLine()) != null) {
        line = line.trim();
        if (!line.startsWith("#"))
          loadLine(line);
      }
      reader.close();
    } catch (IOException e) {
      throw new BugHappened(e, null);
    }

  }

  private void loadLine(String line) {
    //    line = line.replaceAll("\\s+", " ");
    String[] split = line.split("\\s+");
    String mimetype = split[0];
    for (int i = 1; i < split.length; i++) {
      String suffix = split[i];
      typeDatabase.put(suffix, mimetype);
    }

  }

  public static String getVersion() {
    String path = "/version.prop";

    InputStream stream = path.getClass().getResourceAsStream(path);
    if (stream == null)
      return "UNKNOWN";
    Properties props = new Properties();
    try {
      props.load(stream);
      stream.close();
      return (String) props.get("version");
    } catch (IOException e) {
      return "UNKNOWN";
    }
  }

  public boolean isText(String mimetype) {
    if (mimetype==null)
      return false;
    
    return mimetype.contains("text");
  }

}
