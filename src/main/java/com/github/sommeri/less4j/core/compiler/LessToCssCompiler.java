package com.github.sommeri.less4j.core.compiler;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Declaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FixedMediaExpression;
import com.github.sommeri.less4j.core.ast.MediaExpressionFeature;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionsEvaluator;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.selectors.ExtendsSolver;
import com.github.sommeri.less4j.core.compiler.selectors.UselessLessElementsRemover;
import com.github.sommeri.less4j.core.compiler.stages.ASTManipulator;
import com.github.sommeri.less4j.core.compiler.stages.EmptyBodiesRemover;
import com.github.sommeri.less4j.core.compiler.stages.ImportsAndScopeSolver;
import com.github.sommeri.less4j.core.compiler.stages.MediaBubblerAndMerger;
import com.github.sommeri.less4j.core.compiler.stages.PropertiesMerger;
import com.github.sommeri.less4j.core.compiler.stages.ReferencesSolver;
import com.github.sommeri.less4j.core.compiler.stages.UnNestingAndBubbling;
import com.github.sommeri.less4j.core.compiler.stages.UrlsAndImportsNormalizer;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.core.validators.CssAstValidator;

public class LessToCssCompiler {

  private ProblemsHandler problemsHandler;
  private Configuration configuration;

  public LessToCssCompiler(ProblemsHandler problemsHandler, Configuration configuration) {
    super();
    this.problemsHandler = problemsHandler;
    this.configuration = configuration;
  }

  public ASTCssNode compileToCss(StyleSheet less, LessSource source, Configuration options) {
    resolveImportsAndReferences(less, source);
    
    evaluateExpressions(less);
    freeNestedRulesetsAndMedia(less);
    solveExtends(less);

    finalMediaMergingAndBubbling(less);
    removeUselessLessElements(less);
    removeEmptyRulesetsAndMedia(less);
    mergeMergingProperties(less);
    //normalizeUrlsAndImportsInImportedFiles(less);

    //final clean up  
    sortTopLevelElements(less);
    removeUselessCharsets(less);

    //final validation
    validateFinalCss(less);

    return less;
  }

  private void removeEmptyRulesetsAndMedia(StyleSheet less) {
    EmptyBodiesRemover remover = new EmptyBodiesRemover();
    remover.removeEmptyBodies(less);
  }

  private void mergeMergingProperties(StyleSheet less) {
    PropertiesMerger propertiesMerger = new PropertiesMerger();
    propertiesMerger.propertiesMerger(less);
  }

  private void solveExtends(StyleSheet less) {
    ExtendsSolver extendsSolver = new ExtendsSolver();
    extendsSolver.solveExtends(less);
  }

  private void freeNestedRulesetsAndMedia(StyleSheet less) {
    UnNestingAndBubbling nestingBubbling = new UnNestingAndBubbling();
    nestingBubbling.unnestRulesetAndMedia(less);
  }

  private void resolveImportsAndReferences(StyleSheet less, LessSource source) {
    ImportsAndScopeSolver solver = new ImportsAndScopeSolver(problemsHandler, configuration);
    IScope scope = solver.buildImportsAndScope(less, source);

    ReferencesSolver referencesSolver = new ReferencesSolver(problemsHandler, configuration);
    referencesSolver.solveReferences(less, scope);
    // Warning at this point: ast changed, but the scope did not changed its structure. The scope stopped to be useful. 
  }

  private void removeUselessLessElements(StyleSheet node) {
    UselessLessElementsRemover remover = new UselessLessElementsRemover();
    remover.removeUselessLessElements(node);
  }

  @SuppressWarnings("unused")
  private void normalizeUrlsAndImportsInImportedFiles(StyleSheet node) {
    UrlsAndImportsNormalizer normalizer = new UrlsAndImportsNormalizer(problemsHandler, configuration);
    normalizer.normalizeUrlsAndImports(node);
  }

  private void removeUselessCharsets(StyleSheet less) {
    ASTManipulator astManipulator = new ASTManipulator();
    Iterator<ASTCssNode> iterator = less.getChilds().iterator();
    if (!iterator.hasNext())
      return;

    ASTCssNode node = iterator.next();
    if (node.getType() != ASTCssNodeType.CHARSET_DECLARATION)
      return;

    if (!iterator.hasNext())
      return;

    while (iterator.hasNext()) {
      node = iterator.next();
      if (node.getType() != ASTCssNodeType.CHARSET_DECLARATION)
        return;

      astManipulator.removeFromBody(node);
    }
  }

  private void finalMediaMergingAndBubbling(StyleSheet less) {
    MediaBubblerAndMerger bubblerAndMerger = new MediaBubblerAndMerger(problemsHandler);
    bubblerAndMerger.bubbleAndMergeMedia(less);
  }

  private void sortTopLevelElements(StyleSheet less) {
    Collections.sort(less.getMembers(), new Comparator<ASTCssNode>() {

      @Override
      public int compare(ASTCssNode first, ASTCssNode second) {
        return code(first) - code(second);
      }

      private int code(ASTCssNode node) {
        if (node.getType() == ASTCssNodeType.CHARSET_DECLARATION)
          return 0;
        if (node.getType() == ASTCssNodeType.IMPORT)
          return 1;

        return 2;
      }
    });
  }

  private void evaluateExpressions(ASTCssNode node) {
    ASTManipulator manipulator = new ASTManipulator();
    //variables are not supposed to be there now
    ExpressionsEvaluator expressionEvaluator = new ExpressionsEvaluator(problemsHandler, configuration);

    if (node instanceof Expression) {
      Expression value = expressionEvaluator.evaluate((Expression) node);
      manipulator.replace(node, value);
    } else {
      List<? extends ASTCssNode> childs = node.getChilds();
      for (ASTCssNode kid : childs) {
        switch (kid.getType()) {
        case FIXED_MEDIA_EXPRESSION:
          evaluateInMediaExpressions((FixedMediaExpression) kid);
          break;

        case DECLARATION:
          evaluateInDeclaration((Declaration) kid);
          break;

        default:
          evaluateExpressions(kid);
          break;
        }

      }
    }
  }

  private void evaluateInDeclaration(Declaration node) {
    if (!node.isFontDeclaration()) {
      evaluateExpressions(node);
      return;
    }
  }

  private void evaluateInMediaExpressions(FixedMediaExpression node) {
    MediaExpressionFeature feature = node.getFeature();
    if (!feature.isRatioFeature()) {
      evaluateExpressions(node);
      return;
    }
  }

  private void validateFinalCss(StyleSheet less) {
    CssAstValidator validator = new CssAstValidator(problemsHandler);
    validator.validate(less);
  }

}
