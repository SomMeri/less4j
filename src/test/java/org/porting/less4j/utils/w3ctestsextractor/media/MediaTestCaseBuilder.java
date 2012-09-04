package org.porting.less4j.utils.w3ctestsextractor.media;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.porting.less4j.utils.w3ctestsextractor.common.CaseBuilder;
import org.porting.less4j.utils.w3ctestsextractor.common.SimpleFileReader;
import org.porting.less4j.utils.w3ctestsextractor.common.SimpleFileWriter;

/**
 * The class converts the output of the modified w3c test suite and converts it into  
 * six .less files. The name of the file suggests what the original suite done with 
 * that test.
 *
 */
public class MediaTestCaseBuilder extends CaseBuilder {

  private static final String INPUT_FILE = "/data/raw_queries.txt";
  private static final String OUTPUT_DIRECTORY = "/output/";
  private static final String STANDARD_BODY = " {\n  font-size: 3em;\n}\n";
  private HashMap<String, List<TestCase>> testCases;

  public MediaTestCaseBuilder() {
    testCases = new HashMap<String, List<TestCase>>();
  }

  public static void main(String[] args) throws IOException {
    MediaTestCaseBuilder builder = new MediaTestCaseBuilder();
    builder.buildTestCases();
    builder.outputTestCases();
  }

  private void outputTestCases() {
    SimpleFileWriter writer = new SimpleFileWriter();
    for (Entry<String, List<TestCase>> couple : testCases.entrySet()) {
      String kind = couple.getKey();
      String content = "";
      List<TestCase> tests = couple.getValue();
      for (TestCase testCase : tests) {
        content += "/* " + testCase.getName() + " */\n";
        content += toMediaQuery(kind, testCase.getTestCase());
      }

      try {
        String outputDirectory = getCurrentDirectory() + OUTPUT_DIRECTORY;
        ensureDirectory(outputDirectory);
        writer.write(outputDirectory + kind + ".less", content);
      } catch (IOException e) {
        throw new IllegalStateException(e);
      }
      System.out.println(content);
    }

  }

  private String toMediaQuery(String kind, String testCase) {
    testCase = testCase.trim();

    if (kind.equals("query should apply"))
      return "@media " + testCase + STANDARD_BODY;

    if (kind.equals("expression should be parseable"))
      return "@media " + testCase + STANDARD_BODY;

    if (kind.equals("query should be parseable"))
      return testCase.replace(" {}", STANDARD_BODY);

    if (kind.equals("query should NOT apply"))
      return "@media " + testCase + STANDARD_BODY;

    if (kind.equals("query should NOT be parseable"))
      return testCase.replace("{}", STANDARD_BODY);

    if (kind.equals("expression should NOT be parseable"))
      return "@media " + testCase + STANDARD_BODY;

    throw new IllegalStateException(kind);
  }

  private void buildTestCases() {
    SimpleFileReader reader = new SimpleFileReader(getCurrentDirectory() + INPUT_FILE);
    reader.skipHeaderIncluding("<tbody>");
    String blockStart = reader.readLine();
    while ("<tr class=\"fail\">".equals(blockStart)) {
      reader.assertLine("<td>Fail</td>");
      String nameLine = reader.readLine();
      String name = nameLine.substring(4, nameLine.length() - 5);
      String testLine = reader.readLine();
      String test = testLine.substring(22, testLine.length() - " Reached unreachable code</td>".length());
      String testKind = test.substring(0, test.indexOf(':'));
      String testCase = test.substring(test.indexOf('"') + 1, test.length() - 1);
      addTestCase(name, testKind, testCase);
      reader.assertLine("</tr>");
      blockStart = reader.readLine();
    }
  }

  private void addTestCase(String name, String testKind, String testCase) {
    TestCase test = new TestCase(name, testCase);
    List<TestCase> list = getTestsList(testKind);
    list.add(test);
  }

  public List<TestCase> getTestsList(String testKind) {
    List<TestCase> list = testCases.get(testKind);
    if (list == null) {
      list = new ArrayList<TestCase>();
      testCases.put(testKind, list);
    }
    return list;
  }

  private class TestCase {
    private final String name;
    private final String testCase;

    public TestCase(String name, String testCase) {
      super();
      this.name = name;
      this.testCase = testCase;
    }

    public String getName() {
      return name;
    }

    public String getTestCase() {
      return testCase;
    }

  }

}
