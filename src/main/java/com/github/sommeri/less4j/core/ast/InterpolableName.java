package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class InterpolableName extends ASTCssNode {
  
  private List<InterpolableNamePart> parts = new ArrayList<InterpolableNamePart>(); 
  
  public InterpolableName(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public InterpolableName(HiddenTokenAwareTree underlyingStructure, InterpolableNamePart... fixedNameParts) {
    super(underlyingStructure);
    add(fixedNameParts);
  }

  public boolean isInterpolated() {
    if (parts==null)
      return false;
    
    for (InterpolableNamePart part : parts) {
      if (part.getType() == ASTCssNodeType.VARIABLE_NAME_PART)
        return true;
    }
    
    return false;
  }

  public String getName() {
    String result = "";
    for (InterpolableNamePart part : parts) {
      result+=part.getName();
    }
    return result;
  }

  public void add(InterpolableNamePart part) {
    parts.add(part);
  }

  public void add(InterpolableNamePart... parts) {
    this.parts.addAll(Arrays.asList(parts));
    configureParentToAllChilds();
  }

  public List<InterpolableNamePart> getParts() {
    return parts;
  }

  public void replaceMember(InterpolableNamePart oldMember, InterpolableNamePart newMember) {
    parts.add(parts.indexOf(oldMember), newMember);
    newMember.setParent(this);
    parts.remove(oldMember);
    oldMember.setParent(null);
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return parts;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.INTERPOLABLE_NAME;
  }

  public InterpolableName clone() {
    InterpolableName clone = (InterpolableName) super.clone();
    clone.parts = ArraysUtils.deeplyClonedList(getParts());
    clone.configureParentToAllChilds();
    return clone;
  }

  public void extendName(String extension) {
    //it would be cleaner to extend with a list of name parts (maybe)
    parts.add(new FixedNamePart(getUnderlyingStructure(), extension));
    
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (InterpolableNamePart part : getParts()) {
      builder.append(part);
    }
    return builder.toString();
  }

  
}
