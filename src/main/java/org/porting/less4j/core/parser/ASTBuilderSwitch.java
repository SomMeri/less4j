package org.porting.less4j.core.parser;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.porting.less4j.core.ast.ASTCssNode;
import org.porting.less4j.core.ast.CharsetDeclaration;
import org.porting.less4j.core.ast.ComposedExpression;
import org.porting.less4j.core.ast.CssClass;
import org.porting.less4j.core.ast.Declaration;
import org.porting.less4j.core.ast.Expression;
import org.porting.less4j.core.ast.FontFace;
import org.porting.less4j.core.ast.IdSelector;
import org.porting.less4j.core.ast.Pseudo;
import org.porting.less4j.core.ast.RuleSet;
import org.porting.less4j.core.ast.Selector;
import org.porting.less4j.core.ast.SelectorAttribute;
import org.porting.less4j.core.ast.SimpleSelector;
import org.porting.less4j.core.ast.StyleSheet;

class ASTBuilderSwitch extends TokenTypeSwitch<ASTCssNode> {
  
  private final TermBuilder termBuilder = new TermBuilder(this);

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

  public Expression handleTerm(CommonTree token) {
    return termBuilder.buildFromTerm(token);
  }
  
  public Expression handleExpression(CommonTree token) {
    List<CommonTree> children = getChildren(token);
    if (children.size() == 0)
      throw new IncorrectTreeException();
    
    return createExpression(token, children);
  }

  private Expression createExpression(CommonTree parent, List<CommonTree> members) {
    // this must represent a term. Otherwise we are doomed anyway.
    Expression head = (Expression) switchOn(members.get(0));

    ComposedExpression.Operator operator;
    if (members.size() == 1)
      return head;
    
    operator = toExpressionOperator(members.get(1));

    if (members.size() < 2)
      return new ComposedExpression(parent, head, operator, null);

    return new ComposedExpression(parent, head, operator, createExpression(parent, members.subList(2, members.size())));
  }

  private ComposedExpression.Operator toExpressionOperator(CommonTree token) {
    switch (token.getType()) {
    case LessLexer.SOLIDUS:
      return ComposedExpression.Operator.SOLIDUS;

    case LessLexer.COMMA:
      return ComposedExpression.Operator.COMMA;

    case LessLexer.STAR:
      return ComposedExpression.Operator.STAR;

    case LessLexer.EMPTY_SEPARATOR:
      return ComposedExpression.Operator.EMPTY_OPERATOR;

    default:
      break;
    }

    throw new IncorrectTreeException();
  }

  public Declaration handleDeclaration(CommonTree token) {
    List<CommonTree> children = getChildren(token);

    String name = children.get(0).getText();
    if (children.size() == 1)
      return new Declaration(token, name);

    CommonTree expressionToken = children.get(1);
    if (expressionToken.getType() == LessLexer.IMPORTANT_SYM)
      return new Declaration(token, name, null, true);

    Expression expression = (Expression) switchOn(expressionToken);
    if (children.size() == 2)
      return new Declaration(token, name, expression);

    if (children.get(2).getType() == LessLexer.IMPORTANT_SYM)
      return new Declaration(token, name, expression, true);

    throw new IncorrectTreeException();
  }

  public FontFace handleFontFace(CommonTree token) {
    // TODO not implemented yet, found out that I have to do declaration first
    return null;
  }

  public CharsetDeclaration handleCharsetDeclaration(CommonTree token) {
    // FIXME: just an idea, what does less.js do if charset is followed by a lot
    // of declarations?
    List<CommonTree> children = getChildren(token);
    if (children.isEmpty())
      throw new IncorrectTreeException();

    return new CharsetDeclaration(token, children.get(0).getText());
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

    List<Declaration> declarations = new ArrayList<Declaration>();
    for (CommonTree kid : children) {
      if (kid.getType() == LessLexer.DECLARATION)
        declarations.add(handleDeclaration(kid));
    }

    ruleSet.addDeclarations(declarations);
    return ruleSet;
  }

  public Selector handleSelector(CommonTree token) {
    List<CommonTree> members = getChildren(token);
    return createSelector(token, members);
  }

