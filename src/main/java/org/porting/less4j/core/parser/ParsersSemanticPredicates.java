package org.porting.less4j.core.parser;

import java.util.HashSet;
import java.util.Set;

import org.antlr.runtime.Token;

public class ParsersSemanticPredicates {

  private static Set<String> NTH_PSEUDOCLASSES = new HashSet<String>();
  static {
    NTH_PSEUDOCLASSES.add("nth-child");
    NTH_PSEUDOCLASSES.add("nth-last-child");
    NTH_PSEUDOCLASSES.add("nth-of-type");
    NTH_PSEUDOCLASSES.add("nth-last-of-type");
  }

  public boolean isNthPseudoClass(Token a) {
    if (a == null)
      return false;
    String text = a.getText();
    if (text == null)
      return false;
    return NTH_PSEUDOCLASSES.contains(text.toLowerCase());
  }

}
