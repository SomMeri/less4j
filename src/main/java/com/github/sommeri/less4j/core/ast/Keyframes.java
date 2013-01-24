package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class Keyframes extends ASTCssNode implements BodyOwner<KeyframesBody> {

  private String dialect;
  private List<KeyframesName> names = new ArrayList<KeyframesName>();
  private KeyframesBody body;

  public Keyframes(HiddenTokenAwareTree token, String dialect) {
    super(token);
    this.dialect = dialect;
  }

  public KeyframesBody getBody() {
    return body;
  }

  public void setBody(KeyframesBody body) {
    this.body = body;
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
    List<ASTCssNode> childs = new ArrayList<ASTCssNode>(names);
    childs.add(body);
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
    result.body = body==null? null : body.clone();
    result.configureParentToAllChilds();
    return result;
  }

  public void addNames(List<KeyframesName> names) {
    this.names.addAll(names);
  }
}
