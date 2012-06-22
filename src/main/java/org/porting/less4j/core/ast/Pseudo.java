package org.porting.less4j.core.ast;

import org.antlr.runtime.tree.CommonTree;

//FIXME: this does not handle parametrized classes yet
//FIXME: this does not handle :: correctly
public class Pseudo extends ASTCssNode {

  private String name;
  private String parameter;

  public Pseudo(CommonTree token, String name) {
    this(token, name, null);
  }
  
  public Pseudo(CommonTree token, String name, String parameter) {
    //TODO asserts
    super(token);
    this.name = name;
    this.parameter = parameter;
  }

  public String getName() {
    return name;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.PSEUDO;
  }

  public boolean hasParameters() {
    return getParameter()!=null;
  }

  public String getParameter() {
    return parameter;
  }

}
