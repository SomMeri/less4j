package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class CharsetDeclaration extends ASTCssNode {

  private CssString charset;

  public CharsetDeclaration(HiddenTokenAwareTree token, CssString charset) {
    super(token);
    this.charset = charset;
  }

  public CssString getCharset() {
    return charset;
  }

  public void setCharset(CssString charset) {
    this.charset = charset;
  }

  @Override
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    ArrayList<ASTCssNode> result = new ArrayList<ASTCssNode>();
    result.add(charset);
    return result;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.CHARSET_DECLARATION;
  }

  @Override
  public CharsetDeclaration clone() {
    CharsetDeclaration clone = (CharsetDeclaration) super.clone();
    clone.charset = charset == null ? null : charset.clone();
    clone.configureParentToAllChilds();

    return clone;
  }

}
