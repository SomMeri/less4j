package org.porting.less4j.core.ast;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public class Medium extends ASTCssNode {

  private MediumModifier modifier = MediumModifier.NONE;
  private String mediumType;

  public Medium(HiddenTokenAwareTree underlyingStructure, MediumModifier modifier, String mediumType) {
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

  public String getMediumType() {
    return mediumType;
  }

  public void setMediumType(String mediumType) {
    this.mediumType = mediumType;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.MEDIUM;
  }

  public enum MediumModifier {
    ONLY, NOT, NONE
  }
}
