package com.github.sommeri.less4j.core.ast;

import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class PureNamespace extends ASTCssNode {

  private IdSelector selector;
  private RuleSetsBody body;

  public PureNamespace(HiddenTokenAwareTree token) {
    super(token);
  }

  public PureNamespace(HiddenTokenAwareTree token, IdSelector selector, RuleSetsBody body) {
    super(token);
    this.selector = selector;
    this.body = body;
  }

  public IdSelector getSelector() {
    return selector;
   }

  public void setSelector(IdSelector selector) {
    this.selector = selector;
  }

  public String getName() {
    return "#"+selector.getName();
  }

  public RuleSetsBody getBody() {
    return body;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.PURE_NAMESPACE;
  } 
  
  @Override
  public PureNamespace clone() {
    PureNamespace result = (PureNamespace) super.clone();
    result.body = body==null? null : body.clone();
    result.selector = selector==null? null : selector.clone();
    return result;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(selector, body);
  }

  
}