  public Selector createSelector(CommonTree parent, List<CommonTree> members) {
    if (members.size() == 0)
      throw new IncorrectTreeException();

    // this must represent a simple selector. Otherwise we are doomed anyway.
    SimpleSelector head = handleSimpleSelector(members.get(0));

    Selector.Combinator combinator;
    if (members.size() == 1)
      combinator = null;
    else
      combinator = toSelectorCombinator(members.get(1));

    if (members.size() < 2)
      return new Selector(parent, head, combinator, null);

    return new Selector(parent, head, combinator, createSelector(parent, members.subList(2, members.size())));
  }

  public Selector.Combinator toSelectorCombinator(CommonTree token) {
    switch (token.getType()) {
    case LessLexer.PLUS:
      return Selector.Combinator.PLUS;
    case LessLexer.GREATER:
      return Selector.Combinator.GREATER;
    case LessLexer.EMPTY_COMBINATOR:
      return Selector.Combinator.EMPTY;
    }
    
    throw new IllegalStateException("Unknown: " + token.getType());
  }
  public SimpleSelector handleSimpleSelector(CommonTree token) {
    List<CommonTree> members = getChildren(token);
    CommonTree theSelector = members.get(0);
    if (theSelector.getType() == LessLexer.STAR)
      return new SimpleSelector(token, null, true, new ArrayList<ASTCssNode>());

    // TODO this of course assumes that the tree is OK
    int startIndex = 0;
    String elementName = null;
    if (theSelector.getType() == LessLexer.ELEMENT_NAME) {
      elementName = getChildren(theSelector).get(0).getText();
      startIndex++;
    }

    if (startIndex >= members.size())
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

  public SelectorAttribute handleSelectorAttribute(CommonTree token) {
    List<CommonTree> children = getChildren(token);
    if (children.size() == 0)
      throw new IncorrectTreeException();

    if (children.size() == 1)
      return new SelectorAttribute(token, children.get(0).getText());

    if (children.size() < 3)
      throw new IncorrectTreeException();

    return new SelectorAttribute(token, children.get(0).getText(), toSelectorOperator(children.get(1)), children.get(2).getText());
  }

  private SelectorAttribute.Operator toSelectorOperator(CommonTree token) {
    switch (token.getType()) {
    case LessLexer.OPEQ:
      return SelectorAttribute.Operator.EQUALS;

    case LessLexer.INCLUDES:
      return SelectorAttribute.Operator.INCLUDES;

    case LessLexer.DASHMATCH:
      return SelectorAttribute.Operator.SPECIAL_PREFIX;

    case LessLexer.PREFIXMATCH:
      return SelectorAttribute.Operator.PREFIXMATCH;

    case LessLexer.SUFFIXMATCH:
      return SelectorAttribute.Operator.SUFFIXMATCH;

    case LessLexer.SUBSTRINGMATCH:
      return SelectorAttribute.Operator.SUBSTRINGMATCH;

    default:
      break;
    }

    throw new IncorrectTreeException();
  }

  public Pseudo handlePseudo(CommonTree token) {
    List<CommonTree> children = getChildren(token);
    if (children.size() == 0)
      throw new IncorrectTreeException();

    if (children.size() == 1)
      return new Pseudo(token, children.get(0).getText());

    if (children.size() == 2)
      return new Pseudo(token, children.get(0).getText(), children.get(1).getText());

    // FIXME: handle this case the same way as less.js
    throw new IncorrectTreeException();
  }

  public IdSelector handleIdSelector(CommonTree token) {
    List<CommonTree> children = getChildren(token);
    if (children.size() != 1)
      throw new IncorrectTreeException();

    String text = children.get(0).getText();
    if (text == null || text.length() < 1)
      throw new IncorrectTreeException();

    return new IdSelector(token, text.substring(1));
  }

}

// FIXME: do something meaningful instead of this exception
@SuppressWarnings("serial")
class IncorrectTreeException extends RuntimeException {

  public IncorrectTreeException() {
  }

  public IncorrectTreeException(String message) {
    super(message);
  }

}