package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public abstract class Expression extends ASTCssNode implements IScopeAware {

  private IScope ownerScope;

  public Expression(HiddenTokenAwareTree token) {
    super(token);
  }

  @Override
  @NotAstProperty
  public IScope getScope() {
    return ownerScope;
  }

  @Override
  @NotAstProperty
  public void setScope(IScope scope) {
    this.ownerScope = scope;
  }

  @Override
  @NotAstProperty
  public boolean hasScope() {
    return ownerScope!=null;
  }

  public List<Expression> splitByComma() {
    return Collections.singletonList(this);
  }
  
  @Override
  public Expression clone() {
    Expression clone = (Expression) super.clone();
    return clone;
  }

}
