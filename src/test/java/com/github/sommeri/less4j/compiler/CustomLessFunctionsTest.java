package com.github.sommeri.less4j.compiler;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runners.Parameterized.Parameters;

import com.github.sommeri.less4j.AbstractFileBasedTest;
import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.LessFunction;
import com.github.sommeri.less4j.LessProblems;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;

public class CustomLessFunctionsTest extends AbstractFileBasedTest {

  private static final String standardCases = "src/test/resources/compile-basic-features/customfunc/";

  public CustomLessFunctionsTest(File inputFile, File outputFile, File errorList, File mapdataFile, File configFile, String testName) {
    super(inputFile, outputFile, errorList, mapdataFile, configFile, testName);
  }

  @Override
  protected Configuration createConfiguration(File cssOutput) {
    Configuration configuration = super.createConfiguration(cssOutput);
    configuration.addCustomFunction(new NoParametersTestFnc());
    configuration.addCustomFunctions(Arrays.asList(new MultipleParametersTestFnc(), new CauseWarningTestFnc()));
    return configuration;
  }

  @Parameters(name="Less: {5}")
  public static Collection<Object[]> allTestsParameters() {
    return createTestFileUtils().loadTestFiles(standardCases);
  }

}

class NoParametersTestFnc implements LessFunction {

  @Override
  public boolean canEvaluate(FunctionExpression input, List<Expression> parameters) {
    return input.getName().equals("worksWithNone") && parameters.isEmpty();
  }

  @Override
  public Expression evaluate(FunctionExpression input, List<Expression> parameters, Expression evaluatedParameter, LessProblems problems) {
    return new IdentifierExpression(input.getUnderlyingStructure(), "worked");
  }
  
}

class MultipleParametersTestFnc implements LessFunction {

  @Override
  public boolean canEvaluate(FunctionExpression input, List<Expression> parameters) {
    return input.getName().equals("worksWithMultiple");
  }

  @Override
  public Expression evaluate(FunctionExpression input, List<Expression> parameters, Expression evaluatedParameter, LessProblems problems) {
    return new CssString(input.getUnderlyingStructure(), "tr", "\"");
  }
  
}

class CauseWarningTestFnc implements LessFunction {

  @Override
  public boolean canEvaluate(FunctionExpression input, List<Expression> parameters) {
    return input.getName().equals("warning");
  }

  @Override
  public Expression evaluate(FunctionExpression input, List<Expression> parameters, Expression evaluatedParameter, LessProblems problems) {
    problems.addWarning(evaluatedParameter, "With custom error node.");
    problems.addWarning(null, "Null error node.");
    return new IdentifierExpression(input.getUnderlyingStructure(), "caused");
  }
  
}