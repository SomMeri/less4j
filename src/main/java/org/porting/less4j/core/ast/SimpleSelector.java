package org.porting.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public class SimpleSelector extends ASTCssNode {
  
  private String elementName;
  private boolean isStar;
  //*.warning and .warning are equivalent http://www.w3.org/TR/css3-selectors/#universal-selector
  //relevant only if the selector is a star
  private boolean isEmptyForm = false;
  private List<ASTCssNode> subsequent = new ArrayList<ASTCssNode>();

  public SimpleSelector(HiddenTokenAwareTree token, String elementName, boolean isStar) {
    super(token);
    assert token!=null;
    this.elementName = elementName;
    this.isStar = isStar;
  }
  
  public String getElementName() {
    return elementName;
  }

  public boolean isStar() {
    return isStar;
  }
  
  public boolean isEmptyForm() {
    return isEmptyForm;
  }

  public void setEmptyForm(boolean isEmptyForm) {
    this.isEmptyForm = isEmptyForm;
  }

  public void setElementName(String elementName) {
    this.elementName = elementName;
  }

  public void setStar(boolean isStar) {
    this.isStar = isStar;
  }

  public boolean hasElement() {
    return null!=getElementName();
  }

  public List<ASTCssNode> getSubsequent() {
    return subsequent;
  }

  public void addSubsequent(ASTCssNode subsequent) {
    this.subsequent.add(subsequent);
  }
  
  public ASTCssNodeType getType() {
    return ASTCssNodeType.SIMPLE_SELECTOR;
  }
}
