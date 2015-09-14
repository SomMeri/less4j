package com.github.sommeri.less4j.utils;

public class Couple<T, M> {

  private T t;
  private M m;

  public Couple(T t, M m) {
    super();
    this.t = t;
    this.m = m;
  }

  public T getT() {
    return t;
  }

  public void setT(T t) {
    this.t = t;
  }

  public M getM() {
    return m;
  }

  public void setM(M m) {
    this.m = m;
  }

  @Override
  public String toString() {
    return "Couple [t=" + t + ", m=" + m + "]";
  }

}
