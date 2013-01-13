package com.github.sommeri.less4j.utils;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Problem;
import com.github.sommeri.less4j.core.ThreadUnsafeLessCompiler;

//FIXME: !!! modify readme and describe API !!!
public class ReadmeExample {
  public static void main(String[] args) throws Less4jException {
    File inputLessFile = createFile("sampleInput.less", "* { margin: 1 1 1 1; }");
    LessCompiler compiler = new ThreadUnsafeLessCompiler();
    CompilationResult compilationResult = compiler.compile(inputLessFile);

    System.out.println(compilationResult.getCss());
    for (Problem warning : compilationResult.getWarnings()) {
      System.err.println(format(warning));
    }
    
    deleteFile(inputLessFile);
  }

  private static void deleteFile(File inputLessFile) {
    try {
      FileUtils.forceDelete(inputLessFile);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static File createFile(String filename, String content) {
    File file = new File(filename);
    try {
      FileUtils.writeStringToFile(file, content, false);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return file;
  }

  private static String format(Problem warning) {
    return "WARNING " + warning.getLine() +":" + warning.getCharacter()+ " " + warning.getMessage();
  }
}
