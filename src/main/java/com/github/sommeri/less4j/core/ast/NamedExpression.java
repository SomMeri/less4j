package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class NamedExpression extends IdentifierExpression {

  private Expression expression;
  private String name;

  public NamedExpression(HiddenTokenAwareTree underlyingStructure, String name, Expression expression) {
    super(underlyingStructure);
    this.name = name;
    this.expression = expression;
  }

  public NamedExpression(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }
  
  public NamedExpression(HiddenTokenAwareTree underlyingStructure, String name, Expression evaluate, IScope scope) {
    this(underlyingStructure, name, evaluate);
    setScope(scope);
  }

  public Expression getExpression() {
    return expression;
  }

  public void setExpression(Expression expression) {
    this.expression = expression;
  }
  
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  @NotAstProperty
  public List<ASTCssNode> getChilds() {
    List<ASTCssNode> result = ArraysUtils.asNonNullList((ASTCssNode)expression);
    result.addAll(super.getChilds());
    return result;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.NAMED_EXPRESSION;
  }

  @Override
  public NamedExpression clone() {
    NamedExpression result = (NamedExpression) super.clone();
    result.expression = expression==null?null:expression.clone();
    result.configureParentToAllChilds();
    return result;
  }
}
