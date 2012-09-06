package org.porting.less4j.core.compiler;

import java.util.ArrayList;
import java.util.List;

import org.porting.less4j.core.ast.ASTCssNode;
import org.porting.less4j.core.ast.ASTCssNodeType;
import org.porting.less4j.core.ast.Body;
import org.porting.less4j.core.ast.Expression;
import org.porting.less4j.core.ast.IndirectVariable;
import org.porting.less4j.core.ast.Variable;
import org.porting.less4j.core.ast.VariableDeclaration;

public class LessToCssCompiler {

  private ASTManipulator manipulator = new ASTManipulator();
  private ActiveVariableScope activeVariableScope;
  private ExpressionEvaluator expressionEvaluator;

  public ASTCssNode compileToCss(ASTCssNode less) {
    activeVariableScope = new ActiveVariableScope();
    expressionEvaluator = new ExpressionEvaluator(activeVariableScope);

    solveVariables(less);
    return less;
  }

  private void solveVariables(ASTCssNode node) {
    boolean hasOwnScope = hasOwnScope(node);
    if (hasOwnScope)
      increaseScope(node);

    switch (node.getType()) {
    case VARIABLE_DECLARATION: {
      activeVariableScope.addDeclaration((VariableDeclaration) node); //TODO no reason to go further
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
