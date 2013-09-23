package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class Medium extends ASTCssNode {

  private MediumModifier modifier;
  private MediumType mediumType;

  public Medium(HiddenTokenAwareTree underlyingStructure, MediumModifier modifier, MediumType mediumType) {
    super(underlyingStructure);
    this.modifier = modifier;
    this.mediumType = mediumType;
  }

  public MediumModifier getModifier() {
    return modifier;
  }

  public void setModifier(MediumModifier modifier) {
    this.modifier = modifier;
  }

  public MediumType getMediumType() {
    return mediumType;
  }

  public void setMediumType(MediumType mediumType) {
    this.mediumType = mediumType;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.MEDIUM;
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(modifier, mediumType);
  }
  
  @Override
  public Medium clone() {
    Medium result = (Medium) super.clone();
    result.modifier = modifier==null?null:modifier.clone();
    result.mediumType = mediumType==null?null:mediumType.clone();
    result.configureParentToAllChilds();
    return result;
  }
}
