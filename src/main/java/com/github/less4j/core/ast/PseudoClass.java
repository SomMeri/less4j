package com.github.less4j.core.ast;

import java.util.List;

import com.github.less4j.core.parser.HiddenTokenAwareTree;
import com.github.less4j.utils.ArraysUtils;

public class PseudoClass extends Pseudo {
  
  private ASTCssNode parameter;

  public PseudoClass(HiddenTokenAwareTree token, String name, ASTCssNode parameter) {
    super(token, name);
    this.parameter = parameter;
  }

  public PseudoClass(HiddenTokenAwareTree token, String name) {
    super(token, name);
  }

  public boolean hasParameters() {
    return getParameter()!=null;
  }

  public ASTCssNode getParameter() {
    return parameter;
  }

  public void setParameter(ASTCssNode parameter) {
    this.parameter = parameter;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.PSEUDO_CLASS;
  }
  
  @Override
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(parameter);
  }

}
