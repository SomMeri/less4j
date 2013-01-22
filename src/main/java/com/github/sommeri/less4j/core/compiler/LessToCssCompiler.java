package com.github.sommeri.less4j.core.compiler;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.Declaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.Media;
import com.github.sommeri.less4j.core.ast.MediaExpression;
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
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class LessToCssCompiler {

  private ProblemsHandler problemsHandler;

  public LessToCssCompiler(ProblemsHandler problemsHandler) {
    super();
    this.problemsHandler = problemsHandler;
  }

  public ASTCssNode compileToCss(StyleSheet less, File baseDirectory) {
    SimpleImportsSolver importsSolver = new SimpleImportsSolver(problemsHandler);
    importsSolver.solveImports(less, baseDirectory);

    ScopeExtractor scopeBuilder = new ScopeExtractor();
    Scope scope = scopeBuilder.extractScope(less);
    
    ReferencesSolver referencesSolver = new ReferencesSolver(problemsHandler);
    referencesSolver.solveReferences(less, scope);

    //just a safety measure
    evaluateExpressions(less);
    bubbleAndMergeMedia(less);
    freeNestedRuleSets(less);
    sortTopLevelElements(less);

    return less;
  }

  private void bubbleAndMergeMedia(StyleSheet less) {
    MediaBubblerAndMerger bubblerAndMerger = new MediaBubblerAndMerger();
    bubblerAndMerger.bubbleAndMergeMedia(less);
  }

  private void sortTopLevelElements(StyleSheet less) {
    Collections.sort(less.getChilds(), new Comparator<ASTCssNode>() {

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

  private void freeNestedRuleSets(Body<ASTCssNode> body) {
    NestedRulesCollector nestedRulesCollector = new NestedRulesCollector();

    List<? extends ASTCssNode> childs = new ArrayList<ASTCssNode>(body.getChilds());
    for (ASTCssNode kid : childs) {
      switch (kid.getType()) {
      case RULE_SET: {
        List<RuleSet> nestedRulesets = nestedRulesCollector.collectNestedRuleSets((RuleSet) kid);
        body.addMembersAfter(nestedRulesets, kid);
        for (RuleSet ruleSet : nestedRulesets) {
          ruleSet.setParent(body);
        }
        break;
      }
      case MEDIA: {
        freeNestedRuleSets((Media) kid);
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
        case MEDIA_EXPRESSION:
          evaluateInMediaExpressions((MediaExpression) kid);
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
    if (!node.isFontDeclaration() && !node.isFilterDeclaration()) {
      evaluateExpressions(node);
      return;
    }
  }

  private void evaluateInMediaExpressions(MediaExpression node) {
    MediaExpressionFeature feature = node.getFeature();
    if (!feature.isRatioFeature()) {
      evaluateExpressions(node);
      return;
    }
  }

}
