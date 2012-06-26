package org.porting.less4j.core.ast;

import org.antlr.runtime.tree.CommonTree;

public class IdentifierExpression extends Expression {
  
  private String name;

  public IdentifierExpression(CommonTree underlyingStructure) {
    this(underlyingStructure, null);
  }

  public IdentifierExpression(CommonTree underlyingStructure, String name) {
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
