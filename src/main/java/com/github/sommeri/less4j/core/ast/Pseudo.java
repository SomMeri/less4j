package com.github.sommeri.less4j.core.ast;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public abstract class Pseudo extends ElementSubsequent {

  private String name;

  public Pseudo(HiddenTokenAwareTree token, String name) {
    super(token);
    this.name = name;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public void extendName(String extension) {
    setName(getName() + extension);
  }

  @Override
  public boolean isInterpolated() {
    return false;
  }

  @Override
  public Pseudo clone() {
    return (Pseudo) super.clone();
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    //builder.append(getClass().getName());
    //builder.append(" [name=");
    builder.append(name);
    //builder.append("]");
    return builder.toString();
  }

}
