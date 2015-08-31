package com.github.sommeri.less4j.core.compiler.stages;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Import;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.PlaceholderScope;
import com.github.sommeri.less4j.core.compiler.stages.SingleImportSolver.AlreadyImportedSources;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class ImportsAndScopeSolver {

  private final ProblemsHandler problemsHandler;
  private final Configuration configuration;
  private SingleImportSolver importsSolver;
  private Set<LessSource> allImportedSources;

  public ImportsAndScopeSolver(ProblemsHandler problemsHandler, Configuration configuration) {
    this.problemsHandler = problemsHandler;
    this.configuration = configuration;
  }

  public IScope buildImportsAndScope(StyleSheet less, LessSource source) {
    importsSolver = new SingleImportSolver(problemsHandler, configuration);
    allImportedSources = new HashSet<LessSource>(); 

    InitialScopeExtractor scopeBuilder = new InitialScopeExtractor();
    IScope scope = scopeBuilder.extractScope(less);
    List<PlaceholderScope> importsPlaceholders = scopeBuilder.getImportsPlaceholders();

    solveNestedImports(importsPlaceholders);

    return scope;
  }

  private void solveNestedImports(List<PlaceholderScope> importsPlaceholders) {
    solveNestedImports(importsPlaceholders, new AlreadyImportedSources(allImportedSources));
  }

  private void solveNestedImports(List<PlaceholderScope> importsPlaceholders, AlreadyImportedSources alreadyImportedSources) {
    for (PlaceholderScope placeholder : importsPlaceholders) {
      List<PlaceholderScope> nextLevelOfImports = importIntoPlaceholder(placeholder, alreadyImportedSources);
      if (!nextLevelOfImports.isEmpty()) {
        Import processedImport = (Import) placeholder.getOwner();
        if (processedImport.isImportMultiple())
          solveNestedImports(nextLevelOfImports);
        else
          solveNestedImports(nextLevelOfImports, alreadyImportedSources);
      }
    }
  }

  private List<PlaceholderScope> importIntoPlaceholder(PlaceholderScope placeholder, AlreadyImportedSources alreadyImportedSources) {
    Import encounteredImport = (Import) placeholder.getOwner();
    ReferencesSolver referencesSolver = new ReferencesSolver(problemsHandler, configuration);
    referencesSolver.solveReferences(encounteredImport, placeholder.getParent());

    ASTCssNode importedAst = importsSolver.importEncountered(encounteredImport, placeholder.getOwner().getSource(), alreadyImportedSources);
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

  public Set<LessSource> getImportedSources() {
    return allImportedSources;
  }

}
