package org.porting.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;

public abstract class ASTCssNode {
  
  private final CommonTree underlyingStructure;
  private List<ASTCssNode> children = new ArrayList<ASTCssNode>();

  public ASTCssNode(CommonTree underlyingStructure) {
    this.underlyingStructure = underlyingStructure;
  }
  
  // FIXME: turn to abstract
  public List<? extends ASTCssNode> getChilds() {
    return children;
  }
  
  @Deprecated
  public String getUnderlyingText(){
    return underlyingStructure.getText();
  }

  public void addChild(ASTCssNode kid) {
    children.add(kid);
  }
  
  public abstract ASTCssNodeType getType();
}
