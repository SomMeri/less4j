package com.github.sommeri.less4j.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

public class TestFileUtils {

  private String expectedDataFileSuffix = ".css";
  
  public TestFileUtils() {
  }
  
  public TestFileUtils(String expectedDataFileSuffix) {
    this.expectedDataFileSuffix = expectedDataFileSuffix;
  }
  
  public Collection<Object[]> loadTestFiles(String... directories) {
    Collection<File> allFiles = getAllLessFiles(directories);
    Collection<Object[]> result = new ArrayList<Object[]>();
    for (File file : allFiles) {
      addFiles(result, file);
    }
    return result;
  }

  private Collection<File> getAllLessFiles(String... directories) {
    Collection<File> allFiles = new LinkedList<File>();
    for (String directory : directories) {
      allFiles.addAll(FileUtils.listFiles(new File(directory), new String[] { "less" }, false));
    }
    return allFiles;
  }

  private void addFiles(Collection<Object[]> result, File... files) {
    for (File file : files) {
      result.add(new Object[] { file, findCorrespondingExpected(file), file.getName() });
    }
  }

  private File findCorrespondingExpected(File lessFile) {
    String lessFileName = lessFile.getPath();
    String cssFileName = convertToOutputFilename(lessFileName);
    File cssFile = new File(cssFileName);
    return cssFile;
  }

  private String convertToOutputFilename(String name) {
    if (name.endsWith(".less")) {
      return name.substring(0, name.length() - 5) + expectedDataFileSuffix;
    }

    return name;
  }

  public Collection<Object[]> loadTestFile(String directory, String filename) {
    String name = directory + filename;
    File input = new File(name);
    Collection<Object[]> result = new ArrayList<Object[]>();
    addFiles(result, input);
    return result;
  }

  public void removeFile(String filename) {
    File file = new File(filename);
    if (file.exists())
      file.delete();
  }

  public void assertFileContent(String lessFile, String expectedContent) {
    File file = new File(lessFile);
    if (!file.exists())
      fail("File " + lessFile + " was not created.");
    
    try {
      String realContent = IOUtils.toString(new FileReader(file)).replace("\r\n", "\n");
      assertEquals(expectedContent, realContent);
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    } 
  }

  public void removeFiles(String... files) {
    for (String file : files) {
      removeFile(file);
    }
  }

}
