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
  public List<ASTCssNode> getChilds() {
    return children;
  }
  
  //FIXME: this will probably disappear, but I need it to test unfinished structures
  public String getUnderlyingText(){
    return underlyingStructure.getText();
  }

  @SuppressWarnings("unchecked")
  protected List<CommonTree> extractUnderlyingMembers() {
    List<CommonTree> result = (List<CommonTree>) underlyingStructure.getChildren();
    if (result==null)
      result = new ArrayList<CommonTree>();
    
    return result;
  }

  public void addChild(ASTCssNode kid) {
    children.add(kid);
  }
  
  public abstract ASTCssNodeType getType();
}
