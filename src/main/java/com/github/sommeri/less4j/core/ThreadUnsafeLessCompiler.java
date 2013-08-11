package com.github.sommeri.less4j.core;

import java.io.File;
import java.net.URI;
import java.net.URL;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.LessSource.CannotReadFile;
import com.github.sommeri.less4j.LessSource.FileNotFound;
import com.github.sommeri.less4j.commandline.FileSystemUtils;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.compiler.LessToCssCompiler;
import com.github.sommeri.less4j.core.parser.ANTLRParser;
import com.github.sommeri.less4j.core.parser.ASTBuilder;
import com.github.sommeri.less4j.core.problems.GeneralProblem;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.platform.Constants;
import com.github.sommeri.less4j.utils.CssPrinter;

public class ThreadUnsafeLessCompiler implements LessCompiler {
  private ProblemsHandler problemsHandler;
  private ASTBuilder astBuilder;
  private LessToCssCompiler compiler;

  private ANTLRParser parser = new ANTLRParser();

  @Override
  public CompilationResult compile(String lessContent) throws Less4jException {
    return compile(new LessSource.StringSource(lessContent), null);
  }

  @Override
  public CompilationResult compile(String lessContent, Configuration options) throws Less4jException {
    return compile(new LessSource.StringSource(lessContent), options);
  }

  @Override
  public CompilationResult compile(File lessFile) throws Less4jException {
    Configuration options = new Configuration();

    //FIXME: source map (!): do the same for url and general less source
    LessSource.FileSource lessSource = new LessSource.FileSource(lessFile);
    LessSource.FileSource cssSource = FileSystemUtils.changeSuffix(lessSource, Constants.CSS_SUFFIX);

    options.setCssResultLocation(cssSource);
    
    return compile(lessSource, options);
  }

  @Override
  public CompilationResult compile(File lessFile, Configuration options) throws Less4jException {
    return compile(new LessSource.FileSource(lessFile), options);
  }

  @Override
  public CompilationResult compile(URL lessURL) throws Less4jException {
    return compile(new LessSource.URLSource(lessURL));
  }

  @Override
  public CompilationResult compile(URL lessURL, Configuration options) throws Less4jException {
    return compile(new LessSource.URLSource(lessURL), options);
  }

  @Override
  public CompilationResult compile(LessSource source) throws Less4jException {
    return compile(source, null);
  }

  @Override
  public CompilationResult compile(LessSource source, Configuration options) throws Less4jException {
    problemsHandler = new ProblemsHandler();
    astBuilder = new ASTBuilder(problemsHandler);
    compiler = new LessToCssCompiler(problemsHandler);
    CompilationResult compilationResult = doCompile(source, options);
    if (problemsHandler.hasErrors()) {
      throw new Less4jException(problemsHandler.getErrors(), compilationResult);
    }
    return compilationResult;
  }

  private CompilationResult doCompile(LessSource source, Configuration options) throws Less4jException {
    if (options==null)
      options = new Configuration();
      
    ANTLRParser.ParseResult result;
    try {
      result = parser.parseStyleSheet(source.getContent(), source);
    } catch (FileNotFound ex) {
      throw new Less4jException(new GeneralProblem("The file " + source + " does not exists."), new CompilationResult(null));
    } catch (CannotReadFile ex) {
      throw new Less4jException(new GeneralProblem("Cannot read the file " + source + "."), new CompilationResult(null));
    }

    if (result.hasErrors()) {
      CompilationResult compilationResult = new CompilationResult("Errors during parsing phase, partial result is not available.");
      throw new Less4jException(result.getErrors(), compilationResult);
    }
    StyleSheet lessStyleSheet = astBuilder.parse(result.getTree());
    ASTCssNode cssStyleSheet = compiler.compileToCss(lessStyleSheet, source, options);

    CompilationResult compilationResult = createCompilationResult(cssStyleSheet, source, options);
    return compilationResult;
  }

//  private Configuration defaultConfiguration(LessSource less) {
//    if (less==null)
//      return new Configuration();
//
//    String defaultCssDestination = FileSystemUtils.changeSuffix(less, Constants.CSS_SUFFIX);
//
//    Configuration configuration = new Configuration();
//    configuration.setCssResultLocation(new LessSource.StringSource("", defaultCssDestination));
//    return configuration;
//  }

  private CompilationResult createCompilationResult(ASTCssNode cssStyleSheet, LessSource lessSource, Configuration options) {
    LessSource cssDestination = options == null ? null : options.getCssResultLocation();
    if (cssDestination==null) {
      String guessedCssName = FileSystemUtils.changeSuffix(lessSource.getName(), Constants.CSS_SUFFIX);
      URI guessedURI = FileSystemUtils.changeSuffix(lessSource.getURI(), Constants.CSS_SUFFIX);
      cssDestination = new LessSource.StringSource("", guessedCssName, guessedURI);
    }
    
    CssPrinter builder = new CssPrinter(lessSource, cssDestination);
    builder.append(cssStyleSheet);
    String css = builder.toCss();
    String sourceMap = builder.toSourceMap();

    //FIXME: source map: add command line
    CompilationResult compilationResult = new CompilationResult(css, sourceMap, problemsHandler.getWarnings());
    return compilationResult;
  }

}
