package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class SignedExpression extends Expression {

  private Expression expression;
  private Sign sign;

  public SignedExpression(HiddenTokenAwareTree token, Sign sign, Expression expression) {
    super(token);
    this.expression = expression;
    this.sign = sign;
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(expression);
  }

  public Expression getExpression() {
    return expression;
  }

  public void setExpression(Expression expression) {
    this.expression = expression;
  }

  public Sign getSign() {
    return sign;
  }

  public void setSign(Sign sign) {
    this.sign = sign;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.SIGNED_EXPRESSION;
  }

  @Override
  public String toString() {
    return "-" +expression;
  }
  
  public enum Sign {
    PLUS("+"), MINUS("-");

    private final String symbol;
    
    private Sign(String symbol) {
      this.symbol = symbol;
    }
    
    public String toSymbol() {
      return symbol;
    }
  }
  
  @Override
  public SignedExpression clone() {
    SignedExpression result = (SignedExpression) super.clone();
    result.expression = expression==null?null:expression.clone();
    result.configureParentToAllChilds();
    return result;
  }

}
