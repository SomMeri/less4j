package com.github.sommeri.less4j.core;

import java.io.File;
import java.io.FileReader;

import org.apache.commons.io.IOUtils;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.compiler.LessToCssCompiler;
import com.github.sommeri.less4j.core.parser.ANTLRParser;
import com.github.sommeri.less4j.core.parser.ASTBuilder;
import com.github.sommeri.less4j.core.problems.GeneralProblem;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.utils.CssPrinter;

public class ThreadUnsafeLessCompiler implements LessCompiler {
  private ProblemsHandler problemsHandler;
  private ASTBuilder astBuilder;
  private LessToCssCompiler compiler;

  private ANTLRParser parser = new ANTLRParser();

  @Override
  public CompilationResult compile(String lessContent) throws Less4jException {
    return compile(lessContent, null);
  }

  @Override
  public CompilationResult compile(File inputFile) throws Less4jException {
    if (!inputFile.exists()) {
      throw new Less4jException(new GeneralProblem("The file " + inputFile.getPath() + " does not exists."), new CompilationResult(null));
    }
    if (!inputFile.canRead()) {
      throw new Less4jException(new GeneralProblem("Cannot read the file " + inputFile.getPath() + "."), new CompilationResult(null));
    }
    String content;
    try {
      FileReader input = new FileReader(inputFile);
      content = IOUtils.toString(input).replace("\r\n", "\n");
      input.close();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }
    return compile(content, inputFile);
  }

  private CompilationResult compile(String lessContent, File baseDirectory) throws Less4jException {
    problemsHandler = new ProblemsHandler();
    astBuilder = new ASTBuilder(problemsHandler);
    compiler = new LessToCssCompiler(problemsHandler);
    String css = doCompile(lessContent, baseDirectory);
    CompilationResult compilationResult = new CompilationResult(css, problemsHandler.getWarnings());
    if (problemsHandler.hasErrors()) {
      throw new Less4jException(problemsHandler.getErrors(), compilationResult);
    }
    return compilationResult;
  }

  private String doCompile(String lessContent, File inputFile) throws Less4jException {
    ANTLRParser.ParseResult result = parser.parseStyleSheet(lessContent, inputFile);
    if (result.hasErrors()) {
      CompilationResult compilationResult = new CompilationResult("Errors during parsing phase, partial result is not available.");
      throw new Less4jException(result.getErrors(), compilationResult);
    }
    StyleSheet lessStyleSheet = astBuilder.parse(result.getTree());
    ASTCssNode cssStyleSheet = compiler.compileToCss(lessStyleSheet, toBaseDirectory(inputFile));

    CssPrinter builder = new CssPrinter();
    builder.append(cssStyleSheet);
    return builder.toString();
  }

  private File toBaseDirectory(File inputFile) {
    return inputFile==null? null : inputFile.getAbsoluteFile().getParentFile();
  }

}
