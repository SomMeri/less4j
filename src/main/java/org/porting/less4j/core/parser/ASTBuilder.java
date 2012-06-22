package org.porting.less4j.core.parser;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.porting.less4j.core.ast.ASTCssNode;
import org.porting.less4j.core.ast.Combinator;
import org.porting.less4j.core.ast.CssClass;
import org.porting.less4j.core.ast.Pseudo;
import org.porting.less4j.core.ast.RuleSet;
import org.porting.less4j.core.ast.Selector;
import org.porting.less4j.core.ast.SimpleSelector;
import org.porting.less4j.core.ast.StyleSheet;

public class ASTBuilder {

  public StyleSheet parse(CommonTree tree) {
    ASTBuilderSwitch builder = new ASTBuilderSwitch();
    ASTCssNode result = builder.switchOn(tree);
    return (StyleSheet) result;
  }

}

class ASTBuilderSwitch extends TokenTypeSwitch<ASTCssNode> {

  public StyleSheet handleStyleSheet(CommonTree token) {
    StyleSheet result = new StyleSheet(token);
    for (CommonTree kid : getChildren(token)) {
      result.addChild(switchOn(kid));
    }

    return result;
  }

  @SuppressWarnings("unchecked")
  private List<CommonTree> getChildren(CommonTree token) {
    return (List<CommonTree>) token.getChildren();
  }

  public RuleSet handleRuleSet(CommonTree token) {
    RuleSet ruleSet = new RuleSet(token);

    List<Selector> selectors = new ArrayList<Selector>();
    List<CommonTree> children = getChildren(token);
    for (CommonTree kid : children) {
      if (kid.getType() == LessLexer.SELECTOR)
        selectors.add(handleSelector(kid));
    }

    ruleSet.addSelectors(selectors);
    return ruleSet;
  }
  
  public Selector handleSelector(CommonTree token) {
    List<CommonTree> members = getChildren(token);
    return createSelector(token, members);
  }

  public Selector createSelector(CommonTree parent, List<CommonTree> members) {
    if (members.size()==0)
      throw new IncorrectTreeException();

    //this must represent a simple selector. Otherwise we are doomed anyway.
    SimpleSelector head = handleSimpleSelector(members.get(0));
    
    Combinator combinator;
    if (members.size()==1)
      combinator = null;
    else 
      combinator= Combinator.convert(members.get(1));
      
    if (members.size()<2)
      return new Selector(parent, head, combinator, null);
    
    return new Selector(parent, head, combinator, createSelector(parent, members.subList(2, members.size())));
  }

  public SimpleSelector handleSimpleSelector(CommonTree token) {
    List<CommonTree> members = getChildren(token);
    CommonTree theSelector = members.get(0);
    if (theSelector.getType()==LessLexer.STAR) 
      return new SimpleSelector(token, null, true, new ArrayList<ASTCssNode>());

    //TODO this of course assumes that the tree is OK
    int startIndex = 0;
    String elementName = null;
    if (theSelector.getType()==LessLexer.ELEMENT_NAME) {
      elementName = getChildren(theSelector).get(0).getText();
      startIndex++;
    }

    if (startIndex>=members.size())
      return new SimpleSelector(token, elementName, false, new ArrayList<ASTCssNode>());
    
    List<CommonTree> subsequentMembers = members.subList(startIndex, members.size());
    List<ASTCssNode> subsequent = new ArrayList<ASTCssNode>();
    for (CommonTree commonTree : subsequentMembers) {
      subsequent.add(switchOn(commonTree));
    }
    
    return new SimpleSelector(token, elementName, false, subsequent);
  }

  public CssClass handleCssClass(CommonTree token) {
    List<CommonTree> children = getChildren(token);
    CssClass result = new CssClass(token, children.get(0).getText());
    return result;
  }

  public Pseudo handlePseudo(CommonTree token) {
    List<CommonTree> children = getChildren(token);
    if (children.size()==0)
      throw new IncorrectTreeException();
    
    if (children.size()==1)
      return new Pseudo(token, children.get(0).getText());
    
    if (children.size()==2)
      return new Pseudo(token, children.get(0).getText(), children.get(1).getText());

    //FIXME: handle this case the same way as less.js
    throw new IncorrectTreeException();
  }
}

//FIXME: do something meaningful instead of this exception
@SuppressWarnings("serial")
class IncorrectTreeException extends RuntimeException {
  
}
