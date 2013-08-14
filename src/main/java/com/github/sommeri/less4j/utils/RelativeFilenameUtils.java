package com.github.sommeri.less4j.utils;

import java.io.File;

import org.apache.commons.io.FilenameUtils;

//copied from org.apache.commons.io.FilenameUtils.doNormalize and customized
public class RelativeFilenameUtils {

  private static final char SYSTEM_SEPARATOR = File.separatorChar;
  private static final char UNIX_SEPARATOR = '/';
  private static final char WINDOWS_SEPARATOR = '\\';
  private static final char OTHER_SEPARATOR;
  static {
    if (SYSTEM_SEPARATOR == WINDOWS_SEPARATOR) {
      OTHER_SEPARATOR = UNIX_SEPARATOR;
    } else {
      OTHER_SEPARATOR = WINDOWS_SEPARATOR;
    }
  }

  public static String normalizeNoEndSeparator(String filename) {
    return doNormalize(filename, SYSTEM_SEPARATOR, false);
  }

  private static String doNormalize(String filename, char separator, boolean keepSeparator) {
    if (filename == null) {
      return null;
    }
    int size = filename.length();
    if (size == 0) {
      return filename;
    }
    int prefix = FilenameUtils.getPrefixLength(filename);
    if (prefix < 0) {
      return null;
    }

    char[] array = new char[size + 2]; // +1 for possible extra slash, +2 for arraycopy
    filename.getChars(0, filename.length(), array, 0);

    // fix separators throughout
    char otherSeparator = separator == SYSTEM_SEPARATOR ? OTHER_SEPARATOR : SYSTEM_SEPARATOR;
    for (int i = 0; i < array.length; i++) {
      if (array[i] == otherSeparator) {
        array[i] = separator;
      }
    }

    // add extra separator on the end to simplify code below
    boolean lastIsDirectory = true;
    if (array[size - 1] != separator) {
      array[size++] = separator;
      lastIsDirectory = false;
    }

    // adjoining slashes
    for (int i = prefix + 1; i < size; i++) {
      if (array[i] == separator && array[i - 1] == separator) {
        System.arraycopy(array, i, array, i - 1, size - i);
        size--;
        i--;
      }
    }

    // dot slash
    for (int i = prefix + 1; i < size; i++) {
      if (array[i] == separator && array[i - 1] == '.' && (i == prefix + 1 || array[i - 2] == separator)) {
        if (i == size - 1) {
          lastIsDirectory = true;
        }
        System.arraycopy(array, i + 1, array, i - 1, size - i);
        size -= 2;
        i--;
      }
    }

    // double dot slash
    outer: for (int i = prefix + 2; i < size; i++) {
      if (isEndOfClimbUp(array, i, separator) && (i == prefix + 2 || array[i - 3] == separator)) {
        //if (located in the beginning or after drive or server name)
        if (i == prefix + 2) {
          // Modified by SomMeri to allow relative paths
          //return null;
          continue outer;
        }
        if (i == size - 1) {
          lastIsDirectory = true;
        }
        if (isEndOfClimbUp(array, i - 3, separator)) {
          // We reached previous climb up. That can happen only if we are at the 
          // beginning of the path and the path is starting with some climbs. e.g.
          // path looks somewhat like this: ../../something
          //
          // We cannot climb further, so no action is needed.
        } else {
          int j;
          for (j = i - 4; j >= prefix; j--) {
            if (array[j] == separator) {
              // remove b/../ from a/b/../c
              System.arraycopy(array, i + 1, array, j + 1, size - i);
              size -= i - j;
              i = j + 1;
              continue outer;
            }
          }
          // remove a/../ from a/../c
          System.arraycopy(array, i + 1, array, prefix, size - i);
          size -= i + 1 - prefix;
          i = prefix + 1;
        }
      }
    }

    if (size <= 0) { // should never be less than 0
      return "";
    }
    if (size <= prefix) { // should never be less than prefix
      return new String(array, 0, size);
    }
    if (lastIsDirectory && keepSeparator) {
      return new String(array, 0, size); // keep trailing separator
    }
    return new String(array, 0, size - 1); // lose trailing separator
  }

  private static boolean isEndOfClimbUp(char[] array, int position, char separator) {
    return array[position] == separator && array[position - 1] == '.' && array[position - 2] == '.';
  }

}
