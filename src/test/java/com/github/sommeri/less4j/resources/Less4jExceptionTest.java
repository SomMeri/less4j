package com.github.sommeri.less4j.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;

import org.junit.Test;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessSource.StringSource;
import com.github.sommeri.less4j.core.DefaultLessCompiler;

public class Less4jExceptionTest {

  @Test
  public void testCompileError() {
    String less = ".class { margin: @undefined; }";
    String expectedError = "Could not compile less. 1 error(s) occurred:\nERROR 1:18 The variable \"@undefined\" was not declared.\n 1: .class { margin: @undefined; }\n\n";

    expectCompileError(less, expectedError);
  }

  @Test
  public void testTwoErrors() {
    String less = ".class { margin: @variable; .mixin(); }";
    String expectedError = "Could not compile less. 2 error(s) occurred:\nERROR 1:29 Could not find mixin named \".mixin\".\n 1: .class { margin: @variable; .mixin(); }\n\nERROR 1:18 The variable \"@variable\" was not declared.\n 1: .class { margin: @variable; .mixin(); }\n\n";

    expectCompileError(less, expectedError);
  }

  @Test
  public void testThreeErrors() {
    String less = ".class { margin: @variable; .mixin(); size: @other;}";
    String expectedError = "Could not compile less. 3 error(s) occurred:\nERROR 1:29 Could not find mixin named \".mixin\".\n 1: .class { margin: @variable; .mixin(); size: @other;}\n\nERROR 1:18 The variable \"@variable\" was not declared.\n 1: .class { margin: @variable; .mixin(); size: @other;}\n\n...\n";

    expectCompileError(less, expectedError);
  }

  @Test
  public void testAntlrError() {
    String less = ".class { margin: 1 1 1 1; ";
    String expectedError = "Could not compile less. 1 error(s) occurred:\nERROR 1:27 no viable alternative at input '<EOF>' in ruleset (which started at 1:1)\n 1: .class { margin: 1 1 1 1; \n\n";

    expectCompileError(less, expectedError);
  }

  private void expectCompileError(String less, String expectedError) {
    StringSource lessSource = new StringSource(less);
    try {
      LessCompiler compiler = new DefaultLessCompiler();
      compiler.compile(lessSource);
    } catch (Less4jException e) {
      assertFalse(e.getErrors().isEmpty());
      assertEquals(expectedError, e.getMessage());
      return ;
    }
    
    fail("Should have thrown exception.");
  }

}
