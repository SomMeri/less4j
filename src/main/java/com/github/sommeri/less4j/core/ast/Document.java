package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class Document extends ASTCssNode implements BodyOwner<GeneralBody> {

  private String dialect;
  private List<FunctionExpression> urlMatchFunction = new ArrayList<FunctionExpression>();
  private GeneralBody body;

  public Document(HiddenTokenAwareTree token, String dialect) {
    super(token);
    this.dialect = dialect;
  }

  public GeneralBody getBody() {
    return body;
  }

  public void setBody(GeneralBody body) {
    this.body = body;
  }

  public String getDialect() {
    return dialect;
  }

  public void setDialect(String dialect) {
    this.dialect = dialect;
  }

  public List<FunctionExpression> getUrlMatchFunctions() {
    return urlMatchFunction;
  }

  @Override
  public List<ASTCssNode> getChilds() {
    List<ASTCssNode> childs = new ArrayList<ASTCssNode>(urlMatchFunction);
    childs.add(body);
    return childs;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.DOCUMENT;
  }

  @Override
  public Document clone() {
    Document result = (Document) super.clone();
    result.urlMatchFunction = ArraysUtils.deeplyClonedList(urlMatchFunction);
    result.body = body==null? null : body.clone();
    result.configureParentToAllChilds();
    return result;
  }

  public void addUrlMatchFunctions(List<FunctionExpression> urlMatchFunctions) {
    this.urlMatchFunction.addAll(urlMatchFunctions);
  }
}
