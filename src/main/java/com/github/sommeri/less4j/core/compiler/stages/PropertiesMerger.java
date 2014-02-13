package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.ComposedExpression;
import com.github.sommeri.less4j.core.ast.Declaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.ExpressionOperator;
import com.github.sommeri.less4j.core.ast.RuleSet;

/**
 * Preconditions: 
 * 1.) properties names must be interpolated,
 * 2.) rulesets bodies must be fully solved.   
 *
 */
public class PropertiesMerger {

  private Map<String, Declaration> mergingProperties = new HashMap<String, Declaration>();
  private ASTManipulator manipulator = new ASTManipulator();

  public PropertiesMerger() {
  }

  public void propertiesMerger(ASTCssNode node) {
    switch (node.getType()) {
    case RULE_SET:
      RuleSet ruleset = (RuleSet) node;
      rulesetsBodyPropertiesMerger(ruleset.getBody());
      break;

    case CHARSET_DECLARATION:
    case IMPORT:
      break;

    default:
      mergeKidsProperties(node);
    }

  }

  private void mergeKidsProperties(ASTCssNode node) {
    List<ASTCssNode> childs = new ArrayList<ASTCssNode>(node.getChilds());
    for (ASTCssNode kid : childs) {
      propertiesMerger(kid);
    }
  }

  private void rulesetsBodyPropertiesMerger(Body node) {
    enteringBody(node);

    List<? extends ASTCssNode> childs = node.getChilds();
    for (ASTCssNode kid : childs) {
      switch (kid.getType()) {
      case DECLARATION:
        Declaration declaration = (Declaration) kid;
        if (declaration.isMerging())
          addToPrevious(declaration);
        break;

      default:
        mergeKidsProperties(kid);
        break;
      }
    }

    //leavingRuleset(node);
  }

  private void addToPrevious(Declaration declaration) {
    if (declaration.getExpression() == null)
      return;

    String key = toMergingPropertiesKey(declaration);
    if (mergingProperties.containsKey(key)) {
      Declaration previousDeclaration = mergingProperties.get(key);
      Expression previousExpression = previousDeclaration.getExpression();
      Expression composedExpression = new ComposedExpression(declaration.getUnderlyingStructure(), previousExpression, new ExpressionOperator(declaration.getUnderlyingStructure(), declaration.getMergeOperator()), declaration.getExpression());
      previousDeclaration.setExpression(composedExpression);
      composedExpression.setParent(previousDeclaration);
      manipulator.removeFromBody(declaration);
    } else {
      mergingProperties.put(key, declaration);
    }
  }

  private String toMergingPropertiesKey(Declaration declaration) {
    String cssPropertyName = declaration.getNameAsString();
    boolean important = declaration.isImportant();
    return cssPropertyName + " " + important;
  }

  private void enteringBody(Body node) {
    mergingProperties = new HashMap<String, Declaration>();
  }
}
