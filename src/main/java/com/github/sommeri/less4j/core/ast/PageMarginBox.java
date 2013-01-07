package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class PageMarginBox extends ASTCssNode {

  private Name name;
  private GeneralBody body;

  public PageMarginBox(HiddenTokenAwareTree token) {
    super(token);
  }

  public PageMarginBox(HiddenTokenAwareTree token, Name name) {
    super(token);
    this.name = name;
  }

  public Name getName() {
    return name;
  }

  public boolean hasName() {
    return getName()!=null;
  }

  public void setName(Name name) {
    this.name = name;
  }

  public GeneralBody getBody() {
    return body;
  }

  public void setBody(GeneralBody body) {
    this.body = body;
  }

  @Override
  public List<ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList((ASTCssNode)name, body);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.PAGE_MARGIN_BOX;
  }

  @Override
  public PageMarginBox clone() {
    PageMarginBox result = (PageMarginBox) super.clone();
    result.body = body==null? null : body.clone();
    result.name = name==null? null : name.clone();
    result.configureParentToAllChilds();
    return result;
  }

}
