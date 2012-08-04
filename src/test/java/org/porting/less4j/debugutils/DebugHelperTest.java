package org.porting.less4j.debugutils;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collection;

import org.antlr.runtime.tree.CommonTree;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.porting.less4j.core.parser.ANTLRParser;
import org.porting.less4j.core.parser.ANTLRParser.ParseResult;
import org.porting.less4j.core.parser.HiddenTokenAwareTree;

@RunWith(Parameterized.class)
@SuppressWarnings("unused")
public class DebugHelperTest {

  private static final String inputDir = "src\\test\\resources\\minitests\\css\\";
  private static final String rulesets = "src\\test\\resources\\minitests\\css\\rulesets.less";
  private static final String empty = "src\\test\\resources\\minitests\\css\\emptyRule.css";
  private static final String variable = "src\\test\\resources\\minitests\\css\\variablesNoCommentsNoMixins.less";
  private static final String variableMini = "src\\test\\resources\\minitests\\css\\variablesMini.less";

  private static final String inputCss21 = "src\\test\\resources\\minitests\\css\\css.less";
  private static final String inputCss3 = "src\\test\\resources\\minitests\\css\\css-3.less";
  private static final String inputNot = "src\\test\\resources\\minitests\\css\\cssNot.less";
  private static final String expectedCss = "src\\test\\resources\\less.js\\css\\css.css";
  private static final String urlCss = "src\\test\\resources\\minitests\\css\\url.css";
  private static final String urlVariable = "src\\test\\resources\\minitests\\css\\urlWithVariable.css";
  private static final String mini2 = "src\\test\\resources\\minitests\\css\\alphaOpacity.css";
  private static final String mini3 = "src\\test\\resources\\minitests\\css\\inputtype.css";
  private static final String mini4 = "src\\test\\resources\\minitests\\css\\emptyRule.css";

  private static final String mixinLess = "src\\test\\resources\\minitests\\less\\mixins.less";
  private static final String mixinCss = "src\\test\\resources\\minitests\\css\\mixins.css";

  private final File file;

  private String name;

  public DebugHelperTest(File file, String name) {
    this.file = file;
    this.name = name;
  }

  // FIXME: missing semicolons are not accepted
  // FIXME: operator = is only hack,
  // FIXME: @@name is OK, is @@@name also OK?
  // FIXME: special URLs url(data:something) are not accepted
  // It also accepts the ‘even’ and ‘odd’ values as arguments.(for n-th argument)
  @Test
  public void shouldParseWithoutErrors() throws Throwable {
    ANTLRParser compiler = new ANTLRParser();
    try {
      ParseResult result = compiler.parseStyleSheet(IOUtils.toString(new FileReader(file)));
      DebugPrint.print(result.getTree());
      assertTrue(name, result.getErrors().isEmpty());
    } catch (Throwable th) {
      th.printStackTrace();
      fail(name + " " + th.toString());
    }
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
    addFiles(result, new File(inputCss21));
    return result;
  }

  private static void addFiles(Collection<Object[]> result, File... files) {
    for (File file : files) {
      result.add(new Object[] { file, file.getName() });
    }
  }

  public static Collection<Object[]> allTests() {
    Collection<File> allFiles = FileUtils.listFiles(new File(inputDir), null, false);
    Collection<Object[]> result = new ArrayList<Object[]>();
    for (File file : allFiles) {
      addFiles(result, file);
    }
    return result;
  }

}
