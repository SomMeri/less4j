package org.porting.less4j.core.ast;

import java.util.List;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;
import org.porting.less4j.utils.ArraysUtils;

public class Medium extends ASTCssNode {

  private MediumModifier modifier = new MediumModifier(null);
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
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(modifier, mediumType);
  }
}
