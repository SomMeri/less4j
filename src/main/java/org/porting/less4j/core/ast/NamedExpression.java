package org.porting.less4j.core.ast;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

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
  public ASTCssNodeType getType() {
    return ASTCssNodeType.NAMED_EXPRESSION;
  }

}
