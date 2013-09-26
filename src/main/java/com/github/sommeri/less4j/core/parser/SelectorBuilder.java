package com.github.sommeri.less4j.core.parser;

import java.util.ArrayList;
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
    Selector result = new Selector(token, new ArrayList<SelectorPart>());
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

      head.setLeadingCombinator(combinator);
      if (combinator != null)
        head.getUnderlyingStructure().moveHidden(combinator.getUnderlyingStructure(), null);

      result.addPart(head);
    }
    return result;
  }

  private boolean isDescendant(SelectorCombinator combinator) {
    return combinator != null && combinator.getCombinator() == Combinator.DESCENDANT;
  }
}
