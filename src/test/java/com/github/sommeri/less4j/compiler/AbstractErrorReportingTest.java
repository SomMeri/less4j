package com.github.sommeri.less4j.compiler;

import static org.junit.Assert.fail;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.ComparisonFailure;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.core.DefaultLessCompiler;
import com.github.sommeri.less4j.core.parser.ANTLRPhaseErrors;
import com.github.sommeri.less4j.core.parser.ANTLRPhaseErrors.SimpleError;

@RunWith(Parameterized.class)
public abstract class AbstractErrorReportingTest {

  private final File lessFile;
  private final File errorList;
  private final String testName;

  public AbstractErrorReportingTest(File lessFile, File errorList, String testName) {
    this.lessFile = lessFile;
    this.testName = testName;
    this.errorList = errorList;
  }

  @Test
  public final void compileAndCompare() throws Throwable {
    try {
      String less = IOUtils.toString(new FileReader(lessFile));
      LessCompiler compiler = getCompiler();
      String compile = compiler.compile(less);
      System.out.println(compile);
    } catch (ComparisonFailure ex) {
      ComparisonFailure fail = (ComparisonFailure) ex;
      throw new ComparisonFailure(testName + " " + fail.getMessage(), fail.getExpected(), fail.getActual());
    } catch (Less4jException ex) {
      validateException(ex);
      return;
    } catch (Throwable ex) {
      throw new RuntimeException(testName + " " + ex.getMessage(), ex);
    }

    fail(testName + " no exception has been thrown");
  }

  private void validateException(Less4jException ex) {
    List<SimpleError> result = parseExpectedOutput(ex);
    if (ex instanceof ANTLRPhaseErrors) {
      ANTLRPhaseErrors ant = (ANTLRPhaseErrors) ex;
      validateMultiple(result, ant);
    } else {
      validateSingle(ex, result);
    }
  }

  private void validateSingle(Less4jException ex, List<SimpleError> result) {
    assertEquals(1, result.size());
    SimpleError expectedError = result.get(0);
    assertEquals(expectedError.getLine(), ex.getLine());
    assertEquals(expectedError.getCharacter(), ex.getCharPositionInLine());
    assertEquals(expectedError.getMessage(), ex.getMessage());
  }

  private void validateMultiple(List<SimpleError> expectedResult, ANTLRPhaseErrors ant) {
    List<SimpleError> simpleErrors = ant.getSimpleErrors();
    assertEquals(expectedResult, simpleErrors);
  }

  public List<SimpleError> parseExpectedOutput(Less4jException ex) {
    List<SimpleError> result = new ArrayList<SimpleError>();
    
    try {
      String description = IOUtils.toString(new FileReader(errorList));
      String[] lines = description.split("\n");
      for (String line : lines) {
        String[] parts = line.split(":", 3);
        int errorLine = Integer.parseInt(parts[0]);
        int errorChar = Integer.parseInt(parts[1]);
        String message = parts[2]==null? null : parts[2].trim();
        
        result.add(new SimpleError(errorLine, errorChar, message));
      }
    } catch (FileNotFoundException e) {
      throw new RuntimeException(testName + " " + ex.getMessage(), ex);
    } catch (IOException e) {
      throw new RuntimeException(testName + " " + ex.getMessage(), ex);
    }
    return result;
  }

  protected String canonize(String text) {
    return text.replaceAll("\\s", "");
  }

  protected LessCompiler getCompiler() {
    return new DefaultLessCompiler();
  }

}

