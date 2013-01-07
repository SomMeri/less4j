package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

//FIXME: !validation! warning on viewport body with nested rulesets in output
public class GeneralBody extends Body<ASTCssNode> {

  public GeneralBody(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public GeneralBody(HiddenTokenAwareTree underlyingStructure, List<ASTCssNode> members) {
    super(underlyingStructure, members);
  }

  public GeneralBody clone() {
    return (GeneralBody) super.clone();
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.GENERAL_BODY;
  }

}
