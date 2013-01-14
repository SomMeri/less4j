package com.github.sommeri.less4j.core.parser;

import java.util.Iterator;

import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.ast.SelectorCombinator;
import com.github.sommeri.less4j.core.ast.SelectorCombinator.Combinator;
import com.github.sommeri.less4j.core.ast.SelectorPart;

public class SelectorBuilder {

  private final HiddenTokenAwareTree token;
  private final ASTBuilderSwitch parent;

  public SelectorBuilder(HiddenTokenAwareTree token, ASTBuilderSwitch astBuilderSwitch) {
    this.token = token;
    this.parent = astBuilderSwitch;
  }

  public Selector buildSelector() {
    Iterator<HiddenTokenAwareTree> iterator = token.getChildren().iterator();
    Selector result = null;
    Selector rightest = null;
    while (iterator.hasNext()) {
      SelectorCombinator combinator = null;
      SelectorPart head = null;
      HiddenTokenAwareTree kid = iterator.next();

      if (ConversionUtils.isSelectorCombinator(kid)) {
        combinator = ConversionUtils.createSelectorCombinator(kid);
        kid = iterator.next();
        head = (SelectorPart) parent.switchOn(kid);
        // Ignore descendant combinator before appender. This info is already hidden in appender.isDirectlyBefore. 
        if (isDescendant(combinator) && kid.getType() == LessLexer.NESTED_APPENDER)
          combinator = null;
      } else {
        //if it is not a combinator, then it is either nested appender, simple selector or escaped selector   
        head = (SelectorPart) parent.switchOn(kid);
      }

      Selector part = new Selector(token, combinator, head);
      if (result == null) {
        result = part;
        rightest = part;
      } else {
        rightest.setRight(part);
        rightest = part;
      }
    }
    return result;
  }

  private boolean isDescendant(SelectorCombinator combinator) {
    return combinator != null && combinator.getCombinator() == Combinator.DESCENDANT;
  }
}
