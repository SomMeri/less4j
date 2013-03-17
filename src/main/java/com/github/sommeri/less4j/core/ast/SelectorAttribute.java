package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class SelectorAttribute extends ElementSubsequent {

  private String name;
  private SelectorOperator operator;
  private Expression value;

  public SelectorAttribute(HiddenTokenAwareTree token, String name) {
    this(token, name, new SelectorOperator(new HiddenTokenAwareTree(token.getSource()), SelectorOperator.Operator.NONE), null);
  }
  
  public SelectorAttribute(HiddenTokenAwareTree token, String name, SelectorOperator operator, Expression value) {
    super(token);
    this.name = name;
    this.operator = operator;
    this.value = value;
  }

  public String getName() {
    return name;
  }

  public String getFullName() {
    return getName();
  }
  
  @Override
  public void extendName(String extension) {
    setName(getName() + extension);
  }

  public SelectorOperator getOperator() {
    return operator;
  }

  public void setOperator(SelectorOperator operator) {
    this.operator = operator;
  }

  public Expression getValue() {
    return value;
  }

  public void setValue(Expression value) {
    this.value = value;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.SELECTOR_ATTRIBUTE;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(operator, value);
  }
  
  @Override
  public SelectorAttribute clone() {
    SelectorAttribute result = (SelectorAttribute) super.clone();
    result.operator = operator==null?null:operator.clone();
    result.value = value==null?null:value.clone();
    result.configureParentToAllChilds();
    return result;
  }

  @Override
  public boolean isInterpolated() {
    return false;
  }

}
