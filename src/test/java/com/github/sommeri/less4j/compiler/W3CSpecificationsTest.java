package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.AbstractFileBasedTest;
import com.github.sommeri.less4j.utils.TestFileUtils;

/**
 *  Testing whether less4j correctly compiles examples found in various w3c specifications 
 *  or w3c specification drafts.
 *  
 */
//FIXME: !paged+viewport+keyframe!: mixin that brings @top @bottom in page or full page to top level
//FIXME: !paged+viewport+keyframe!: nested rulesets and variables scoping
//FIXME: !validation! warnings on bad body-able-members in result
public class W3CSpecificationsTest extends AbstractFileBasedTest {

  private static final String standardCases = "src/test/resources/w3c-specifications/CSS Paged Media Module Level 3";
  
  public W3CSpecificationsTest(File inputFile, File outputFile, String testName) {
    super(inputFile, outputFile, testName);
  }

  //@Parameters(name="Compile Less: {0}, {2}")
  @Parameters()
  public static Collection<Object[]> allTestsParameters() {
    return (new TestFileUtils()).loadTestFiles(standardCases);
  }

}
