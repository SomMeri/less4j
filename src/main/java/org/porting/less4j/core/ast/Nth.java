package org.porting.less4j.core.ast;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public class Nth extends ASTCssNode {
  
  private NumberExpression repeater;
  private NumberExpression mod;
  private Form form;

  public Nth(HiddenTokenAwareTree underlyingStructure, NumberExpression repeater, NumberExpression mod) {
    this(underlyingStructure, repeater, mod, Form.STANDARD);
  }

  public Nth(HiddenTokenAwareTree underlyingStructure, NumberExpression repeater, NumberExpression mod, Form form) {
    super(underlyingStructure);
    this.repeater = repeater;
    this.mod = mod;
    this.form = form;
  }

  public NumberExpression getRepeater() {
    return repeater;
  }

  public NumberExpression getMod() {
    return mod;
  }

  public void setRepeater(NumberExpression repeater) {
    this.repeater = repeater;
  }

  public void setMod(NumberExpression mod) {
    this.mod = mod;
  }
  
  public Form getForm() {
    return form;
  }

  public void setForm(Form form) {
    this.form = form;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.NTH;
  }

  public enum Form {
    EVEN, ODD, STANDARD;
  }
}
