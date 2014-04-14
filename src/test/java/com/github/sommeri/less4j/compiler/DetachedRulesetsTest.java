package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Collection;

import org.junit.Ignore;
import org.junit.runners.Parameterized.Parameters;

//FIXME: call on variable with wrong datatype in
//FIXME: test order detached mixin imports who sees who and who overwrites who

/*
 * .wrap-mixin(@ruleset) {
    .wrap-selector {
        @d: invisible;
        @ruleset();
    }
};

.mixin() {
  abusing: less;
}

.wrap-mixin({
    two: @d;
    .mixin();
});

.selector {
  @nosemi: {};
  @indirect: "nosemi";
  @indirect();
}

// ******************* another **************************
@detached: { color: blue; };
.selector {
  interpolation: "@{detached}";
}



 */
@Ignore
public class DetachedRulesetsTest extends BasicFeaturesTest {

  private static final String standardCases = "src/test/resources/compile-basic-features/detached-rulesets/";

  public DetachedRulesetsTest(File inputFile, File outputFile, File errorList, File mapdataFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, testName);
  }

  @Parameters(name="Less: {4}")
  public static Collection<Object[]> allTestsParameters() {
    return createTestFileUtils().loadTestFiles(standardCases);
  }

}
