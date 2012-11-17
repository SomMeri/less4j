package com.github.sommeri.less4j.core;

import java.util.List;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.compiler.LessToCssCompiler;
import com.github.sommeri.less4j.core.parser.ANTLRParser;
import com.github.sommeri.less4j.core.parser.ANTLRPhaseErrors;
import com.github.sommeri.less4j.core.parser.ASTBuilder;
import com.github.sommeri.less4j.utils.CssPrinter;

public class ThreadUnsafeLessCompiler implements LessCompiler {
  private ProblemsCollector problemsCollector = new ProblemsCollector();
  private ANTLRParser parser = new ANTLRParser();
  private ASTBuilder astBuilder = new ASTBuilder(problemsCollector);
  private LessToCssCompiler compiler = new LessToCssCompiler();

  @Override
  public String compile(String lessContent) throws Less4jException {
    try {
      return doCompile(lessContent);
    } catch (TranslationException ex) {
      throw new Less4jException(ex);
    }
  }

  private String doCompile(String lessContent) {
    // FIXME: ugly, clean hierarchy and dependencies
    problemsCollector.clean();
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

  @SuppressWarnings("unused")
  private List<Problem> getProblems() {
    return problemsCollector.getWarnings();
  }
  
}

