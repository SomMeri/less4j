package com.github.sommeri.less4j.nodemime;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import com.github.sommeri.less4j.core.problems.BugHappened;

/**
 * Port of node.js mime package into java. It detects file mimetype from filename suffix. File content 
 * is ignored. 
 */
public class NodeMime {

  private static final String MIMETYPES_NODE_TYPES = "/mimetypes/node.types";
  private static final String MIMETYPES_MIME_TYPES = "/mimetypes/mime.types";
  private static final String FALLBACK_SUFFIX = "bin";
  private Map<String, String> typeDatabase = null;

  public NodeMime() {
  }

  public String lookupMime(String path) {
    if (path==null)
      return defaultType();
    
    int indx = Math.max(Math.max(path.lastIndexOf("."), path.lastIndexOf("\\")), path.lastIndexOf("/"));
    String suffix = path.substring(indx + 1, path.length());
    return lookupBySuffix(suffix);
  }

  public String lookupCharset(String mimeType) {
    return lookupCharset(mimeType, null);
  }
  
  public String lookupCharset(String mimeType, String fallback) {
    if (mimeType==null)
      return fallback;
    
    return mimeType.startsWith("text") ? "UTF-8" : fallback;
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
      load(MIMETYPES_MIME_TYPES);
      load(MIMETYPES_NODE_TYPES);
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
    String[] split = line.split("\\s+");
    String mimetype = split[0];
    for (int i = 1; i < split.length; i++) {
      String suffix = split[i];
      typeDatabase.put(suffix, mimetype);
    }

  }

  public boolean isText(String mimetype) {
    if (mimetype==null)
      return false;
    
    return mimetype.contains("text");
  }

}
