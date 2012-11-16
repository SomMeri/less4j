package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class MixinReference extends ASTCssNode {

  private ElementSubsequent selector;
  private List<Expression> positionalParameters = new ArrayList<Expression>();
  private Map<String, Expression> namedParameters = new HashMap<String, Expression>();
  private boolean important = false;

  public MixinReference(HiddenTokenAwareTree token) {
    super(token);
  }

  public ElementSubsequent getSelector() {
    return selector;
  }

  public String getName() {
    return getSelector().getFullName();
  }

  public void setSelector(ElementSubsequent selector) {
    this.selector = selector;
  }

  public List<Expression> getPositionalParameters() {
    return positionalParameters;
  }

  public int getNumberOfDeclaredParameters() {
    return positionalParameters.size() + namedParameters.size();
  }

  public boolean hasNamedParameter(Variable variable) {
    return namedParameters.containsKey(variable.getName());
  }

  public Expression getNamedParameter(Variable variable) {
    return namedParameters.get(variable.getName());
  }

  public boolean hasPositionalParameter(int parameterNumber) {
    return getPositionalParameters().size() > parameterNumber;
  }
  
  public Expression getPositionalParameter(int parameterNumber) {
    return getPositionalParameters().get(parameterNumber);
  }

  public void addPositionalParameter(Expression parameter) {
    this.positionalParameters.add(parameter);
  }

  public void addNamedParameter(VariableDeclaration parameter) {
    this.namedParameters.put(parameter.getVariable().getName(), parameter.getValue());
  }

  public boolean isImportant() {
    return important;
  }
  
  public void setImportant(boolean important) {
    this.important = important;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    List<ASTCssNode> result = ArraysUtils.asNonNullList((ASTCssNode)selector);
    result.addAll(positionalParameters);
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
    result.positionalParameters = ArraysUtils.deeplyClonedList(positionalParameters);
    result.configureParentToAllChilds();
    return result;
  }

  public List<Expression> getAllPositionalArgumentsFrom(int startIndx) {
    if (hasPositionalParameter(startIndx))
      return positionalParameters.subList(startIndx, positionalParameters.size());
    
    return Collections.emptyList();
  }

}
