package com.github.sommeri.less4j;

@SuppressWarnings("serial")
public abstract class Less4jException extends RuntimeException {

  protected Less4jException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public Less4jException(String message, Throwable cause) {
    super(message, cause);
  }

  public Less4jException(String message) {
    super(message);
  }

  public Less4jException(Throwable cause) {
    super(cause);
  }

  public Less4jException() {
  }

  protected String getPositionInformation() {
    if (!hasErrorPosition())
      return "";

    String result = "\n Line: " + getLine() + " Character: " + getCharPositionInLine();
    return result;
  }

  /**
   * @return <code>true</code> if the position of the error in the input file is known.
   */
  public abstract boolean hasErrorPosition();

  /**
   * @return position of the first character that caused the error. First character in line has number 1. If the causing character
   * number is not known, returns -1; 
   */
  public abstract int getCharPositionInLine();

  /**
   * @return number of line that caused the error. The number of the first line in the file is 1. If the causing line
   * number is not known, returns -1; 
   */
  public abstract int getLine();

}
