package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class MultiTargetExtend extends ASTCssNode {

  private List<Extend> allExtends = new ArrayList<Extend>();

  public MultiTargetExtend(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public MultiTargetExtend(HiddenTokenAwareTree underlyingStructure, List<Extend> allExtends) {
    this(underlyingStructure);
    this.allExtends = allExtends;
  }

  public List<Extend> getAllExtends() {
    return allExtends;
  }

  public void setAllExtends(List<Extend> allExtends) {
    this.allExtends = allExtends;
  }

  public void addExtend(Extend extend) {
    this.allExtends.add(extend);
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    return new ArrayList<ASTCssNode>(allExtends);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.MULTI_TARGET_EXTEND;
  }

  @Override
  public MultiTargetExtend clone() {
    MultiTargetExtend result = (MultiTargetExtend) super.clone();
    result.allExtends = allExtends == null ? null : ArraysUtils.deeplyClonedList(allExtends);
    result.configureParentToAllChilds();
    return result;
  }

}
