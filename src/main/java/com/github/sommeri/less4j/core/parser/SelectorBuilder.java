package com.github.sommeri.less4j.core.parser;

import java.util.ArrayList;
import java.util.Iterator;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ElementSubsequent;
import com.github.sommeri.less4j.core.ast.Extend;
import com.github.sommeri.less4j.core.ast.MultiTargetExtend;
import com.github.sommeri.less4j.core.ast.PseudoClass;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.ast.SelectorCombinator;
import com.github.sommeri.less4j.core.ast.SelectorPart;
import com.github.sommeri.less4j.core.ast.SimpleSelector;
import com.github.sommeri.less4j.core.problems.BugHappened;

public class SelectorBuilder {

  private static String EXTEND_PSEUDO = "extend";

  private final HiddenTokenAwareTree token;
  private final ASTBuilderSwitch parent;
  private SelectorCombinator leadingCombinator = null;
  private SimpleSelector currentSimpleSelector = null;
  private SelectorPart nextPart = null;

  public SelectorBuilder(HiddenTokenAwareTree token, ASTBuilderSwitch astBuilderSwitch) {
    this.token = token;
    this.parent = astBuilderSwitch;
  }

  public Selector buildSelector() {
    Iterator<HiddenTokenAwareTree> iterator = token.getChildren().iterator();
    Selector result = new Selector(token, new ArrayList<SelectorPart>());

    currentSimpleSelector = null;
    while (iterator.hasNext()) {
      HiddenTokenAwareTree kid = iterator.next();
      if (ConversionUtils.isSelectorCombinator(kid)) {
        finishPart(result);
        leadingCombinator = ConversionUtils.createSelectorCombinator(kid);
      } else {
        ASTCssNode node = parent.switchOn(kid);
        if (node instanceof SelectorPart) {
          openPart(result, (SelectorPart) node);
        } else {
          if (currentSimpleSelector == null) {
            SimpleSelector empty = new SimpleSelector(kid, null, null, true);
            empty.setEmptyForm(true);
            openPart(result, empty);
          }

          currentSimpleSelector.addSubsequent((ElementSubsequent) node);
        }
      }
    }
    finishPart(result);
    return result;
  }

  private void openPart(Selector result, SelectorPart node) {
    finishPart(result);
    nextPart = node;

    if (nextPart instanceof SimpleSelector) {
      currentSimpleSelector = (SimpleSelector) nextPart;
    }

    nextPart.setLeadingCombinator(leadingCombinator);
    if (leadingCombinator != null)
      nextPart.getUnderlyingStructure().moveHidden(leadingCombinator.getUnderlyingStructure(), null);

    leadingCombinator = null;
  }

  private void finishPart(Selector result) {
    if (nextPart != null)
      addPart(result, nextPart);

    nextPart = null;
    currentSimpleSelector = null;
  }

  private void addPart(Selector selector, SelectorPart part) {
    ElementSubsequent lastSubsequent = part.getLastSubsequent();
    while (lastSubsequent != null && isExtends(lastSubsequent)) {
      convertAndAddExtends(selector, (PseudoClass) lastSubsequent);
      part.removeSubsequent(lastSubsequent);
      lastSubsequent = part.getLastSubsequent();
    }

    // if the part had only extend as members
    if (!part.isEmpty())
      selector.addPart(part);
  }

  private void convertAndAddExtends(Selector selector, PseudoClass extendPC) {
    ASTCssNode parameter = extendPC.getParameter();
    if (parameter.getType() == ASTCssNodeType.EXTEND) {
      selector.addExtend((Extend) parameter);
    } else if (parameter.getType() == ASTCssNodeType.MULTI_TARGET_EXTEND) {
      MultiTargetExtend extend = (MultiTargetExtend) parameter;
      for (Extend node : extend.getAllExtends()) {
        selector.addExtend((Extend) node);
      }
    } else {
      throw new BugHappened(ASTBuilderSwitch.GRAMMAR_MISMATCH, parameter.getUnderlyingStructure());
    }
  }

  private boolean isExtends(ElementSubsequent subsequent) {
    return (subsequent instanceof PseudoClass) && EXTEND_PSEUDO.equals(subsequent.getName());
  }
}
