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

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.FUNCTION;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(parameter);
  }
}
