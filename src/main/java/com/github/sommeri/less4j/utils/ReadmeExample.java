package com.github.sommeri.less4j.utils;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

import org.apache.commons.io.FileUtils;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessFunction;
import com.github.sommeri.less4j.LessProblems;
import com.github.sommeri.less4j.LessCompiler.CompilationResult;
import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.LessCompiler.Problem;
import com.github.sommeri.less4j.LessSource.StringSource;
import com.github.sommeri.less4j.core.DefaultLessCompiler;
import com.github.sommeri.less4j.core.ThreadUnsafeLessCompiler;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;

public class ReadmeExample {
  
  public static void main(String[] args) throws Less4jException {
    basicFileExample();
    customCssLocationExample1();
    customCssLocationExample2();
    guessedCssLocationExample1();
    guessedCssLocationExample2();
    customFunctionExample();
  }

  private static void basicFileExample() throws Less4jException {
    File inputLessFile = createFile("sampleInput.less", "* { margin: 1 1 1 1; }");
    LessCompiler compiler = new ThreadUnsafeLessCompiler();
    CompilationResult compilationResult = compiler.compile(inputLessFile);

    System.out.println(compilationResult.getCss());
    for (Problem warning : compilationResult.getWarnings()) {
      System.err.println(format(warning));
    }
    
    deleteFile(inputLessFile);
  }

  private static void guessedCssLocationExample1() throws Less4jException {
    File inputLessFile = createFile("src/sampleInput.less", ".class { margin: 1 1 1 1; }");

    File lessFile = new File("src/sampleInput.less").getAbsoluteFile();
    
    LessCompiler compiler = new DefaultLessCompiler();
    CompilationResult compilationResult = compiler.compile(lessFile);

    System.out.println(compilationResult.getCss());
    System.out.println(compilationResult.getSourceMap());

    deleteFile(inputLessFile);
  }

  private static void guessedCssLocationExample2() throws Less4jException {
    URI uri = (new File("src/sampleInput.less")).toURI();
    StringSource lessSource = new StringSource(".class { margin: 1 1 1 1; }", "sampleInput.less", uri);
    
    LessCompiler compiler = new DefaultLessCompiler();
    CompilationResult compilationResult = compiler.compile(lessSource);

    System.out.println(compilationResult.getCss());
    System.out.println(compilationResult.getSourceMap());

  }

  private static void customCssLocationExample1() throws Less4jException {
    File inputLessFile = createFile("src/sampleInput.less", ".class { margin: 1 1 1 1; }");

    File lessFile = new File("src/sampleInput.less").getAbsoluteFile();
    File cssFile = new File("dist/sampleInput.css").getAbsoluteFile();
    
    Configuration configuration = new Configuration();
    configuration.setCssResultLocation(cssFile);
    
    LessCompiler compiler = new DefaultLessCompiler();
    CompilationResult compilationResult = compiler.compile(lessFile, configuration);

    System.out.println(compilationResult.getCss());
    System.out.println(compilationResult.getSourceMap());

    deleteFile(inputLessFile);
  }
  
  private static void customCssLocationExample2() throws Less4jException {
    File cssFile = new File("dist/sampleInput.css").getAbsoluteFile();
    Configuration configuration = new Configuration();
    configuration.setCssResultLocation(cssFile);

    URI uri = (new File("src/sampleInput.less")).toURI();
    StringSource lessSource = new StringSource(".class { margin: 1 1 1 1; }", "sampleInput.less", uri);
    
    LessCompiler compiler = new DefaultLessCompiler();
    CompilationResult compilationResult = compiler.compile(lessSource, configuration);

    System.out.println(compilationResult.getCss());
    System.out.println(compilationResult.getSourceMap());
  }

  private static void customFunctionExample() throws Less4jException {
    File inputLessFile = createFile("sampleInput.less", 
    "div {\n" +
    "    property: constant();\n" +
    "}");

    Configuration configuration = new Configuration();
    configuration.addCustomFunction(new ConstantFunction());

    LessCompiler compiler = new DefaultLessCompiler();
    CompilationResult compilationResult = compiler.compile(inputLessFile, configuration);

    System.out.println(compilationResult.getCss());
    deleteFile(inputLessFile);
  }

  private static void deleteFile(File inputLessFile) {
    try {
      FileUtils.forceDelete(inputLessFile);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static File createFile(String filename, String content) {
    File file = new File(filename);
    try {
      FileUtils.writeStringToFile(file, content, false);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return file;
  }

  private static String format(Problem warning) {
    return "WARNING " + warning.getLine() +":" + warning.getCharacter()+ " " + warning.getMessage();
  }
}

class ConstantFunction implements LessFunction {

  @Override
  public boolean canEvaluate(FunctionExpression input, List<Expression> parameters) {
    return input.getName().equals("constant") && parameters.isEmpty();
  }

  @Override
  public Expression evaluate(FunctionExpression input, List<Expression> parameters, Expression evaluatedParameter, LessProblems problems) {
    return new IdentifierExpression(input.getUnderlyingStructure(), "fixed");
  }
  
}
