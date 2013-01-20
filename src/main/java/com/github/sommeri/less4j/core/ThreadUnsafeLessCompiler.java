package com.github.sommeri.less4j.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessSource;
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
    return compile(new LessSource.StringSource(lessContent));
  }

  @Override
  public CompilationResult compile(File inputFile) throws Less4jException {
	return compile(new LessSource.FileSource(inputFile));
}

  @Override
  public CompilationResult compile(URL inputURL) throws Less4jException {
	return compile(new LessSource.URLSource(inputURL));
  }

  public CompilationResult compile(LessSource source) throws Less4jException {
    problemsHandler = new ProblemsHandler();
    astBuilder = new ASTBuilder(problemsHandler);
    compiler = new LessToCssCompiler(problemsHandler);
    String css = doCompile(source);
    CompilationResult compilationResult = new CompilationResult(css, problemsHandler.getWarnings());
    if (problemsHandler.hasErrors()) {
      throw new Less4jException(problemsHandler.getErrors(), compilationResult);
    }
    return compilationResult;
  }

private String doCompile(LessSource source) throws Less4jException {
    ANTLRParser.ParseResult result;
	try {
		result = parser.parseStyleSheet(source.getContent(), source);
	} catch (FileNotFoundException ex) {
		throw new Less4jException(new GeneralProblem("The file " + source + " does not exists."), new CompilationResult(null));
	} catch (IOException ex) {
		throw new Less4jException(new GeneralProblem("Cannot read the file " + source + "."), new CompilationResult(null));
	}
	
    if (result.hasErrors()) {
      CompilationResult compilationResult = new CompilationResult("Errors during parsing phase, partial result is not available.");
      throw new Less4jException(result.getErrors(), compilationResult);
    }
    StyleSheet lessStyleSheet = astBuilder.parse(result.getTree());
    ASTCssNode cssStyleSheet = compiler.compileToCss(lessStyleSheet, source);

    CssPrinter builder = new CssPrinter();
    builder.append(cssStyleSheet);
    return builder.toString();
  }

}
