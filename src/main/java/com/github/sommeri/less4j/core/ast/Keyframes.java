package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

//FIXME: !!!! now, what if some mixin add declaration or something similar into this??? FIX
public class Keyframes extends Body<ASTCssNode> {

  private String dialect;
  private List<KeyframesName> names = new ArrayList<KeyframesName>();

  public Keyframes(HiddenTokenAwareTree token, String dialect) {
    super(token);
    this.dialect = dialect;
  }

  public List<ASTCssNode> getBody() {
    return super.getBody();
  }
  
  public String getDialect() {
    return dialect;
  }

  public void setDialect(String dialect) {
    this.dialect = dialect;
  }

  public List<KeyframesName> getNames() {
    return names;
  }

  @Override
  public List<ASTCssNode> getChilds() {
    List<ASTCssNode> childs = new ArrayList<ASTCssNode>(super.getChilds());
    childs.addAll(names);
    return childs;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.KEYFRAMES;
  }

  @Override
  public Keyframes clone() {
    Keyframes result = (Keyframes) super.clone();
    result.names = ArraysUtils.deeplyClonedList(names);
    result.configureParentToAllChilds();
    return result;
  }

  public void addNames(List<KeyframesName> names) {
    this.names.addAll(names);
  }
}
