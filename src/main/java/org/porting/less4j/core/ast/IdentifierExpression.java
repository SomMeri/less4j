package org.porting.less4j.core.ast;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public class IdentifierExpression extends Expression {
  
  private String name;

  public IdentifierExpression(HiddenTokenAwareTree underlyingStructure) {
    this(underlyingStructure, null);
  }

  public IdentifierExpression(HiddenTokenAwareTree underlyingStructure, String name) {
    super(underlyingStructure);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.IDENTIFIER_EXPRESSION;
  }

}
