package com.github.sommeri.less4j.compiler;

import java.io.File;


import com.github.sommeri.less4j.AbstractFileBasedTest;
import com.github.sommeri.less4j.ILessCompiler;
import com.github.sommeri.less4j.core.DefaultLessCompiler;

public abstract class BasicFeaturesTests extends AbstractFileBasedTest {

  public BasicFeaturesTests(File inputFile, File outputFile, String testName) {
    super(inputFile, outputFile, testName);
  }

  protected ILessCompiler getCompiler() {
    return new DefaultLessCompiler();
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
