package com.github.sommeri.less4j.utils;

import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.SelectorPart;

public class ArraysUtils {

  public static <T> T[] asArray(T... args) {
    return args;
  }

  public static <T> List<T> sameLengthSuffix(List<T> ofList, List<T> asThis) {
    if (asThis.size()>ofList.size())
      return null;
    
    int to = ofList.size();
    int from = to -  asThis.size();
    return ofList.subList(from, to);
  }

  public static <T> List<T> sameLengthPrefix(List<T> ofList, List<T> asThis) {
    if (asThis.size()>ofList.size())
      return null;
    
    return ofList.subList(0, asThis.size());
  }

  public static <T> List<T> sublistWithoutLast(List<T> values) {
    if (values.isEmpty())
      return null;
    
    return values.subList(0, values.size()-1);
  }

  public static <T> List<T> sublistWithoutFirst(List<T> values) {
    if (values.isEmpty())
      return null;
    
    return values.subList(1, values.size());
  }

  public static <T> List<T> safeSublist(List<T> values, int from, int to) {
    if (values.isEmpty())
      return null; 
    
    if (from>to)
      return new ArrayList<T>();
    
    if (from>values.size())
      from=values.size();

    if (to>values.size())
      to=values.size();

    return values.subList(from, to);
  }

  public static <T> T last(List<T> values) {
    if (values.isEmpty())
      return null;
    
    return values.get(values.size() - 1);
  }

  public static <T> T chopLast(List<T> values) {
    if (values.isEmpty())
      return null;
    
    return values.remove(values.size() - 1);
  }

  public static <T> T first(List<T> values) {
    if (values.isEmpty())
      return null;
    
    return values.get(0);
  }

  public static <T> T chopFirst(List<T> values) {
    if (values.isEmpty())
      return null;
    
    return values.remove(0);
  }

  public static <T> List<T> chopUpTo(List<T> list, T exclusiveTo) {
    int indx = list.indexOf(exclusiveTo);
    if (indx == -1)
      return new ArrayList<T>();

    List<T> subList = list.subList(0, indx);
    List<T> result = new ArrayList<T>(subList);
    subList.clear();
    return result;
  }

  public static <T> List<T> chopPrefix(List<T> list, int exclusiveTo) {
    if (exclusiveTo > list.size())
      exclusiveTo = list.size();

    List<T> subList = list.subList(0, exclusiveTo);
    List<T> result = new ArrayList<T>(subList);
    subList.clear();
    return result;
  }

  public static <T> List<T> asNonNullList(T... a) {
    List<T> result = new ArrayList<T>();
    for (T t : a) {
      if (t != null)
        result.add(t);
    }
    return result;
  }

  public static <T> List<T> addIfNonNull(List<T> destination, T... a) {
    for (T t : a) {
      if (t != null)
        destination.add(t);
    }
    return destination;
  }

  @SuppressWarnings("unchecked")
  public static <T extends ASTCssNode> List<T> deeplyClonedList(List<T> list) {
    List<T> result = new ArrayList<T>();
    for (T t : list) {
      result.add((T)t.clone());
    }
    return result;
  }

  public static <T> List<T> remaining(Iterator<T> iterator) {
    List<T> result = new ArrayList<T>();
    while (iterator.hasNext())
      result.add(iterator.next());

    return result;
  }

  public static <T> List<T> asModifiableList(T... head) {
    List<T> result = new ArrayList<T>();
    for (T t : head) {
      result.add(t);
    }
    return result;
  }

  public static <T> List<T> joinAll(List<T>... lists) {
    List<T> result = new ArrayList<T>();
    for (List<T> t : lists) {
      result.addAll(t);
    }
    return result;
  }

  public static <T> void replace(SelectorPart lookFor, List<T> inside, List<T> replaceBy) {
    int indx = inside.indexOf(lookFor);
    inside.remove(lookFor);
    inside.addAll(indx, replaceBy);
  }

  public static boolean isUtf8(byte[] input) {
    return isEncodedAs(input, "UTF-8");
  }

  public static boolean isUsAscii(byte[] input) {
    return isEncodedAs(input, "US-ASCII");
  }

  public static boolean isEncodedAs(byte[] input, String encoding) {
    CharsetDecoder decoder = Charset.forName(encoding).newDecoder().onMalformedInput(CodingErrorAction.REPORT).onUnmappableCharacter(CodingErrorAction.REPORT);
    try {
      decoder.decode(ByteBuffer.wrap(input));
    } catch (CharacterCodingException e) {
      return false;
    }
    return true;
  }

}
