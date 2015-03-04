package com.github.sommeri.less4j.core.ast;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public abstract class Directive extends ASTCssNode implements BodyOwner<GeneralBody> {

	public Directive(HiddenTokenAwareTree underlyingStructure) {
		super(underlyingStructure);
	}

  public boolean hasBody() {
    return getBody()!=null;
  }

}
