package org.porting.less4j.core.ast;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

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
  public ASTCssNodeType getType() {
    return ASTCssNodeType.CHARSET_DECLARATION;
  }

}
