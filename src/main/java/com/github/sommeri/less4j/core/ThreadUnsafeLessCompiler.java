package com.github.sommeri.less4j.core;

import java.io.File;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.LessSource.CannotReadFile;
import com.github.sommeri.less4j.LessSource.FileNotFound;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.ast.VariableDeclaration;
import com.github.sommeri.less4j.core.compiler.LessToCssCompiler;
import com.github.sommeri.less4j.core.parser.ANTLRParser;
import com.github.sommeri.less4j.core.parser.ANTLRParser.ParseResult;
import com.github.sommeri.less4j.core.parser.ASTBuilder;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.GeneralProblem;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.core.problems.UnableToFinish;
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
    return compile(new LessSource.FileSource(lessFile, "utf-8"), options);
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
    if (options == null)
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
    StyleSheet lessStyleSheet = null;
    if (options != null && options.getCache() != null) {
      lessStyleSheet = (StyleSheet) options.getCache().getAst(source);
      if (lessStyleSheet != null) {
        lessStyleSheet = lessStyleSheet.clone(); // need to leave cached version unchanged
      }
    }
    if (lessStyleSheet == null) {
      ParseResult result = toAntlrTree(source);
      lessStyleSheet = astBuilder.parseStyleSheet(result.getTree());
      if (options != null && options.getCache() != null) {
        options.getCache().setAst(source, lessStyleSheet);
        lessStyleSheet = lessStyleSheet.clone(); // need to leave cached version unchanged
      }
    }

    Map<String, HiddenTokenAwareTree> variables = toAntlrTree(options.getVariables());
    List<VariableDeclaration> externalVariables = astBuilder.parseVariables(variables);
    lessStyleSheet.addMembers(externalVariables);
    lessStyleSheet.configureParentToAllChilds();

    try {
      ASTCssNode cssStyleSheet = compiler.compileToCss(lessStyleSheet, source, options);
      CompilationResult compilationResult = createCompilationResult(cssStyleSheet, source, externalVariables, compiler.getImportedsources(), options);
      return compilationResult;
    } catch (UnableToFinish ex) {
      problemsHandler.unableToFinish(lessStyleSheet, ex);
      return createEmptyCompilationResult();
    }
  }

  private ParseResult toAntlrTree(LessSource source) throws Less4jException {
    ParseResult result;
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

  private Map<String, HiddenTokenAwareTree> toAntlrTree(Map<String, String> variables) throws Less4jException {
    Map<String, HiddenTokenAwareTree> result = new HashMap<String, HiddenTokenAwareTree>();
    List<Problem> problems = new ArrayList<Problem>();
    for (Entry<String, String> entry : variables.entrySet()) {
      String nameStr = entry.getKey();
      String valueStr = entry.getValue();
      ParseResult valueParseResult = toAntlrExpressionTree(nameStr, valueStr);

      problems.addAll(valueParseResult.getErrors());
      result.put(nameStr, valueParseResult.getTree());
    }

    if (!problems.isEmpty()) {
      CompilationResult compilationResult = new CompilationResult("Errors parsing custom variables, partial result is not available.");
      throw new Less4jException(problems, compilationResult);
    }

    return result;
  }

  private ParseResult toAntlrExpressionTree(String dummySourceName, String expression) {
    LessSource source = new DummyLessSource(dummySourceName, expression);
    return parser.parseFullExpression(expression, source);
  }

  private CompilationResult createCompilationResult(ASTCssNode cssStyleSheet, LessSource lessSource, List<VariableDeclaration> externalVariables, Collection<LessSource> additionalSourceFiles, Configuration options) {
    LessSource cssDestination = options == null ? null : options.getCssResultLocation();
    if (cssDestination == null) {
      String guessedCssName = URIUtils.changeSuffix(lessSource.getName(), Constants.CSS_SUFFIX);
      URI guessedURI = URIUtils.changeSuffix(lessSource.getURI(), Constants.CSS_SUFFIX);
      cssDestination = new LessSource.StringSource("", guessedCssName, guessedURI);
    }

    CssPrinter builder = new CssPrinter(lessSource, cssDestination, extractSources(externalVariables), additionalSourceFiles, options);
    builder.append(cssStyleSheet);
    StringBuilder css = builder.toCss();
    String sourceMap = builder.toSourceMap();

    handleSourceMapLink(cssStyleSheet, css, options, lessSource, sourceMap);

    CompilationResult compilationResult = new CompilationResult(css.toString(), sourceMap, problemsHandler.getWarnings());
    return compilationResult;
  }

  private List<LessSource> extractSources(List<VariableDeclaration> externalVariables) {
    List<LessSource> result = new ArrayList<LessSource>();
    for (VariableDeclaration variableDeclaration : externalVariables) {
      result.add(variableDeclaration.getSource());
    }
    return result;
  }

  private CompilationResult createEmptyCompilationResult() {
    CompilationResult compilationResult = new CompilationResult("", null, problemsHandler.getWarnings());
    return compilationResult;
  }

  private void handleSourceMapLink(ASTCssNode cssAst, StringBuilder css, Configuration options, LessSource source, String sourceMap) {
    String cssResultLocation = getCssResultLocationName(options, source);
    LessCompiler.SourceMapConfiguration sourceMapConfiguration = options.getSourceMapConfiguration();
    if (!sourceMapConfiguration.shouldLinkSourceMap() && !sourceMapConfiguration.isInline())
      return;

    if (!sourceMapConfiguration.isInline() && cssResultLocation == null) {
      problemsHandler.warnSourceMapLinkWithoutCssResultLocation(cssAst);
      return;
    }

    addNewLine(css);

    String commentText;
    String encodingCharset = sourceMapConfiguration.getEncodingCharset();
    if (sourceMapConfiguration.isInline()) {
      String encodedSourceMap = PrintUtils.base64Encode(sourceMap, encodingCharset, problemsHandler, cssAst);
      commentText = "/*# sourceMappingURL=data:application/json;base64," + encodedSourceMap + " */";
    } else {
      // compose linking comment
      String url = sourceMapConfiguration.getSourceMapNameGenerator().generateUrl(cssResultLocation);
      String encodedUrl = PrintUtils.urlEncode(url, encodingCharset, problemsHandler, cssAst);
      commentText = "/*# sourceMappingURL=" + encodedUrl + " */";
    }

    css.append(commentText).append("\n");
  }

  private void addNewLine(StringBuilder css) {
    if (css == null)
      return;

    int length = css.length();
    if (length == 0) {
      css.append("\n");
      return;
    }
    String endingSymbol = css.substring(length - 1);
    if ("\n".equals(endingSymbol))
      return;

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

class DummyLessSource extends LessSource {

  private final String content;
  private final String name;

  public DummyLessSource(String name, String content) {
    super();
    this.name = name;
    this.content = content;
  }

  @Override
  public LessSource relativeSource(String filename) throws FileNotFound, CannotReadFile, StringSourceException {
    return this;
  }

  @Override
  public String getContent() throws FileNotFound, CannotReadFile {
    return content;
  }

  @Override
  public byte[] getBytes() throws FileNotFound, CannotReadFile {
    return content == null ? null : content.getBytes();
  }

  public String getName() {
    return name;
  }

}
