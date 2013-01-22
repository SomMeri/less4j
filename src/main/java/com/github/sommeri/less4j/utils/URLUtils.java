package com.github.sommeri.less4j.utils;

import java.net.MalformedURLException;
import java.net.URL;

public class URLUtils {

  public static URL toParentURL(URL url) {
    String path = url.getPath();
    int i = path.lastIndexOf('/');
    if (i != -1) {
      path = path.substring(0, i + 1);
    }
    try {
      return new URL(url, path + url.getQuery());
    } catch (MalformedURLException ex) {
      throw new RuntimeException(ex);
    }
  }

}
