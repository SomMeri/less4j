package com.github.sommeri.less4j.core.parser;

import java.util.List;

import com.github.sommeri.less4j.core.parser.LessLexer;
import com.github.sommeri.less4j.core.ast.ElementSubsequent;
import com.github.sommeri.less4j.core.ast.NestedSelectorAppender;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.ast.SelectorCombinator;
import com.github.sommeri.less4j.core.ast.SelectorPart;
import com.github.sommeri.less4j.core.ast.SimpleSelector;
import com.github.sommeri.less4j.core.ast.SelectorCombinator.Combinator;

public class SelectorBuilder {

  private SimpleSelector currentSimpleSelector;
  private Selector result;
  private Selector currentSelector;
  private final HiddenTokenAwareTree token;

  private final ASTBuilderSwitch parentBuilder;
  private HiddenTokenAwareTree lastCombinator;

  public SelectorBuilder(HiddenTokenAwareTree token, ASTBuilderSwitch parentBuilder) {
    this.token = token;
    this.parentBuilder = parentBuilder;
  }

  public Selector buildSelector() {
    List<HiddenTokenAwareTree> members = token.getChildren();
    HiddenTokenAwareTree previousNonCombinator = null;
    for (HiddenTokenAwareTree kid : members) {
      switch (kid.getType()) {
      case LessLexer.ELEMENT_NAME:
        addElementName(kid);
        previousNonCombinator = kid;
        break;
      case LessLexer.NESTED_APPENDER:
        addAppender(kid);
        previousNonCombinator = null;
        break;
      case LessLexer.ELEMENT_SUBSEQUENT:
        addElementSubsequent(previousNonCombinator, kid);
        previousNonCombinator = kid;
        break;
      default:
        lastCombinator = kid;
      }

    }
    return result;
  }

  private void addElementSubsequent(HiddenTokenAwareTree previousNonSeparator, HiddenTokenAwareTree kid) {
    if (previousNonSeparator == null) {
      addWithImplicitStar(kid);
      return;
    }
    if (previousNonSeparator.getTokenStopIndex() + 1 < kid.getTokenStartIndex()) {
      addWithImplicitStar(kid);
      return;
    }
    //finally, add subsequent element to the previous simple selector
    addSubsequent(kid);
  }

  public void addSubsequent(HiddenTokenAwareTree kid) {
    currentSimpleSelector.addSubsequent((ElementSubsequent)parentBuilder.switchOn(kid.getChild(0)));
  }

  private void addWithImplicitStar(HiddenTokenAwareTree kid) {
    currentSimpleSelector = new SimpleSelector(kid, null, true);
    currentSimpleSelector.setEmptyForm(true);
    addSubsequent(kid);
    startNewSelector(currentSimpleSelector);
  }

  private SelectorCombinator consumeLastCombinator() {
    if (lastCombinator == null)
      return null;

    SelectorCombinator combinator = ConversionUtils.createSelectorCombinator(lastCombinator);
    lastCombinator = null;

    //if it is not descendant, everything is OK, it was explicitly written in the input.
    if (!isDescendant(combinator))
      return combinator;
    
    // Descendant combinator before first selector should be ignored.
    // Descendant combinator after appender should be ignored. That information is stored in appender itself.
    if (currentSelector==null || currentSelector.getHead().isAppender())
      return null;
    
    return combinator;
  }

  private void addElementName(HiddenTokenAwareTree kid) {
    HiddenTokenAwareTree realName = kid.getChild(0);
    currentSimpleSelector = new SimpleSelector(kid, realName.getText(), realName.getType() == LessLexer.STAR);
    startNewSelector(currentSimpleSelector);
  }

  private void addAppender(HiddenTokenAwareTree kid) {
    currentSimpleSelector = null;
    boolean directlyBefore = true;
    boolean directlyAfter = true;
    if  (kid.getChildren().size()==2) {
      directlyBefore = isMeaningfullWhitespace(kid);
      directlyAfter = !directlyBefore;
    } else if (kid.getChildren().size()==3) {
      directlyBefore = false;
      directlyAfter = false;
    }
      
    startNewSelector(new NestedSelectorAppender(kid, directlyBefore, directlyAfter));
    // Ignore descendant combinator before appender. This info is already hidden in appender.isDirectlyBefore. 
    if (isDescendant(currentSelector.getLeadingCombinator()))
      currentSelector.setLeadingCombinator(null);
  }

  private boolean isDescendant(SelectorCombinator combinator) {
    return combinator!=null && combinator.getCombinator()==Combinator.DESCENDANT;
  }

  private boolean isMeaningfullWhitespace(HiddenTokenAwareTree kid) {
    int type = kid.getChild(0).getType();
    return type == LessLexer.MEANINGFULL_WHITESPACE || type == LessLexer.DUMMY_MEANINGFULL_WHITESPACE;
  }

  private void startNewSelector(SelectorPart head) {
    Selector newSelector = new Selector(token);
    newSelector.setLeadingCombinator(consumeLastCombinator());
    if (currentSelector != null) {
      currentSelector.setRight(newSelector);
    } 

    currentSelector = newSelector;
    currentSelector.setHead(head);

    if (result == null)
      result = currentSelector;
  }

}
