package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class Declaration extends ASTCssNode {

  private String name;
  private Expression expression;
  private boolean important;

  public Declaration(HiddenTokenAwareTree token, String name) {
    this(token, name, null, false);
  }

  public Declaration(HiddenTokenAwareTree token, String name, Expression expression) {
    this(token, name, expression, false);
  }

  public Declaration(HiddenTokenAwareTree token, String name, Expression expression, boolean important) {
    super(token);
    this.name = name;
    this.expression = expression;
    this.important = important;
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
  
  public void setName(String name) {
    this.name = name;
  }

  public void setExpression(Expression expression) {
    this.expression = expression;
  }

  public void setImportant(boolean important) {
    this.important = important;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.DECLARATION;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(expression);
  }

  public boolean isFontDeclaration() {
    return getName()!=null? getName().toLowerCase().equals("font") : false;
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
