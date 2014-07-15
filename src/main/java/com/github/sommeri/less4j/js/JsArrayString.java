package com.github.sommeri.less4j.js;

/**
 * A simple wrapper around a homogeneous native array of string values.
 * 
 * This class may not be directly instantiated, and can only be returned from a
 * native method. For example,
 * 
 * <code>
 * native JsArrayString getNativeArray() /*-{
 *   return ['foo', 'bar', 'baz'];
 * }-* /;
 * </code>
 */
public class JsArrayString extends JavaScriptObject {

  protected JsArrayString() {
  }

  /**
   * Gets the value at a given index.
   * 
   * @param index the index to be retrieved
   * @return the value at the given index, or <code>null</code> if none exists
   */
  public final native String get(int index) /*-{
    return this[index];
  }-*/;

  /**
   * Convert each element of the array to a String and join them with a comma
   * separator. The value returned from this method may vary between browsers
   * based on how JavaScript values are converted into strings.
   */
  public final String join() {
    // As per JS spec
    return join(",");
  }

  /**
   * Convert each element of the array to a String and join them with a comma
   * separator. The value returned from this method may vary between browsers
   * based on how JavaScript values are converted into strings.
   */
  public final native String join(String separator) /*-{
    return this.join(separator);
  }-*/;

  /**
   * Gets the length of the array.
   * 
   * @return the array length
   */
  public final native int length() /*-{
    return this.length;
  }-*/;

  /**
   * Pushes the given value onto the end of the array.
   */
  public final native void push(String value) /*-{
    this[this.length] = value;
  }-*/;

  /**
   * Sets the value value at a given index.
   * 
   * If the index is out of bounds, the value will still be set. The array's
   * length will be updated to encompass the bounds implied by the added value.
   * 
   * @param index the index to be set
   * @param value the value to be stored
   */
  public final native void set(int index, String value) /*-{
    this[index] = value;
  }-*/;

  /**
   * Reset the length of the array.
   * 
   * @param newLength the new length of the array
   */
  public final native void setLength(int newLength) /*-{
    this.length = newLength;
  }-*/;

  /**
   * Shifts the first value off the array.
   * 
   * @return the shifted value
   */
  public final native String shift() /*-{
    return this.shift();
  }-*/;

  /**
   * Shifts a value onto the beginning of the array.
   * 
   * @param value the value to the stored
   */
  public final native void unshift(String value) /*-{
    this.unshift(value);
  }-*/;
}