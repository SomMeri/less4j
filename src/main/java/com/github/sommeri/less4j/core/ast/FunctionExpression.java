package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class FunctionExpression extends Expression {

  private String name;
  private Expression parameter;

  public FunctionExpression(HiddenTokenAwareTree token, String name, Expression parameter) {
    super(token);
    this.name = name;
    this.parameter = parameter;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public Expression getParameter() {
    return parameter;
  }

  public void setParameter(Expression parameter) {
    this.parameter = parameter;
  }

  public boolean isCssOnlyFunction() {
    if (isMsFilter() || hasNamedParameter())
      return true;
    
    return false;
  }

  private boolean hasNamedParameter() {
    if (getParameter()==null)
      return false;
    
    List<Expression> parameters = getParameter().splitByComma();
    for (Expression param : parameters) {
      if (param.getType()==ASTCssNodeType.NAMED_EXPRESSION)
        return true;
    }

    return false;
  }

  private boolean isMsFilter() {
    return getName().contains(":") || getName().contains(".");
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.FUNCTION;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(parameter);
  }

  @Override
  public FunctionExpression clone() {
    FunctionExpression result = (FunctionExpression) super.clone();
    result.parameter = parameter == null ? null : parameter.clone();
    result.configureParentToAllChilds();
    return result;
  }

}
