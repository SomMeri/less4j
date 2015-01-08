package com.github.sommeri.less4j.core.ast;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class KeywordExpression extends IdentifierExpression {
  
  private boolean isImportant;
  
  public KeywordExpression(HiddenTokenAwareTree underlyingStructure) {
    this(underlyingStructure, null, false);
  }

  public KeywordExpression(HiddenTokenAwareTree underlyingStructure, String value, boolean isImportant) {
    super(underlyingStructure, value);
    this.isImportant = isImportant;
  }

  public boolean isImportant() {
    return isImportant;
  }

  public void setImportant(boolean isImportant) {
    this.isImportant = isImportant;
  }

  @Override
  public KeywordExpression clone() {
    return (KeywordExpression) super.clone();
  }

}
