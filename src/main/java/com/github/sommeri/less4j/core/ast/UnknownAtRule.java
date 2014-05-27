package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class UnknownAtRule extends ASTCssNode implements BodyOwner<GeneralBody> {

  private String name;
  private List<Expression> names = new ArrayList<Expression>();
  private GeneralBody body;
  private SyntaxOnlyElement semicolon;

  public UnknownAtRule(HiddenTokenAwareTree token, String name) {
    super(token);
    this.name = name;
  }

  public GeneralBody getBody() {
    return body;
  }

  public void setBody(GeneralBody body) {
    this.body = body;
  }

  public SyntaxOnlyElement getSemicolon() {
    return semicolon;
  }

  public void setSemicolon(SyntaxOnlyElement semicolon) {
    this.semicolon = semicolon;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Expression> getNames() {
    return names;
  }

  @Override
  @NotAstProperty
  public List<ASTCssNode> getChilds() {
    List<ASTCssNode> childs = new ArrayList<ASTCssNode>(names);
    ArraysUtils.addIfNonNull(childs, body, semicolon);
    return childs;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.UNKNOWN_AT_RULE;
  }

  @Override
  public UnknownAtRule clone() {
    UnknownAtRule result = (UnknownAtRule) super.clone();
    result.names = ArraysUtils.deeplyClonedList(names);
    result.body = body==null? null : body.clone();
    result.semicolon = semicolon==null? null : semicolon.clone(); 
    result.configureParentToAllChilds();
    return result;
  }

  public void addNames(List<Expression> names) {
    this.names.addAll(names);
  }
}
