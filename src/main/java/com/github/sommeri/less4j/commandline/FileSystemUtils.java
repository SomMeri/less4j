package com.github.sommeri.less4j.commandline;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.LessSource.FileSource;

public class FileSystemUtils {
//  URI
//  public URI(String scheme,
//     String userInfo,
//     String host,
//     int port,
//     String path,
//     String query,
//     String fragment)
//      throws URISyntaxException

  public static URI changeSuffix(URI uri, String dottedSuffix) {
    if (uri==null)
      return null;
    
    String newPath = changeSuffix(uri.getPath(), dottedSuffix);
    try {
      URI result = new URI(uri.getScheme(),uri.getUserInfo(), uri.getHost(), uri.getPort(), newPath, null, null);
      return result;
    } catch (URISyntaxException ex) {
      //TODO do something more reasonable, maybe return null?
      throw new IllegalStateException(ex);
    }
  }

  public static String changeSuffix(String filename, String dottedSuffix) {
    if (filename == null)
      return null;

    int lastIndexOf = filename.lastIndexOf('.');
    if (lastIndexOf == -1)
      return filename + dottedSuffix;

    return filename.substring(0, lastIndexOf) + dottedSuffix;
  }

  public static String addSuffix(String filename, String dottedSuffix) {
    if (filename == null)
      return null;

    return filename + dottedSuffix;
  }

  public static File changeSuffix(File file, String dottedSuffix) {
    if (file == null)
      return null;

    String filename = changeSuffix(file.toString(), dottedSuffix);
    return new File(filename);
  }

  public static FileSource changeSuffix(FileSource source, String dottedSuffix) {
    if (source == null)
      return null;

    return new LessSource.FileSource(changeSuffix(source.getInputFile(), dottedSuffix));
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
