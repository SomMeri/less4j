package org.porting.less4j.core.ast;

import java.util.List;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;
import org.porting.less4j.utils.ArraysUtils;

public class SignedExpression extends Expression {

  private Expression expression;
  private Sign sign;

  public SignedExpression(HiddenTokenAwareTree token, Sign sign, Expression expression) {
    super(token);
    this.expression = expression;
    this.sign = sign;
  }

  @Override
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
    return ASTCssNodeType.NEGATED_EXPRESSION;
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

}
