package org.porting.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public class StyleSheet extends ASTCssNode {
  
  private List<ASTCssNode> children = new ArrayList<ASTCssNode>();

  public StyleSheet(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public void addMember(ASTCssNode child) {
    children.add(child);
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return children;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.STYLE_SHEET;
  }

}
