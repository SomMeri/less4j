package com.github.sommeri.less4j.commandline;

import java.io.File;

public class FileSystemUtils {

  public static String changeSuffix(String filename, String dottedSuffix) {
    int lastIndexOf = filename.lastIndexOf('.');
    if (lastIndexOf == -1)
      return filename + dottedSuffix;

    return filename.substring(0, lastIndexOf) + dottedSuffix;
  }

  public static boolean ensureDirectory(String outputDirectory, CommandLinePrint print) {
    if (outputDirectory == null || outputDirectory.isEmpty())
      return true;

    File directory = new File(outputDirectory);
    if (directory.exists() && !directory.isDirectory()) {
      print.reportError(directory + " is not a directory.");
      return false;
    }

    if (!directory.exists()) {
      if (!directory.mkdirs()) {
        print.reportError("Could not create the directory " + directory + ".");
        return false;
      }
    }

    return true;
  }

}
