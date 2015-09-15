package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class MixinReference extends ASTCssNode {

  private List<ReusableStructureName> nameChain = new ArrayList<ReusableStructureName>();
  private ReusableStructureName finalName;
  private List<Expression> positionalParameters = new ArrayList<Expression>();
  private Map<String, Expression> namedParameters = new HashMap<String, Expression>();
  private boolean important = false;

  public MixinReference(HiddenTokenAwareTree token) {
    super(token);
  }

  public ReusableStructureName getFinalName() {
    return finalName;
  }

  public String getFinalNameAsString() {
    return getFinalName().asString();
  }

  public void setFinalName(ReusableStructureName name) {
    this.finalName = name;
  }

  public List<ReusableStructureName> getNameChain() {
    return nameChain;
  }

  public boolean hasInterpolatedNameChain() {
    for (ReusableStructureName name : nameChain) {
      if (name.isInterpolated())
        return true;
    }
    return false;
  }

  public List<String> getNameChainAsStrings() {
    List<String> result = new ArrayList<String>();
    for (ReusableStructureName name : nameChain) {
      result.add(name.asString());
    }
    return result;
  }

  public void setNameChain(List<ReusableStructureName> nameChain) {
    this.nameChain = nameChain;
  }

  public List<Expression> getPositionalParameters() {
    return positionalParameters;
  }

  public boolean hasNamedParameter(Variable variable) {
    return namedParameters.containsKey(variable.getName());
  }

  public Expression getNamedParameter(Variable variable) {
    return namedParameters.get(variable.getName());
  }

  public void addName(ReusableStructureName text) {
    nameChain.add(text);
  }

  public void addNames(List<ReusableStructureName> nameChain) {
    this.nameChain.addAll(nameChain);
  }

  public void addPositionalParameter(Expression parameter) {
    this.positionalParameters.add(parameter);
  }

  public void addNamedParameter(VariableDeclaration parameter) {
    //FIXME: this is wrong way of doing it, if the value changes later on, the map is NOT updated 
    this.namedParameters.put(parameter.getVariable().getName(), parameter.getValue());
  }

  public boolean isImportant() {
    return important;
  }
  
  public void setImportant(boolean important) {
    this.important = important;
  }

  public boolean hasInterpolatedFinalName() {
    return finalName.isInterpolated();
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    List<ASTCssNode> result = ArraysUtils.asNonNullList((ASTCssNode)finalName);
    result.addAll(nameChain);
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
    StringBuilder builder = new StringBuilder("MixinReference[");
    for (ReusableStructureName str : nameChain) {
      builder.append(str.asString()).append(" > ");
    }
    builder.append(getFinalName()).append("]");
    return builder.toString();
  }
  
  @Override
  public MixinReference clone() {
    MixinReference result = (MixinReference) super.clone();
    result.nameChain = nameChain == null ? null : new ArrayList<ReusableStructureName>(nameChain);
    result.finalName = finalName==null?null:finalName.clone();
    result.positionalParameters = ArraysUtils.deeplyClonedList(positionalParameters);
    result.namedParameters = ArraysUtils.deeplyClonedMap(namedParameters);
    result.configureParentToAllChilds();
    return result;
  }
  
  @NotAstProperty
  public Map<String, Expression> getNamedParameters() {
    return namedParameters;
  }

}
