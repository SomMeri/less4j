package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class FaultyExpression extends Expression {
  
  public FaultyExpression(HiddenTokenAwareTree token) {
    super(token);
  }

  public FaultyExpression(ASTCssNode cause) {
    super(cause.getUnderlyingStructure());
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.FAULTY_EXPRESSION;
  }

  @Override
  public FaultyExpression clone() {
    FaultyExpression clone = (FaultyExpression) super.clone();
    return clone;
  }
}
