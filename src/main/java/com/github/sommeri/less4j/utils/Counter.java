package com.github.sommeri.less4j.utils;

public class Counter {

  private int value = 0;
      
  public Counter() {
  }

  public Counter(int initValue) {
    this.value = initValue;
  }

  public void increment(){
    value++;
  }

  public int getValue() {
    return value;
  }
  
}
