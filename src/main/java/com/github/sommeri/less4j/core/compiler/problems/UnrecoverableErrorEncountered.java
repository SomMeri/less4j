package com.github.sommeri.less4j.core.compiler.problems;

@SuppressWarnings("serial")
public class UnrecoverableErrorEncountered extends RuntimeException {

  public UnrecoverableErrorEncountered() {
    super("Unrecoverable error encountered, compilation aborted.");
  }
}
