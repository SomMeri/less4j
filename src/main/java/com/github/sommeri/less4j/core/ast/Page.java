package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class Page extends ASTCssNode implements BodyOwner<GeneralBody>{

  private Name name;
  private boolean dockedPseudopage = true;
  private Name pseudopage;
  private GeneralBody body;

  public Page(HiddenTokenAwareTree token) {
    super(token);
  }

  public Page(HiddenTokenAwareTree token, Name name, Name pseudopage) {
    super(token);
    this.name = name;
    this.pseudopage = pseudopage;
  }

  public boolean hasDockedPseudopage() {
    return dockedPseudopage;
  }

  public void setDockedPseudopage(boolean dockedPseudopage) {
    this.dockedPseudopage = dockedPseudopage;
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

  public Name getPseudopage() {
    return pseudopage;
  }

  public boolean hasPseudopage() {
    return getPseudopage()!=null;
  }

  public void setPseudopage(Name pseudopage) {
    this.pseudopage = pseudopage;
  }

  public GeneralBody getBody() {
    return body;
  }

  public void setBody(GeneralBody body) {
    this.body = body;
  }

  @Override
  public List<ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList((ASTCssNode)name, pseudopage, body);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.PAGE;
  }

  @Override
  public Page clone() {
    Page result = (Page) super.clone();
    result.body = body==null? null : body.clone();
    result.name = name==null? null : name.clone();
    result.pseudopage = pseudopage==null? null : pseudopage.clone();
    result.configureParentToAllChilds();
    return result;
  }

}
