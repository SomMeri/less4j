package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class MixinReference extends ASTCssNode {

  private CssClass selector;
  private List<Expression> parameters = new ArrayList<Expression>();

  public MixinReference(HiddenTokenAwareTree token) {
    super(token);
  }

  public CssClass getSelector() {
    return selector;
  }

  public String getName() {
    return getSelector().getName();
  }

  public void setSelector(CssClass selector) {
    this.selector = selector;
  }

  public List<Expression> getParameters() {
    return parameters;
  }

  public boolean hasParameter(int parameterNumber) {
    return getParameters().size() > parameterNumber;
  }
  
  public Expression getParameter(int parameterNumber) {
    return getParameters().get(parameterNumber);
  }

  public void addParameter(Expression parameter) {
    this.parameters.add(parameter);
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    List<ASTCssNode> result = ArraysUtils.asNonNullList((ASTCssNode)selector);
    result.addAll(parameters);
    return result;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.MIXIN_REFERENCE;
  }

  @Override
  public String toString() {
    return "MixinReference[" +getName()+ "]";
  }
  
  @Override
  public MixinReference clone() {
    MixinReference result = (MixinReference) super.clone();
    result.selector = selector==null?null:selector.clone();
    result.parameters = ArraysUtils.deeplyClonedList(parameters);
    result.configureParentToAllChilds();
    return result;
  }

}
