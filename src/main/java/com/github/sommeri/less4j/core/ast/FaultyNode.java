package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class FaultyNode extends ASTCssNode {
  
  public FaultyNode(HiddenTokenAwareTree token) {
    super(token);
  }

  public FaultyNode(ASTCssNode cause) {
    super(cause.getUnderlyingStructure());
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.FAULTY_NODE;
  }

  public boolean isFaulty() {
    return true;
  }

  @Override
  public FaultyNode clone() {
    FaultyNode clone = (FaultyNode) super.clone();
    return clone;
  }
}
