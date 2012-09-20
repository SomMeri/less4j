package org.porting.less4j.core.compiler;

import java.util.ArrayList;
import java.util.List;

import org.porting.less4j.core.ast.ASTCssNode;
import org.porting.less4j.core.ast.ASTCssNodeType;
import org.porting.less4j.core.ast.Body;
import org.porting.less4j.core.ast.Declaration;
import org.porting.less4j.core.ast.Expression;
import org.porting.less4j.core.ast.IndirectVariable;
import org.porting.less4j.core.ast.MediaExpression;
import org.porting.less4j.core.ast.MediaExpressionFeature;
import org.porting.less4j.core.ast.RuleSet;
import org.porting.less4j.core.ast.StyleSheet;
import org.porting.less4j.core.ast.Variable;
import org.porting.less4j.core.ast.VariableDeclaration;

public class LessToCssCompiler {

  private ASTManipulator manipulator = new ASTManipulator();
  private ActiveVariableScope activeVariableScope;
  private ExpressionEvaluator expressionEvaluator;
  private NestedRulesCollector nestedRulesCollector;

  public ASTCssNode compileToCss(StyleSheet less) {
    activeVariableScope = new ActiveVariableScope();
    expressionEvaluator = new ExpressionEvaluator(activeVariableScope);
    nestedRulesCollector = new NestedRulesCollector();

    solveVariables(less);
    evaluateExpressions(less);
    freeNestedRuleSets(less);

    return less;
  }

  private void freeNestedRuleSets(StyleSheet sheet) {
    List<? extends ASTCssNode> childs = new ArrayList<ASTCssNode>(sheet.getChilds());
    for (ASTCssNode kid : childs) {
      if (kid.getType() == ASTCssNodeType.RULE_SET) {
        List<RuleSet> nestedRulesets = nestedRulesCollector.collectNestedRuleSets((RuleSet)kid);
        sheet.addMembersAfter(nestedRulesets, kid);
        for (RuleSet ruleSet : nestedRulesets) {
          ruleSet.setParent(sheet);
        }
      }

    }
  }

  private void evaluateExpressions(ASTCssNode node) {
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
    if (!node.isFontDeclaration()) {
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

  private void solveVariables(ASTCssNode node) {
    boolean hasOwnScope = hasOwnScope(node);
    if (hasOwnScope)
      increaseScope(node);

    switch (node.getType()) {
    case VARIABLE_DECLARATION: {
      activeVariableScope.addDeclaration((VariableDeclaration) node); //no reason to go further
      break;
    }
    case VARIABLE: {
      Expression replacement = expressionEvaluator.evaluate((Variable) node);
      manipulator.replace(node, replacement);
      break;
    }
    case INDIRECT_VARIABLE: {
      Expression replacement = expressionEvaluator.evaluate((IndirectVariable) node);
      manipulator.replace(node, replacement);
      break;
    }
    }

    List<? extends ASTCssNode> childs = node.getChilds();
    List<ASTCssNode> childsToBeRemoved = new ArrayList<ASTCssNode>();
    for (ASTCssNode kid : childs) {
      solveVariables(kid);
      if (kid.getType() == ASTCssNodeType.VARIABLE_DECLARATION) {
        childsToBeRemoved.add(kid);
      }
    }

    for (ASTCssNode kid : childsToBeRemoved) {
      manipulator.removeFromBody(kid);
    }

    if (hasOwnScope)
      decreaseScope(node);
  }

  private void decreaseScope(ASTCssNode node) {
    activeVariableScope.decreaseScope();
  }

  private void increaseScope(ASTCssNode node) {
    activeVariableScope.increaseScope();
  }

  private boolean hasOwnScope(ASTCssNode node) {
    return (node instanceof Body);
  }

}
