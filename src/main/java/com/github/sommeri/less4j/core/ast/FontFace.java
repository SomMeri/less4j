package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class FontFace extends Directive {

  private GeneralBody body;

  public FontFace(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public GeneralBody getBody() {
    return body;
  }

  public void setBody(GeneralBody body) {
    this.body = body;
  }

  @Override
  @NotAstProperty
  public List<ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList((ASTCssNode)body);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.FONT_FACE;
  }

  @Override
  public FontFace clone() {
    FontFace result = (FontFace) super.clone();
    result.body = body==null?null:body.clone();
    result.configureParentToAllChilds();
    return result;
  }
}
