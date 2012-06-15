package org.porting.less4j;

import java.io.FileReader;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.porting.less4j.core.LessCompiler;

public class PureCssTest {
  private static final String inputLess = "src\\test\\resources\\less.js\\less\\css.less";
  private static final String expectedCss = "src\\test\\resources\\less.js\\css\\css.css";
  private static final String mini1 = "src\\test\\resources\\minitests\\css\\url.css";
  private static final String mini2 = "src\\test\\resources\\minitests\\css\\alphaOpacity.css";
  private static final String mini3 = "src\\test\\resources\\minitests\\css\\inputtype.css";
  
  //FIXME: missing semicolons are not accepted
  //FIXME: operator = is only hack, 
  @Test
  public void test() throws Throwable {
    LessCompiler compiler = new LessCompiler();
    compiler.compile(IOUtils.toString(new FileReader(mini3)));

    compiler.compile(IOUtils.toString(new FileReader(mini1)));
    compiler.compile(IOUtils.toString(new FileReader(mini2)));
    compiler.compile(IOUtils.toString(new FileReader(mini3)));
    compiler.compile(IOUtils.toString(new FileReader(inputLess)));
    compiler.compile(IOUtils.toString(new FileReader(expectedCss)));
    compiler.compileExpression("url(images/image.jpg)");
  }

}
