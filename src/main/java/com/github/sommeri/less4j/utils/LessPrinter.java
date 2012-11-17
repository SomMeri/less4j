package com.github.sommeri.less4j.utils;

import java.util.Iterator;

import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.NamespaceReference;

public class LessPrinter {
  
  public String toString(NamespaceReference reference) {
    StringBuilder result = new StringBuilder();
    Iterator<String> iterator = reference.getNameChain().iterator();
    if (iterator.hasNext())
      result.append(iterator.next());

    while (iterator.hasNext()) {
      result.append(" > ");
      result.append(iterator.next());
    }

    result.append(" > ");
    result.append(toString(reference.getFinalReference()));

    return result.toString();
  }

  private StringBuilder toString(MixinReference finalReference) {
    StringBuilder result = new StringBuilder(finalReference.getName());
    result.append("(...)");
    return result;
  }
}
