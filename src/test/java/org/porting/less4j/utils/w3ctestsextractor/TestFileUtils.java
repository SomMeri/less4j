package org.porting.less4j.utils.w3ctestsextractor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;

public class TestFileUtils {

  public static Collection<Object[]> loadTestFiles(String... directories) {
    Collection<File> allFiles = getAllLessFiles(directories);
    Collection<Object[]> result = new ArrayList<Object[]>();
    for (File file : allFiles) {
      addFiles(result, file);
    }
    return result;
  }

  private static Collection<File> getAllLessFiles(String... directories) {
    Collection<File> allFiles = new LinkedList<File>();
    for (String directory : directories) {
      allFiles.addAll(FileUtils.listFiles(new File(directory), new String[] { "less" }, false));
    }
    return allFiles;
  }

  private static void addFiles(Collection<Object[]> result, File... files) {
    for (File file : files) {
      result.add(new Object[] { file, findCorrespondingCss(file), file.getName() });
    }
  }

  private static File findCorrespondingCss(File lessFile) {
    String lessFileName = lessFile.getPath();
    String cssFileName = convertToOutputFilename(lessFileName);
    File cssFile = new File(cssFileName);
    return cssFile;
  }

  private static String convertToOutputFilename(String name) {
    if (name.endsWith(".less"))
      return name.substring(0, name.length() - 5) + ".css";

    return name;
  }

  public static Collection<Object[]> loadTestFile(String directory, String filename) {
    String name = directory + filename;
    File input = new File(name);
    Collection<Object[]> result = new ArrayList<Object[]>();
    addFiles(result, input);
    return result;
  }

}
