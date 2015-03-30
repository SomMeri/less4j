package com.github.sommeri.less4j.core.ast;

public interface BodyOwner<T extends Body> {

  void setBody(T body);
  
  T getBody();
  
  BodyOwner<?> clone();
}
