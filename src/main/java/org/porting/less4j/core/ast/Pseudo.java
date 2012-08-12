package org.porting.less4j.core.ast;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

//FIXME: this does not handle parametrized classes yet
//FIXME: this does not handle :: correctly
//FIXME: not done yet
public abstract class Pseudo extends ASTCssNode {

  private String name;

  public Pseudo(HiddenTokenAwareTree token, String name) {
    super(token);
    this.name = name;
  }
  public String getName() {
    return name;
  }

}
