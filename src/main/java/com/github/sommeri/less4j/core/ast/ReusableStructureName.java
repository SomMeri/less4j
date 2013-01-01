package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class ReusableStructureName extends ASTCssNode {

  private List<ElementSubsequent> nameParts = new ArrayList<ElementSubsequent>();
    
  public ReusableStructureName(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public ReusableStructureName(HiddenTokenAwareTree underlyingStructure, List<ElementSubsequent> nameParts) {
    this(underlyingStructure);
    this.nameParts=nameParts;
  }

  public boolean isInterpolated() {
    for (ElementSubsequent namePart : nameParts) {
      if (namePart.isInterpolated())
        return true;
    }
    
    return false;
  }
  
  public String asString() {
    String result = "";
    
    for (ElementSubsequent namePart : nameParts) {
      result+=namePart.getFullName();
    }
    
    return result;
  }

  public List<ElementSubsequent> getNameParts() {
    return nameParts;
  }

  public void addNamePart(ElementSubsequent namePart) {
    nameParts.add(namePart);
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.REUSABLE_STRUCTURE_NAME;
  }

  @Override
  public ReusableStructureName clone() {
    ReusableStructureName result = (ReusableStructureName) super.clone();
    result.nameParts = new ArrayList<ElementSubsequent>(nameParts);
    return result;
  }

}
