package org.porting.less4j;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.porting.less4j.core.LessCompiler;

@RunWith(Parameterized.class)
public class ParserTest {

  private static final String inputDir = "src\\test\\resources\\minitests\\css\\";
  private static final String rulesets = "src\\test\\resources\\minitests\\css\\rulesets.less";
  private static final String empty = "src\\test\\resources\\minitests\\css\\emptyRule.css";
  private static final String variable = "src\\test\\resources\\minitests\\css\\variablesNoCommentsNoMixins.less";
  //private static final String variable = "src\\test\\resources\\minitests\\css\\variablesMini.less";
  
  private static final String inputLess = "src\\test\\resources\\less.js\\less\\css.less";
  private static final String expectedCss = "src\\test\\resources\\less.js\\css\\css.css";
  private static final String mini1 = "src\\test\\resources\\minitests\\css\\url.css";
  private static final String mini2 = "src\\test\\resources\\minitests\\css\\alphaOpacity.css";
  private static final String mini3 = "src\\test\\resources\\minitests\\css\\inputtype.css";
  private static final String mini4 = "src\\test\\resources\\minitests\\css\\emptyRule.css";

  private static final String mixinLess = "src\\test\\resources\\minitests\\less\\mixins.less";
  private static final String mixinCss = "src\\test\\resources\\minitests\\css\\mixins.css";

  private final File file;

  private String name;

  public ParserTest(File file, String name) {
    this.file = file;
    this.name = name;
  }

  // FIXME: missing semicolons are not accepted
  // FIXME: operator = is only hack,
  // FIXME: @@name is OK, is @@@name also OK?
  @Test
  public void shouldParseWithoutErrors() throws Throwable {
    LessCompiler compiler = new LessCompiler();
    compiler.compile(IOUtils.toString(new FileReader(file)));
    assertTrue(name, compiler.getAllErrors().isEmpty());
  }

  // TODO: add names after jUnit 11 comes out
  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    if (true)
      return singleTestsParameters();

    return allTests();
  }

  public static Collection<Object[]> singleTestsParameters() {
    Collection<Object[]> result = new ArrayList<Object[]>();
    //File file = new File(rulesets);
    File file = new File(variable);
    result.add(new Object[] { file, file.getName() });
    return result;
  }

  public static Collection<Object[]> allTests() {
    Collection<File> allFiles = FileUtils.listFiles(new File(inputDir), null, false);
    Collection<Object[]> result = new ArrayList<Object[]>();
    for (File file : allFiles) {
      result.add(new Object[] { file, file.getName() });
    }
    return result;
  }

}
