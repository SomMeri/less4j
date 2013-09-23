package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

//TODO: this name no longer reflects the reality, it should be abstract reusable structure name at best
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

  public boolean hasMultipleParts() {
    return nameParts.size()>1;
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    //TODO: review needed: this is suspect, shouldnt it return list of name parts?
    return Collections.emptyList();
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.REUSABLE_STRUCTURE_NAME;
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder();
    for (ElementSubsequent namePart : nameParts) {
      result.append(namePart);
    }
    
    return result.toString();
  }

  @Override
  public ReusableStructureName clone() {
    ReusableStructureName result = (ReusableStructureName) super.clone();
    result.nameParts = new ArrayList<ElementSubsequent>(nameParts);
    return result;
  }

}
