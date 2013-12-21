package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class Declaration extends ASTCssNode {

  private String name;
  private Expression expression;
  private boolean important;
  private boolean merging;

  public Declaration(HiddenTokenAwareTree token, String name) {
    this(token, name, null, false, false);
  }

  public Declaration(HiddenTokenAwareTree token, String name, Expression expression, boolean merging) {
    this(token, name, expression, false, merging);
  }

  public Declaration(HiddenTokenAwareTree token, String name, Expression expression, boolean important, boolean merging) {
    super(token);
    this.name = name;
    this.expression = expression;
    this.important = important;
    this.merging = merging;
  }

  public String getName() {
    return name;
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
  
  public void setName(String name) {
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
    return ArraysUtils.asNonNullList(expression);
  }

  public boolean isFontDeclaration() {
    return getName()!=null? getName().toLowerCase().equals("font") : false;
  }

  public boolean isFilterDeclaration() {
    return getName()!=null? getName().toLowerCase().endsWith("filter") : false;
  }
  
  @Override
  public String toString() {
    return ""+ name + ":" + expression;
  }
  
  @Override
  public Declaration clone() {
    Declaration result = (Declaration) super.clone();
    result.expression = expression==null?null:expression.clone();
    result.configureParentToAllChilds();
    return result;
  }
}
