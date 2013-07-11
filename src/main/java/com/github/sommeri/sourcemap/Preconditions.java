package com.github.sommeri.sourcemap;

public class Preconditions {

  public static void checkState(boolean value) {
    if (!value) {
      throw new IllegalStateException();
    }
  }

  public static void checkState(boolean value, String message) {
    if (!value) {
      throw new IllegalStateException(message);
    }
  }

}
