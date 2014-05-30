package com.github.sommeri.less4j.core;

import java.io.File;
import java.net.URI;
import java.net.URL;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.LessSource.CannotReadFile;
import com.github.sommeri.less4j.LessSource.FileNotFound;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.compiler.LessToCssCompiler;
import com.github.sommeri.less4j.core.parser.ANTLRParser;
import com.github.sommeri.less4j.core.parser.ASTBuilder;
import com.github.sommeri.less4j.core.problems.GeneralProblem;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.platform.Constants;
import com.github.sommeri.less4j.utils.CssPrinter;
import com.github.sommeri.less4j.utils.PrintUtils;
import com.github.sommeri.less4j.utils.URIUtils;

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
    LessSource.FileSource lessSource = new LessSource.FileSource(lessFile);
    return compile(lessSource, null);
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
    return compile(source, new Configuration());
  }

  @Override
  public CompilationResult compile(LessSource source, Configuration options) throws Less4jException {
    if (options==null)
      options = new Configuration();
      
    problemsHandler = new ProblemsHandler();
    astBuilder = new ASTBuilder(problemsHandler);
    compiler = new LessToCssCompiler(problemsHandler, options);
    CompilationResult compilationResult = doCompile(source, options);
    if (problemsHandler.hasErrors()) {
      throw new Less4jException(problemsHandler.getErrors(), compilationResult);
    }
    return compilationResult;
  }

  private CompilationResult doCompile(LessSource source, Configuration options) throws Less4jException {
    ANTLRParser.ParseResult result = toAntlrTree(source);
    StyleSheet lessStyleSheet = astBuilder.parse(result.getTree());
    ASTCssNode cssStyleSheet = compiler.compileToCss(lessStyleSheet, source, options);

    CompilationResult compilationResult = createCompilationResult(cssStyleSheet, source, options);
    return compilationResult;
  }

  private ANTLRParser.ParseResult toAntlrTree(LessSource source) throws Less4jException {
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
    return result;
  }

  private CompilationResult createCompilationResult(ASTCssNode cssStyleSheet, LessSource lessSource, Configuration options) {
    LessSource cssDestination = options == null ? null : options.getCssResultLocation();
    if (cssDestination==null) {
      String guessedCssName = URIUtils.changeSuffix(lessSource.getName(), Constants.CSS_SUFFIX);
      URI guessedURI = URIUtils.changeSuffix(lessSource.getURI(), Constants.CSS_SUFFIX);
      cssDestination = new LessSource.StringSource("", guessedCssName, guessedURI);
    }
    
    CssPrinter builder = new CssPrinter(lessSource, cssDestination, options);
    builder.append(cssStyleSheet);
    StringBuilder css = builder.toCss();
    String sourceMap = builder.toSourceMap();
    
    handleSourceMapLink(cssStyleSheet, css, options, lessSource, sourceMap);

    CompilationResult compilationResult = new CompilationResult(css.toString(), sourceMap, problemsHandler.getWarnings());
    return compilationResult;
  }

  private void handleSourceMapLink(ASTCssNode cssAst, StringBuilder css, Configuration options, LessSource source, String sourceMap) {
    String cssResultLocation = getCssResultLocationName(options, source);
    LessCompiler.SourceMapConfiguration sourceMapConfiguration = options.getSourceMapConfiguration();
    if (!sourceMapConfiguration.shouldLinkSourceMap() && !sourceMapConfiguration.isInline())
      return;
    
    if (!sourceMapConfiguration.isInline() && cssResultLocation==null) {
      problemsHandler.warnSourceMapLinkWithoutCssResultLocation(cssAst);
      return ; 
    }

    addNewLine(css);

    String commentText;
    String encodingCharset = sourceMapConfiguration.getEncodingCharset();
    if (sourceMapConfiguration.isInline()) {
      String encodedSourceMap = PrintUtils.base64Encode(sourceMap, encodingCharset, problemsHandler, cssAst);
      commentText = "/*# sourceMappingURL=data:application/json;base64," + encodedSourceMap + " */";
    } else {
      //compose linking comment
      String url = URIUtils.addSuffix(cssResultLocation, Constants.SOURCE_MAP_SUFFIX);
      String encodedUrl = PrintUtils.urlEncode(url, encodingCharset, problemsHandler, cssAst);
      commentText = "/*# sourceMappingURL=" + encodedUrl + " */";
    }

    css.append(commentText).append("\n");
  }

  private void addNewLine(StringBuilder css) {
    if (css==null)
      return ;
    
    int length = css.length();
    if (length==0) {
      css.append("\n");
      return ;
    }
    String endingSymbol = css.substring(length-1);
    if ("\n".equals(endingSymbol))
      return ;
      
    css.append("\n");
  }

  private String getCssResultLocationName(Configuration options, LessSource source) {
    LessSource location = options.getCssResultLocation();
    String name = location == null ? null : location.getName();

    if (name == null)
      name = URIUtils.changeSuffix(source.getName(), Constants.CSS_SUFFIX);

    return name;
  }

}
