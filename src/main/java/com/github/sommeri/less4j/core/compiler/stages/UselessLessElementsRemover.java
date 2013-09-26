package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.ast.SimpleSelector;

public class UselessLessElementsRemover {
  
  public void removeUselessLessElements(ASTCssNode node) {
    switch (node.getType()) {
    case RULE_SET:
      removeFrom((RuleSet) node);
      break;

    case CHARSET_DECLARATION:
    case IMPORT:
      break;

    default:
      List<ASTCssNode> childs = new ArrayList<ASTCssNode>(node.getChilds());
      for (ASTCssNode kid : childs) {
        removeUselessLessElements(kid);
      }
    }
  }

  private void removeFrom(RuleSet ruleSet) {
    List<Selector> selectors = ruleSet.getSelectors();
    for (Selector selector : selectors) {
      removeFrom(selector, ruleSet);
    }
  }

  private void removeFrom(Selector selector, RuleSet parentRuleSet) {
    selector = replaceLeadingAppendersByEmptiness(selector, parentRuleSet);
    if (!selector.containsAppender())
      return ;
    
    SelectorsManipulator manipulator= new SelectorsManipulator();
    Selector empty = new Selector(selector.getUnderlyingStructure(), createEmptySimpleSelector(selector));
    List<Selector> replaceAppenders = manipulator.replaceAppenders(selector, Arrays.asList(empty));
    Selector replacement = replaceAppenders.get(0);
    parentRuleSet.replaceSelector(selector, replacement);
  }

  private Selector replaceLeadingAppendersByEmptiness(Selector selector, RuleSet parentRuleSet) {
    while (!selector.isEmpty() && selector.getHead().isAppender()) {
      selector.getHead().setParent(null);
      selector.removeHead();
    }
    
    if (selector.isEmpty()) {
      SimpleSelector empty = createEmptySimpleSelector(selector);
      selector.addPart(empty);
    }
    return selector;
  }

  private SimpleSelector createEmptySimpleSelector(ASTCssNode underlyingStructureSource) {
    SimpleSelector empty = new SimpleSelector(underlyingStructureSource.getUnderlyingStructure(), null, null, true);
    empty.setEmptyForm(true);
    return empty;
  }

}
