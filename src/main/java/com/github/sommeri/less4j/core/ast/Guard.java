package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class Guard extends ASTCssNode {

  //since guards allow only the "and" operator, we to not need special oerator field
  private List<GuardCondition> conditions = new ArrayList<GuardCondition>();

  public Guard(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public Guard(HiddenTokenAwareTree token, List<GuardCondition> conditions) {
    this(token);
    this.conditions = conditions;
  }

  public List<GuardCondition> getConditions() {
    return conditions;
  }

  public void addCondition(GuardCondition condition) {
    this.conditions.add(condition);
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return conditions;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.GUARD;
  }

  @Override
  public Guard clone() {
    Guard result = (Guard) super.clone();
    result.conditions = ArraysUtils.deeplyClonedList(conditions);
    result.configureParentToAllChilds();
    return result;
  }

}
