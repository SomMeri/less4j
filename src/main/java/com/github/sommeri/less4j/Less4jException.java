package com.github.sommeri.less4j;

import com.github.sommeri.less4j.core.TranslationException;

@SuppressWarnings("serial")
public class Less4jException extends Exception {

  public Less4jException(TranslationException ex) {
    super(ex);
    // TODO do something reasonable here
  }

  @Override
  public synchronized TranslationException getCause() {
    return (TranslationException) super.getCause();
  }
  
}
