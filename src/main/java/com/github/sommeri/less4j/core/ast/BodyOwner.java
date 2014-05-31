package com.github.sommeri.less4j.core.ast;

import com.github.sommeri.less4j.utils.PubliclyCloneable;

public interface BodyOwner<T extends Body> extends PubliclyCloneable {

  public BodyOwner<T> clone();

  void setBody(T body);
  
  T getBody();
  
}
