package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.KeywordExpression;
import com.github.sommeri.less4j.core.ast.ListExpression;
import com.github.sommeri.less4j.core.ast.ListExpressionOperator;
import com.github.sommeri.less4j.utils.ArraysUtils;


public class ExpressionManipulator {

  public Expression findRightmostListedExpression(Expression expression) {
    Expression result = expression;
    while (result.getType() == ASTCssNodeType.LIST_EXPRESSION) {
      ListExpression parentList = (ListExpression) result;
      result = ArraysUtils.last(parentList.getExpressions());
    }
    return result;
  }
  
  public ListExpression findRightmostSpaceSeparatedList(Expression expression) {
    ListExpression result = null;
    Expression rightmost = expression;
    while (rightmost.getType() == ASTCssNodeType.LIST_EXPRESSION) {
      ListExpression rightmostList = (ListExpression) rightmost;
      if (isSpaceSeparated(rightmostList))
        result = rightmostList;
      
      rightmost = ArraysUtils.last(rightmostList.getExpressions());
    }
    return result;
  }

  public boolean isSpaceSeparatedList(Expression expression) {
    if (expression.getType()!=ASTCssNodeType.LIST_EXPRESSION) {
      return false;
    }
    return isSpaceSeparated((ListExpression)expression);
  }

  public boolean isSpaceSeparated(ListExpression list) {
    return list.getOperator().getOperator()==ListExpressionOperator.Operator.EMPTY_OPERATOR;
  }
  
  public boolean isImportant(Expression expression) {
    if (expression==null)
      return false;
    
    Expression rightmost = findRightmostListedExpression(expression);
    if (rightmost==null || rightmost.getType() != ASTCssNodeType.IDENTIFIER_EXPRESSION)
      return false;
    
    if (!(rightmost instanceof KeywordExpression)) {
      return false;
    }
    
    KeywordExpression identifier = (KeywordExpression)  rightmost;
    return identifier.isImportant(); 
  }

  public Expression cutRightmostListedExpression(Expression expression) {
    Expression rightmost = findRightmostListedExpression(expression);
    ListExpression parent = (ListExpression) rightmost.getParent();
    parent.removeExpression(rightmost);
    rightmost.setParent(null);

    return rightmost;
  }

  public void squashLists(Expression expression) {
    if (expression.getType()!=ASTCssNodeType.LIST_EXPRESSION)
      return ;
    
    ListExpression list = (ListExpression) expression;
    List<Expression> members = list.getExpressions();
    
    for (Expression element : members) {
      squashLists(element);
    }

    ArrayList<Expression> membersCopy = new ArrayList<Expression>(members);
    for (Expression element : membersCopy) if (element.getType()==ASTCssNodeType.LIST_EXPRESSION) {
      ListExpression sublist = (ListExpression) element;
      if (sameListOperator(list, sublist)) {
        replaceInList(list, members, sublist, sublist.getExpressions());
      }
    }
  }

  private void replaceInList(Expression parent, List<Expression> list, Expression oldChild, List<Expression> newChilds) {
    int childsIndex = list.indexOf(oldChild);
    list.remove(oldChild);
    list.addAll(childsIndex, newChilds);
    oldChild.setParent(null);
    for (Expression newKid : newChilds) {
      newKid.setParent(parent);
    }
  }

  private boolean sameListOperator(ListExpression list, ListExpression sublist) {
    return list.getOperator().getOperator()==sublist.getOperator().getOperator();
  }

}
