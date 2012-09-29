package com.github.less4j.utils.w3ctestsextractor.common;

import java.io.File;

public class CaseBuilder {

  protected String getCurrentDirectory() {
    String path = getClass().getPackage().getName().replace(".", "/");
    path = "src/test/java/" + path;
    return path;
  }

  protected void ensureDirectory(String directory) {
    (new File(directory)).mkdirs();
  }

}
