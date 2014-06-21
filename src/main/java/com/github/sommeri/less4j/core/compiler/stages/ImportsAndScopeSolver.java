package com.github.sommeri.less4j.core.compiler.stages;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Import;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.PlaceholderScope;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class ImportsAndScopeSolver {

  private final ProblemsHandler problemsHandler;
  private final Configuration configuration;
  private SingleImportSolver importsSolver;

  public ImportsAndScopeSolver(ProblemsHandler problemsHandler, Configuration configuration) {
    this.problemsHandler = problemsHandler;
    this.configuration = configuration;
  }

  public IScope buildImportsAndScope(StyleSheet less, LessSource source) {
    importsSolver = new SingleImportSolver(problemsHandler, configuration);

    InitialScopeExtractor scopeBuilder = new InitialScopeExtractor();
    IScope scope = scopeBuilder.extractScope(less);
    List<PlaceholderScope> importsPlaceholders = scopeBuilder.getImportsPlaceholders();

    solveNestedImports(importsPlaceholders);

    return scope;
  }

  private void solveNestedImports(List<PlaceholderScope> importsPlaceholders) {
    for (PlaceholderScope placeholder : importsPlaceholders) {
      List<PlaceholderScope> nextLevelOfImports = importIntoPlaceholder(placeholder);
      if (!nextLevelOfImports.isEmpty())
        solveNestedImports(nextLevelOfImports);
    }
  }

  private List<PlaceholderScope> importIntoPlaceholder(PlaceholderScope placeholder) {
    Import encounteredImport = (Import) placeholder.getOwner();
    ReferencesSolver referencesSolver = new ReferencesSolver(problemsHandler, configuration);
    referencesSolver.solveReferences(encounteredImport, placeholder.getParent());

    ASTCssNode importedAst = importsSolver.importEncountered(encounteredImport, placeholder.getOwner().getSource());
    if (importedAst != null) {
      InitialScopeExtractor importedAstScopeBuilder = new InitialScopeExtractor();
      IScope addThisIntoScopeTree = importedAstScopeBuilder.extractScope(importedAst);
      
      placeholder.replaceSelf(addThisIntoScopeTree);
      return importedAstScopeBuilder.getImportsPlaceholders();
    } else {
      placeholder.removeSelf();
    }
    
    return Collections.emptyList();
  }

}
