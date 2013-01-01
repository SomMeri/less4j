package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class MixinReference extends ASTCssNode {

  private ReusableStructureName name;
  private List<Expression> positionalParameters = new ArrayList<Expression>();
  private Map<String, Expression> namedParameters = new HashMap<String, Expression>();
  private boolean important = false;

  public MixinReference(HiddenTokenAwareTree token) {
    super(token);
  }

  public ReusableStructureName getName() {
    return name;
  }

  public String getNameAsString() {
    return getName().asString();
  }

  public void setName(ReusableStructureName name) {
    this.name = name;
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

  public boolean hasInterpolatedName() {
    return name.isInterpolated();
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    List<ASTCssNode> result = ArraysUtils.asNonNullList((ASTCssNode)name);
    result.addAll(positionalParameters);
    result.addAll(namedParameters.values());
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
    result.name = name==null?null:name.clone();
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
