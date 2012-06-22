package org.porting.less4j.core.ast;

import org.antlr.runtime.tree.CommonTree;

public class SelectorAttribute extends ASTCssNode {

  private String name;
  private Operator operator;
  private String value;

  public SelectorAttribute(CommonTree token, String name) {
    this(token, name, Operator.NONE, null);
  }
  
  public SelectorAttribute(CommonTree token, String name, Operator operator, String value) {
    super(token);
    this.name = name;
    this.operator = operator;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public Operator getOperator() {
    return operator;
  }

  public void setOperator(Operator operator) {
    this.operator = operator;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.SELECTOR_ATTRIBUTE;
  }

  public enum Operator {
    NONE, EQUALS, INCLUDES, SPECIAL_PREFIX, PREFIXMATCH, SUFFIXMATCH, SUBSTRINGMATCH
  }
}
