package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class CharsetDeclaration extends ASTCssNode {

  private String charset;

  public CharsetDeclaration(HiddenTokenAwareTree token, String charset) {
    super(token);
    this.charset = charset;
  }

  public String getCharset() {
    return charset;
  }

  public void setCharset(String charset) {
    this.charset = charset;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.CHARSET_DECLARATION;
  }

  @Override
  public CharsetDeclaration clone() {
    return (CharsetDeclaration) super.clone();
  }

}
