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

//*** should this work? ****
.mixin() {
  @detached: { extreme: simplicity; };
}
.selector {
  @detached();
  .mixin();
}
//********************************************88 
.mixin() {
  @detached: { scope: @see-here; };
}
.selector {
  @see-here: yes;
  .nested {
    .mixin();
    @detached();
  }
}
//************************************************************
 .mixin() {
  @detached: { scope-detached: @see-here; };
  .nested() {
    scope-mixin: @see-here; 
  }
}
.definer-wrapper() {
  @see-here: yes;
  .mixin();
}
.selector {
  .definer-wrapper();
  @detached();
  .nested();
}

 
//********************************************88
 * test if it works from imported !!!!!!!!!!!!!!!!!!!!
 * test if it works when variables copy detached forever !!!!!!!!!!!!!!!!!!!!
 * test if it works from list correctly -- e.g. including various callers scopes
 * test if @defaults works correctly -- e.g. including various callers scopes

 */
//@Ignore
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
