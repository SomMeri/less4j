package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class Variable extends Expression {
  
  private String name;
  private boolean hasInterpolatedForm;

  public Variable(HiddenTokenAwareTree underlyingStructure, String name) {
    this(underlyingStructure, name, false);
  }

  public Variable(HiddenTokenAwareTree underlyingStructure, String name, boolean hasInterpolatedForm) {
    super(underlyingStructure);
    this.name = name;
    this.hasInterpolatedForm = hasInterpolatedForm;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public boolean hasInterpolatedForm() {
    return hasInterpolatedForm;
  }

  public void setInterpolatedForm(boolean has) {
    this.hasInterpolatedForm = has;
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.VARIABLE;
  }

  @Override
  public String toString() {
    return getName()==null? "Variable" : getName();
  }

  @Override
  public Variable clone() {
    return (Variable) super.clone();
  }
}
