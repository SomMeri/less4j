package org.porting.less4j.core.ast;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

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

}
