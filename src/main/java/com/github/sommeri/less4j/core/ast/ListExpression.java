package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ListExpressionOperator.Operator;
import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class ListExpression extends Expression {

  private List<Expression> expressions;
  private ListExpressionOperator operator;

  public ListExpression(HiddenTokenAwareTree token, List<Expression> expressions, ListExpressionOperator operator) {
    super(token);
    this.expressions = expressions;
    this.operator = operator;
  }

  public ListExpression(HiddenTokenAwareTree token, List<Expression> expressions, ListExpressionOperator operator, IScope scope) {
    this(token, expressions, operator);
    setScope(scope);
  }

  public ListExpressionOperator getOperator() {
    return operator;
  }

  public void setOperator(ListExpressionOperator operator) {
    this.operator = operator;
  }

  public List<Expression> getExpressions() {
    return expressions;
  }

  public void addExpression(Expression expression) {
    expressions.add(expression);
  }
  
  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.LIST_EXPRESSION;
  }

  @Override
  public List<Expression> splitByComma() {
    if (operator.getOperator()!=Operator.COMMA)
      return super.splitByComma();
    
    return new ArrayList<Expression>(getExpressions());
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    List<ASTCssNode> result = ArraysUtils.asNonNullList((ASTCssNode)operator);
    result.addAll(expressions);
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder("[");
    Iterator<Expression> iterator = expressions.iterator();
    if (iterator.hasNext()) 
      builder.append(iterator.next());
    
    while (iterator.hasNext()) {
      builder.append(operator.getOperator()).append(iterator.next());
    }
    builder.append("]");
    return  builder.toString();
  }

  @Override
  public ListExpression clone() {
    ListExpression clone = (ListExpression) super.clone();
    clone.expressions = ArraysUtils.deeplyClonedList(expressions);
    clone.operator = operator.clone();
    clone.configureParentToAllChilds();
    return clone;
  }

}
