package com.github.sommeri.less4j.core.compiler.selectors;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.Selector;

public class UselessLessElementsRemover {
  
  private final SelectorsManipulator manipulator = new SelectorsManipulator();
  
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

  public void removeFrom(RuleSet ruleSet) {
    List<Selector> selectors = ruleSet.getSelectors();
    for (Selector selector : selectors) {
      Selector replacement = manipulator.removeAppenders(selector);
      if (replacement!=selector)
        ruleSet.replaceSelector(selector, replacement);
    }
  }

}
