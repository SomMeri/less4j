package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class SupportsLogicalCondition extends SupportsCondition {
  
  private List<SupportsLogicalOperator> logicalOperators = new ArrayList<SupportsLogicalOperator>();
  private List<SupportsCondition> conditions = new ArrayList<SupportsCondition>();

  public SupportsLogicalCondition(HiddenTokenAwareTree token, SupportsCondition firstCondition) {
    super(token);
    conditions.add(firstCondition);
  }

  public void addCondition(SupportsLogicalOperator logicalOperator, SupportsCondition condition) {
    logicalOperators.add(logicalOperator);
    conditions.add(condition);
  }

  public List<SupportsLogicalOperator> getLogicalOperators() {
    return logicalOperators;
  }

  public List<SupportsCondition> getConditions() {
    return conditions;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    List<ASTCssNode> childs = new ArrayList<ASTCssNode>(logicalOperators);
    childs.addAll(conditions);
    return childs;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.SUPPORTS_CONDITION_LOGICAL;
  }

  @Override
  public SupportsLogicalCondition clone() {
    SupportsLogicalCondition result = (SupportsLogicalCondition) super.clone();
    result.logicalOperators = ArraysUtils.deeplyClonedList(logicalOperators);
    result.conditions = ArraysUtils.deeplyClonedList(conditions);
    result.configureParentToAllChilds();
    return result;
  }

}
