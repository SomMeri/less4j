package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class InterpolableName extends ASTCssNode {
  
  private List<InterpolableNamePart> parts = new ArrayList<InterpolableNamePart>(); 
  
  public InterpolableName(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public boolean isSimple() {
    if (parts==null || parts.size()!=1)
      return false;
    
    return parts.get(0).getType() == ASTCssNodeType.FIXED_NAME_PART;
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
    //FIXME: this will probably change and extension will be list of name parts
    parts.add(new FixedNamePart(getUnderlyingStructure(), extension));
    
  }

}
