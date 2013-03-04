package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Import;
import com.github.sommeri.less4j.core.compiler.expressions.TypesConversionUtils;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class UrlsAndImportsNormalizer {

  private TypesConversionUtils conversionUtils = new TypesConversionUtils();
  private final ProblemsHandler problemsHandler;
  
  public UrlsAndImportsNormalizer(ProblemsHandler problemsHandler) {
    this.problemsHandler = problemsHandler;
  }

  public void normalizeUrlsAndImports(ASTCssNode node) {
    switch (node.getType()) {
    case IMPORT:
      normalizeImport((Import)node);
      break;

    default:
      List<ASTCssNode> childs = new ArrayList<ASTCssNode>(node.getChilds());
      for (ASTCssNode kid : childs) {
        normalizeUrlsAndImports(kid);
      }
    }
  }

  private void normalizeImport(Import node) {
    String extractFilename = conversionUtils.extractFilename(node.getUrlExpression(), problemsHandler);
    // TODO Auto-generated method stub
    
  }

}
