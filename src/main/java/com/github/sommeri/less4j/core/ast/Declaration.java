package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class Declaration extends ASTCssNode {

  //private String name;
  private InterpolableName name;
  private Expression expression;
  private boolean important;
  private boolean merging;

  public Declaration(HiddenTokenAwareTree token, InterpolableName name) {
    this(token, name, null, false, false);
  }

  public Declaration(HiddenTokenAwareTree token, InterpolableName name, Expression expression, boolean merging) {
    this(token, name, expression, false, merging);
  }

  public Declaration(HiddenTokenAwareTree token, InterpolableName name, Expression expression, boolean important, boolean merging) {
    super(token);
    this.name = name;
    this.expression = expression;
    this.important = important;
    this.merging = merging;
  }

  public String getNameAsString() {
    return name.getName();
  }

  public Expression getExpression() {
    return expression;
  }

  public boolean isImportant() {
    return important;
  }
  
  public boolean isMerging() {
    return merging;
  }
  
  public void setName(InterpolableName name) {
    this.name = name;
  }

  public void setExpression(Expression expression) {
    this.expression = expression;
  }

  public void setImportant(boolean important) {
    this.important = important;
  }

  public void setMerging(boolean merging) {
    this.merging = merging;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.DECLARATION;
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(expression, name);
  }

  public boolean isFontDeclaration() {
    return getNameAsString()!=null? getNameAsString().toLowerCase().equals("font") : false;
  }

  public boolean isFilterDeclaration() {
    return getNameAsString()!=null? getNameAsString().toLowerCase().endsWith("filter") : false;
  }
  
  @Override
  public String toString() {
    return ""+ name + ":" + expression;
  }
  
  @Override
  public Declaration clone() {
    Declaration result = (Declaration) super.clone();
    result.name = name==null?null:name.clone();
    result.expression = expression==null?null:expression.clone();
    result.configureParentToAllChilds();
    return result;
  }
}
