package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class GuardBinary extends Guard {

  //we do not have to care about where exactly are comments inside guards,
  //because guards will disappear anyway after compilation.
  private Guard left;
  private Operator operator;
  private Guard right;

  public GuardBinary(HiddenTokenAwareTree token, Guard left, Operator operator, Guard right) {
    super(token);
    this.left = left;
    this.operator = operator;
    this.right = right;
  }

  public Operator getOperator() {
    return operator;
  }

  public void setOperator(Operator operator) {
    this.operator = operator;
  }
 
  public Guard getLeft() {
    return left;
  }

  public void setLeft(Guard left) {
    this.left = left;
  }

  public Guard getRight() {
    return right;
  }

  public void setRight(Guard right) {
    this.right = right;
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(left, right);
  }

  @Override
  public String toString() {
    return "[" + left + operator + right + "]";
  }

  @Override
  public GuardBinary clone() {
    GuardBinary result = (GuardBinary) super.clone();
    result.left = left==null?null:left.clone();
    result.right = right==null?null:right.clone();
    result.configureParentToAllChilds();
    return result;
  }
  
  @Override
  public Type getGuardType() {
    return Guard.Type.BINARY;
  }

  public enum Operator {
    AND("and"), OR("or");
    
    private final String symbol;

    private Operator(String symbol) {
      this.symbol = symbol;
    }

    public String getSymbol() {
      return symbol;
    }
  }

}
