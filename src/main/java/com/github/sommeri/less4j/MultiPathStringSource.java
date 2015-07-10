package com.github.sommeri.less4j;

import java.io.File;
import java.net.URI;
import java.util.Arrays;

/**
 * Class that wraps the LESS code that could be located either on a file or given as a string.
 * Used to emulate the --include-path options which is not natively implemented in Less4j.
 * See: https://github.com/SomMeri/less4j/wiki/Customizing-Compiler.
 */
public class MultiPathStringSource extends LessSource {
  
  private File[] includePaths;
  private String name;
  private String content;
  private String charsetName;
  private URI uri;

  /**
   * @param content a string containing the LESS code to compile.
   * @param includePaths an array of paths where the include files can be located.
   */
  public MultiPathStringSource(String content, String name, URI uri, String charsetName, File... includePaths) {
    this.content = content;
    this.name = name;
    this.uri = uri;
    this.charsetName = charsetName;
    this.includePaths = includePaths;
  }

  /**
   * @param content a string containing the LESS code to compile.
   * @param includePaths an array of paths where the include files can be located.
   */
  public MultiPathStringSource(String content, String name, URI uri, File... includePaths) {
    this.content = content;
    this.name = name;
    this.uri = uri;
    this.includePaths = includePaths;
  }

  /**
   * @param content a string containing the LESS code to compile.
   * @param includePaths an array of paths where the include files can be located.
   */
  public MultiPathStringSource(String content, String name, File... includePaths) {
    this.content = content;
    this.name = name;
    this.includePaths = includePaths;
  }

  /**
   * @param content a string containing the LESS code to compile.
   * @param includePaths an array of paths where the include files can be located.
   */
  public MultiPathStringSource(String content, File... includePaths) {
    this.content = content;
    this.charsetName = null;
    this.includePaths = includePaths;
  }

  @Override
  public LessSource relativeSource(String filename) throws FileNotFound {
    // look inside the include paths
    for (int i = 0; i < includePaths.length; ++i) {
      File directory = includePaths[i];
      File newFile = new File(directory, filename);
      if (newFile.exists()) {
        return new MultiPathFileSource(newFile, charsetName, this.includePaths);
      }
    }

    // The file has not been found
    throw new FileNotFound();
  }

  @Override
  public String getContent() throws FileNotFound, CannotReadFile {
    return content;
  }

  @Override
  public byte[] getBytes() throws FileNotFound, CannotReadFile {
    if (content != null) {
      content.getBytes();
    }

    return null;
  }

  @Override
  public String getName() {
    return name;
  }


  @Override
  public URI getURI() {
    return uri;
  }


  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((charsetName == null) ? 0 : charsetName.hashCode());
    result = prime * result + ((content == null) ? 0 : content.hashCode());
    result = prime * result + Arrays.hashCode(includePaths);
    result = prime * result + ((name == null) ? 0 : name.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MultiPathStringSource other = (MultiPathStringSource) obj;
    if (charsetName == null) {
      if (other.charsetName != null)
        return false;
    } else if (!charsetName.equals(other.charsetName))
      return false;
    if (content == null) {
      if (other.content != null)
        return false;
    } else if (!content.equals(other.content))
      return false;
    if (!Arrays.equals(includePaths, other.includePaths))
      return false;
    if (name == null) {
      if (other.name != null)
        return false;
    } else if (!name.equals(other.name))
      return false;
    return true;
  }
  
  
}
