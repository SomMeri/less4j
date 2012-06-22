package org.porting.less4j.core.ast;

import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.porting.less4j.core.parser.LessLexer;

public class SimpleSelector extends ASTCssNode {
  
  private String elementName;
  private boolean isStar;
  private List<ASTCssNode> subsequent;

  public SimpleSelector(CommonTree token, String elementName, boolean isStar, List<ASTCssNode> subsequent) {
    super(token);
    assert token!=null && token.getType()==LessLexer.SIMPLE_SELECTOR;
    this.elementName = elementName;
    this.isStar = isStar;
    this.subsequent =  subsequent;
  }
  
  public String getElementName() {
    return elementName;
  }

  public boolean isStar() {
    return isStar;
  }

  public boolean hasElement() {
    return null!=getElementName();
  }

  public List<ASTCssNode> getSubsequent() {
    return subsequent;
  }
  
  public ASTCssNodeType getType() {
    return ASTCssNodeType.SIMPLE_SELECTOR;
  }
}
