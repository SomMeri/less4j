package org.porting.less4j.core.ast;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public class MediumModifier extends ASTCssNode {
  
  private Modifier modifier = Modifier.NONE;

  public MediumModifier(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public MediumModifier(HiddenTokenAwareTree underlyingStructure, Modifier modifier) {
    this(underlyingStructure);
    this.modifier = modifier;
  }
  
  public Modifier getModifier() {
    return modifier;
  }

  public void setModifier(Modifier modifier) {
    this.modifier = modifier;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.MEDIUM_MODIFIER   ;
  }

  public enum Modifier {
    ONLY, NOT, NONE
  }
}
