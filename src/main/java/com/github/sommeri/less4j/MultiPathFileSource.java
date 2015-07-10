package com.github.sommeri.less4j;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;

/**
 * Class that wraps the LESS code that could be located either on a file or given as a string.
 * Used to emulate the --include-path options which is not natively implemented in Less4j.
 * See: https://github.com/SomMeri/less4j/wiki/Customizing-Compiler.
 */
public class MultiPathFileSource extends LessSource {

  private File[] includePaths;
  private String charsetName;
  private File file;

  /**
   * @param file the file containing the LESS code to compile.
   * @param includePaths an array of paths where the include files can be located.
   */
  public MultiPathFileSource(File file, String charsetName, File... includePaths) {
    this.file = file;
    this.charsetName = charsetName;
    this.includePaths = includePaths;
  }

  /**
   * @param file the file containing the LESS code to compile.
   * @param includePaths an array of paths where the include files can be located.
   */
  public MultiPathFileSource(File file, File... includePaths) {
    this.file = file;
    this.charsetName = null;
    this.includePaths = includePaths;
  }

  @Override
  public LessSource relativeSource(String filename) throws FileNotFound {
    // First look at files located in the same directory than the current one, if there is a current one
    if (file != null) {
      File currentDir = file.getParentFile();
      File newFile = new File(currentDir, filename);
      if (newFile.exists()) {
        return new MultiPathFileSource(newFile, charsetName, this.includePaths);
      }
    }

    // then look inside the include paths
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
    FileSource fileSource = new FileSource(file, charsetName);
    return fileSource.getContent();
  }

  @Override
  public byte[] getBytes() throws FileNotFound, CannotReadFile {
    FileSource fileSource = new FileSource(file, charsetName);
    return fileSource.getBytes();
  }

  @Override
  public String getName() {
    FileSource fileSource = new FileSource(file, charsetName);
    return fileSource.getName();
  }
  
  public URI getURI() {
    FileSource fileSource = new FileSource(file, charsetName);
    return fileSource.getURI();
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((charsetName == null) ? 0 : charsetName.hashCode());
    File canonicalFile = getCanonicalFile();
    result = prime * result + ((canonicalFile == null) ? 0 : canonicalFile.hashCode());
    result = prime * result + Arrays.hashCode(includePaths);
    return result;
  }
  
  private File getCanonicalFile() {
    try {
      return getInputFile().getCanonicalFile();
    } catch (IOException e) {
      return getInputFile().getAbsoluteFile();
    }
  }

  public File getInputFile() {
    return file;
  }

  public String toString() {
    return getInputFile().toString();
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    MultiPathFileSource other = (MultiPathFileSource) obj;
    if (charsetName == null) {
      if (other.charsetName != null)
        return false;
    } else if (!charsetName.equals(other.charsetName))
      return false;
    File canonicalFile = getCanonicalFile();
    File othercanonicalFile = other.getCanonicalFile();
    if (canonicalFile == null) {
      if (othercanonicalFile != null)
        return false;
    } else if (!canonicalFile.equals(othercanonicalFile))
      return false;
    if (!Arrays.equals(includePaths, other.includePaths))
      return false;
    return true;
  }

  
}
