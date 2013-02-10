package com.github.sommeri.less4j.core.ast;

public interface BodyOwner<T extends ASTCssNode> {

  void setBody(T body);
  
  T getBody();
  
}
