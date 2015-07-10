package com.github.sommeri.less4j;

import java.io.File;
import java.net.URI;
import java.nio.file.Path;

import org.apache.commons.io.Charsets;

public class MySource extends LessSource {

  private String content;
  private File path;

  public MySource(String content, File path) {
    this.content = content;
    this.path = path;
  }

  @Override
  public URI getURI() {
    return path.toURI();
  }

  @Override
  public String getName() {
    return path.getName();
  }

  @Override
  public String getContent() {
    return content;
  }

  @Override
  public byte[] getBytes() {
    return content != null ? content.getBytes() : null;
  }

  @Override
  public LessSource relativeSource(String filename) throws StringSourceException {
    return new FileSource(new File(path, filename), Charsets.UTF_8.toString());
  }
}
