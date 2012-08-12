package org.porting.less4j.core.ast;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

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

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.PSEUDO_CLASS;
  }
  
}
