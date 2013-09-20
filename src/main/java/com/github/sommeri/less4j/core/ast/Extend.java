package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class Extend extends ASTCssNode {

  private boolean all = false;
  private Selector target;

  public Extend(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public Extend(HiddenTokenAwareTree underlyingStructure, Selector selector) {
    this(underlyingStructure, selector, false);
  }

  public Extend(HiddenTokenAwareTree underlyingStructure, Selector target, boolean all) {
    super(underlyingStructure);
    this.target = target;
    this.all = all;
  }

  public boolean isAll() {
    return all;
  }

  public void setAll(boolean all) {
    this.all = all;
  }

  public Selector getTarget() {
    return target;
  }

  public void setTarget(Selector target) {
    this.target = target;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(target);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.EXTEND;
  }
  
  @Override
  public Extend clone() {
    Extend result = (Extend) super.clone();
    result.target = target==null?null:target.clone();
    result.configureParentToAllChilds();
    return result;
  }
  
}
