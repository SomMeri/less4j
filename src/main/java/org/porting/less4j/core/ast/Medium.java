package org.porting.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

//FIXME this will not fry with CSS3 medium definition. It is compliant only with CSS2. 
public class Medium extends ASTCssNode {

  private List<String> mediums;
  
  public Medium(HiddenTokenAwareTree token) {
    this(token, new ArrayList<String>());
  }

  public Medium(HiddenTokenAwareTree token, List<String> mediums) {
    super(token);
    this.mediums =mediums;
  }

  public List<String> getMediums() {
    return mediums;
  }

  public void setMediums(List<String> mediums) {
    this.mediums = mediums;
  }

  public void addMedium(String medium) {
    mediums.add(medium);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.MEDIUM;
  }

}
