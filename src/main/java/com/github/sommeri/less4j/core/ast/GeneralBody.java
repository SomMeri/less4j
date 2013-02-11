package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class GeneralBody extends Body {

  public GeneralBody(HiddenTokenAwareTree underlyingStructure) {
    this(underlyingStructure, new ArrayList<ASTCssNode>());
  }

  public GeneralBody(HiddenTokenAwareTree underlyingStructure, List<ASTCssNode> members) {
    this(underlyingStructure, SyntaxOnlyElement.lbrace(underlyingStructure), SyntaxOnlyElement.rbrace(underlyingStructure), members);
  }

  public GeneralBody(HiddenTokenAwareTree underlyingStructure, SyntaxOnlyElement lbrace, SyntaxOnlyElement rbrace, List<ASTCssNode> members) {
    super(underlyingStructure, lbrace, rbrace, members);
  }

  public GeneralBody clone() {
    return (GeneralBody) super.clone();
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.GENERAL_BODY;
  }

}
