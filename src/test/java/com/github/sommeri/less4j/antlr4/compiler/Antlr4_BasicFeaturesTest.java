package com.github.sommeri.less4j.antlr4.compiler;

import java.io.File;

import com.github.sommeri.less4j.AbstractFileBasedTest;
import com.github.sommeri.less4j.antlr4.Antlr4_AbstractFileBasedTest;

public abstract class Antlr4_BasicFeaturesTest extends Antlr4_AbstractFileBasedTest {

  public Antlr4_BasicFeaturesTest(File inputFile, File outputFile, File errorList, File mapdataFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, testName);
  }

  protected String canonize(String text) {
    //ignore end of line separator differences
    text = text.replace("\r\n", "\n");

    //ignore differences in various ways to write "1/1"
    text = text.replaceAll("1 */ *1", "1/1");

    //ignore occasional end lines
    while (text.endsWith("\n"))
      text=text.substring(0, text.length()-1);
    
    return text;
  }


}
