package com.github.sommeri.less4j.core;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.compiler.LessToCssCompiler;
import com.github.sommeri.less4j.core.compiler.problems.ProblemsHandler;
import com.github.sommeri.less4j.core.parser.ANTLRParser;
import com.github.sommeri.less4j.core.parser.ANTLRPhaseErrors;
import com.github.sommeri.less4j.core.parser.ASTBuilder;
import com.github.sommeri.less4j.utils.CssPrinter;

public class ThreadUnsafeLessCompiler implements LessCompiler {
  private ProblemsHandler problemsHandler;
  private ASTBuilder astBuilder;
  private LessToCssCompiler compiler;

  private ANTLRParser parser = new ANTLRParser();

  @Override
  public CompilationResult compile(String lessContent) throws Less4jException {
    try {
      problemsHandler = new ProblemsHandler();
      astBuilder = new ASTBuilder(problemsHandler);
      compiler = new LessToCssCompiler(problemsHandler);
      String css = doCompile(lessContent);
      CompilationResult compilationResult = new CompilationResult(css, problemsHandler.getWarnings());
      if (problemsHandler.hasErrors()) {
        throw new Less4jException(problemsHandler.getErrors(), compilationResult);
      }
      return compilationResult;
    } catch (TranslationException ex) {
      throw new Less4jException(ex);
    }
  }

  private String doCompile(String lessContent) {
    // FIXME: ugly, clean hierarchy and dependencies
    ExtendedStringBuilder stringBuilder = new ExtendedStringBuilder("");

    ANTLRParser.ParseResult result = parser.parseStyleSheet(lessContent);
    if (result.hasErrors()) {
      throw new ANTLRPhaseErrors(result.getErrors());
    }
    StyleSheet lessStyleSheet = astBuilder.parse(result.getTree());
    ASTCssNode cssStyleSheet = compiler.compileToCss(lessStyleSheet);
    
    CssPrinter builder = new CssPrinter(stringBuilder);
    builder.append(cssStyleSheet);
    return stringBuilder.toString();
  };

}

