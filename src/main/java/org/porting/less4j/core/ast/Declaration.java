package org.porting.less4j.core.ast;

import java.util.List;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;
import org.porting.less4j.utils.ArraysUtils;

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
}
