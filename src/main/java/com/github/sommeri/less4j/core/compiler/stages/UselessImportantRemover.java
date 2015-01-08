package com.github.sommeri.less4j.core.compiler.stages;

import java.util.List;
import java.util.ListIterator;

import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.Declaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.KeywordExpression;
import com.github.sommeri.less4j.core.ast.ListExpression;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionManipulator;
import com.github.sommeri.less4j.utils.ArraysUtils;

/**
 * Preconditions: 
 * 1.) properties names must be interpolated,
 * 2.) rulesets bodies must be fully solved.
 * 3.) properties were merged   
 *
 */
public class UselessImportantRemover extends TreeDeclarationsVisitor {

  private ExpressionManipulator expressionManipulator = new ExpressionManipulator();

  public UselessImportantRemover() {
  }


  protected void applyToDeclaration(Declaration declaration) {
    Expression expression = declaration.getExpression();
    if (!expressionManipulator.isImportant(expression))
      return ;
    
    expressionManipulator.squashLists(expression);
    
    ListExpression list = expressionManipulator.findRightmostSpaceSeparatedList(expression);
    if (list==null)
      return ;
    
    List<Expression> expressions = list.getExpressions();
    if (!isImportant(ArraysUtils.last(expressions)))
      return ;
      
    ListIterator<Expression> iterator = expressions.listIterator(expressions.size());
    
    int countImportant = 0;
    while (iterator.hasPrevious()) {
      if (isImportant(iterator.previous()))
        countImportant++;
    }
    
    if (countImportant<=1)
      return ;
    
    int removalIndex = expressions.size()-countImportant;
    for (int i=countImportant-1; i>0;i--) {
      list.removeExpression(removalIndex);
    }
  }

  private boolean isImportant(Expression last) {
    if (last==null || !(last instanceof KeywordExpression))
      return false;
    
    return ((KeywordExpression) last).isImportant();
  }


  protected void enteringBody(Body node) {
  }

  protected void leavingBody(Body node) {
  }

}
