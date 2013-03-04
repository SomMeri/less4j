package com.github.sommeri.less4j.core.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.Declaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.Media;
import com.github.sommeri.less4j.core.ast.FixedMediaExpression;
import com.github.sommeri.less4j.core.ast.MediaExpressionFeature;
import com.github.sommeri.less4j.core.ast.Page;
import com.github.sommeri.less4j.core.ast.PageMarginBox;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionEvaluator;
import com.github.sommeri.less4j.core.compiler.scopes.Scope;
import com.github.sommeri.less4j.core.compiler.stages.ASTManipulator;
import com.github.sommeri.less4j.core.compiler.stages.MediaBubblerAndMerger;
import com.github.sommeri.less4j.core.compiler.stages.NestedRulesCollector;
import com.github.sommeri.less4j.core.compiler.stages.ReferencesSolver;
import com.github.sommeri.less4j.core.compiler.stages.ScopeExtractor;
import com.github.sommeri.less4j.core.compiler.stages.SimpleImportsSolver;
import com.github.sommeri.less4j.core.compiler.stages.UrlsAndImportsNormalizer;
import com.github.sommeri.less4j.core.compiler.stages.UselessLessElementsRemover;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.core.validators.CssAstValidator;

public class LessToCssCompiler {

  private ProblemsHandler problemsHandler;
  ASTManipulator astManipulator = new ASTManipulator();

  public LessToCssCompiler(ProblemsHandler problemsHandler) {
    super();
    this.problemsHandler = problemsHandler;
  }

  public ASTCssNode compileToCss(StyleSheet less, LessSource source) {
    SimpleImportsSolver importsSolver = new SimpleImportsSolver(problemsHandler);
    importsSolver.solveImports(less, source);

    ScopeExtractor scopeBuilder = new ScopeExtractor();
    Scope scope = scopeBuilder.extractScope(less);

    ReferencesSolver referencesSolver = new ReferencesSolver(problemsHandler);
    referencesSolver.solveReferences(less, scope);

    //just a safety measure
    evaluateExpressions(less);
    bubbleAndMergeMedia(less);
    freeNestedRuleSets(less);
    removeUselessLessElements(less);
    normalizeUrlsAndImportsInImportedFiles(less);

    //final clean up  
    sortTopLevelElements(less);
    removeUselessCharsets(less);

    //final validation
    validateFinalCss(less);

    return less;
  }

  private void removeUselessLessElements(StyleSheet node) {
    UselessLessElementsRemover remover = new UselessLessElementsRemover();
    remover.removeUselessLessElements(node);
  }

  private void normalizeUrlsAndImportsInImportedFiles(StyleSheet node) {
    UrlsAndImportsNormalizer normalizer = new UrlsAndImportsNormalizer(problemsHandler);
    normalizer.normalizeUrlsAndImports(node);
  }

  private void removeUselessCharsets(StyleSheet less) {
    ASTManipulator astManipulator = new ASTManipulator();
    Iterator<ASTCssNode> iterator = less.getChilds().iterator();
    if (!iterator.hasNext())
      return ;
    
    ASTCssNode node = iterator.next();
    if (node.getType() != ASTCssNodeType.CHARSET_DECLARATION)
      return;

    if (!iterator.hasNext())
      return ;

    while (iterator.hasNext()) {
      node = iterator.next();
      if (node.getType() != ASTCssNodeType.CHARSET_DECLARATION)
        return;
      
      astManipulator.removeFromBody(node);
    }
  }

  private void bubbleAndMergeMedia(StyleSheet less) {
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

  private void freeNestedRuleSets(Body body) {
    NestedRulesCollector nestedRulesCollector = new NestedRulesCollector();

    List<? extends ASTCssNode> childs = new ArrayList<ASTCssNode>(body.getChilds());
    for (ASTCssNode kid : childs) {
      switch (kid.getType()) {
      case RULE_SET: {
        List<RuleSet> nestedRulesets = nestedRulesCollector.collectNestedRuleSets((RuleSet) kid);
        astManipulator.addIntoBody(nestedRulesets, kid);
        break;
      }
      case MEDIA: {
        freeNestedRuleSets(((Media) kid).getBody());
        break;
      }
      case PAGE: {
        Page page = (Page) kid;
        freeNestedRuleSets(page.getBody());
        break;
      }
      case PAGE_MARGIN_BOX: {
        PageMarginBox marginBox = (PageMarginBox) kid;
        freeNestedRuleSets(marginBox.getBody());
        break;
      }
      }
    }
  }

  private void evaluateExpressions(ASTCssNode node) {
    ASTManipulator manipulator = new ASTManipulator();
    //variables are not supposed to be there now
    ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(problemsHandler);

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
