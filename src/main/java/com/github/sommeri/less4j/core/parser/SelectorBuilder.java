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
import com.github.sommeri.less4j.core.ast.SelectorCombinator.CombinatorType;
import com.github.sommeri.less4j.core.ast.SelectorPart;
import com.github.sommeri.less4j.core.problems.BugHappened;

public class SelectorBuilder {

  private static String EXTEND_PSEUDO = "extend";

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
      SelectorPart part = null;
      HiddenTokenAwareTree kid = iterator.next();

      if (ConversionUtils.isSelectorCombinator(kid)) {
        combinator = ConversionUtils.createSelectorCombinator(kid);
        kid = iterator.next();
        part = (SelectorPart) parent.switchOn(kid);
        // Ignore descendant combinator before appender. This info is already hidden in appender.isDirectlyBefore. 
        if (isDescendant(combinator) && kid.getGeneralType() == LessLexer.NESTED_APPENDER)
          combinator = null;
      } else {
        //if it is not a combinator, then it is either nested appender, simple selector or escaped selector   
        part = (SelectorPart) parent.switchOn(kid);
      }

      part.setLeadingCombinator(combinator);
      if (combinator != null)
        part.getUnderlyingStructure().moveHidden(combinator.getUnderlyingStructure(), null);

      addPart(result, part);
    }
    return result;
  }

  private void addPart(Selector selector, SelectorPart part) {
    ElementSubsequent lastSubsequent = part.getLastSubsequent();
    while (lastSubsequent!=null && isExtends(lastSubsequent)) {
      convertAndAddExtends(selector, (PseudoClass) lastSubsequent);
      part.removeSubsequent(lastSubsequent);
      lastSubsequent = part.getLastSubsequent();
    } 
    
    //if the part had only extend as members
    if (!part.isEmpty())
      selector.addPart(part);
  }
  
  private void convertAndAddExtends(Selector selector, PseudoClass extendPC) {
    ASTCssNode parameter = extendPC.getParameter();
    if (parameter.getType()==ASTCssNodeType.EXTEND) {
      selector.addExtend((Extend) parameter);
    } else if (parameter.getType()==ASTCssNodeType.MULTI_TARGET_EXTEND) {
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


  private boolean isDescendant(SelectorCombinator combinator) {
    return combinator != null && combinator.getCombinatorType() == CombinatorType.DESCENDANT;
  }
}
