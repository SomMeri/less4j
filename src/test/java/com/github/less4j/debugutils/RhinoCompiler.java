package com.github.less4j.debugutils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import ro.isdc.wro.WroRuntimeException;
import ro.isdc.wro.extensions.processor.support.less.LessCss;

/**
 * Utility class generating expected output for test cases. Goes through all .less files in a directory and:
 * * Runs less.js on them.
 * * Runs {@link LessjsToLess4jTransformer} on less.js output.  
 * 
 */
public class RhinoCompiler {
  
  private static final String CSS3_SELECTORS_TEST_CASES = "src\\test\\resources\\w3c-official-test-cases\\CSS3-Selectors\\";
  private LessjsToLess4jTransformer less2Less = new LessjsToLess4jTransformer();

  public void compileAll(String directory) throws FileNotFoundException, IOException {
    String unsuccesfull = "";
    LessCss officialCompiler = new LessCss();

    Collection<File> allFiles = FileUtils.listFiles(new File(directory), new String[] { "less" }, false);
    for (File file : allFiles) {
      if (!compile(directory, unsuccesfull, officialCompiler, file))
        unsuccesfull += "\n" + file.getName();
    }
    System.out.println("*****************************************************************");
    System.out.println(unsuccesfull);
    System.out.println("*****************************************************************");
  }

  public boolean compile(String directory, String unsuccesfull, LessCss officialCompiler, File file) throws IOException, FileNotFoundException {
    String less = IOUtils.toString(new FileReader(file));
    String inputFileName = file.getName();
    try {
      String css = officialCompiler.less(less);
      css = less2Less.transform(css);
      
      String name = toOutputFilename(directory, inputFileName);
      write(name, css);
    } catch (WroRuntimeException ex) {
      System.out.println(inputFileName);
      return false;
    }
    return true;
  }

  public void write(String fileName, String content) throws IOException {
    File newFile = new File(fileName);
    newFile.createNewFile();
    FileWriter fstream = new FileWriter(fileName);
    BufferedWriter out = new BufferedWriter(fstream);
    out.write(content);
    out.close();
  }

  public String toOutputFilename(String directory, String inputFileName) {
    return directory + inputFileName.substring(0, inputFileName.length() - 5) + ".css";
  }

  public static void main(String[] args) throws FileNotFoundException, IOException {
    String input = CSS3_SELECTORS_TEST_CASES;
    RhinoCompiler me = new RhinoCompiler();
    me.compileAll(input);
  }
}
