package org.porting.less4j.core.parser;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.Token;
import org.porting.less4j.core.ast.ASTCssNode;
import org.porting.less4j.core.ast.CharsetDeclaration;
import org.porting.less4j.core.ast.Comment;
import org.porting.less4j.core.ast.ComposedExpression;
import org.porting.less4j.core.ast.CssClass;
import org.porting.less4j.core.ast.Declaration;
import org.porting.less4j.core.ast.Expression;
import org.porting.less4j.core.ast.ExpressionOperator;
import org.porting.less4j.core.ast.FontFace;
import org.porting.less4j.core.ast.IdSelector;
import org.porting.less4j.core.ast.Media;
import org.porting.less4j.core.ast.Medium;
import org.porting.less4j.core.ast.Pseudo;
import org.porting.less4j.core.ast.RuleSet;
import org.porting.less4j.core.ast.DeclarationsBody;
import org.porting.less4j.core.ast.Selector;
import org.porting.less4j.core.ast.SelectorAttribute;
import org.porting.less4j.core.ast.SelectorCombinator;
import org.porting.less4j.core.ast.SelectorOperator;
import org.porting.less4j.core.ast.SimpleSelector;
import org.porting.less4j.core.ast.StyleSheet;

class ASTBuilderSwitch extends TokenTypeSwitch<ASTCssNode> {

  private final TermBuilder termBuilder = new TermBuilder(this);

  @Override
  public <M extends ASTCssNode> M postprocess(M something) {
    HiddenTokenAwareTree underlyingStructure = something.getUnderlyingStructure();
    List<Comment> preceeding = convertToComments(underlyingStructure.getPreceeding());
    something.setOpeningComments(preceeding);

    List<Comment> following = convertToComments(underlyingStructure.getFollowing());
    something.setTrailingComments(following);

    List<Comment> orphans = convertToComments(underlyingStructure.getOrphans());
    something.setOrphanComments(orphans);
    return something;
  }

  private List<Comment> convertToComments(List<Token> preceeding) {
    List<Comment> result = new ArrayList<Comment>();

    Comment comment = null;
    for (Token token : preceeding) {
      if (token.getType() == LessLexer.COMMENT) {
        comment = new Comment(new HiddenTokenAwareTree(token));
        result.add(comment);
      }
      if (token.getType() == LessLexer.NEW_LINE) {
        if (comment!=null)
          comment.setHasNewLine(true);
      }
    }

    //FIXME DOCUMENT: I do not follow less.js /* A C-style comment */\n\n I will create 1 new line less 2 new lines 


    return result;
  }

  public StyleSheet handleStyleSheet(HiddenTokenAwareTree token) {
    StyleSheet result = new StyleSheet(token);
    for (HiddenTokenAwareTree kid : token.getChildren()) {
      result.addChild(switchOn(kid));
    }

    return result;
  }

  public Expression handleTerm(HiddenTokenAwareTree token) {
    return termBuilder.buildFromTerm(token);
  }

  public Expression handleExpression(HiddenTokenAwareTree token) {
    List<HiddenTokenAwareTree> children = token.getChildren();
    if (children.size() == 0)
      throw new IncorrectTreeException();
    
    if (children.size() == 1) {
      Expression head = (Expression) switchOn(children.get(0));
      // we have to switch to parent token, because we would loose it otherwise.
      // for example, comments before simple expressions would not be accessible anymore 
      head.setUnderlyingStructure(token);
      return head;
    }

    return createExpression(token, children);
  }

  private Expression createExpression(HiddenTokenAwareTree parent, List<HiddenTokenAwareTree> members) {
    // this must represent a term. Otherwise we are doomed anyway.
    Expression head = (Expression) switchOn(members.get(0));
    if (members.size() == 1) {
      return head;
    }

    HiddenTokenAwareTree token = members.get(1);
    ExpressionOperator operator = postprocess(new ExpressionOperator(token, toExpressionOperator(token)));

    if (members.size() < 2)
      return new ComposedExpression(parent, head, operator, null);

    return new ComposedExpression(parent, head, operator, createExpression(parent, members.subList(2, members.size())));
  }

  private ExpressionOperator.Operator toExpressionOperator(HiddenTokenAwareTree token) {
    switch (token.getType()) {
    case LessLexer.SOLIDUS:
      return ExpressionOperator.Operator.SOLIDUS;

    case LessLexer.COMMA:
      return ExpressionOperator.Operator.COMMA;

    case LessLexer.STAR:
      return ExpressionOperator.Operator.STAR;

    case LessLexer.EMPTY_SEPARATOR:
      return ExpressionOperator.Operator.EMPTY_OPERATOR;

    default:
      break;
    }

    throw new IncorrectTreeException();
  }

  public Declaration handleDeclaration(HiddenTokenAwareTree token) {
    List<HiddenTokenAwareTree> children = token.getChildren();

    String name = children.get(0).getText();
    if (children.size() == 1)
      return new Declaration(token, name);

    HiddenTokenAwareTree expressionToken = children.get(1);
    if (expressionToken.getType() == LessLexer.IMPORTANT_SYM)
      return new Declaration(token, name, null, true);

    Expression expression = (Expression) switchOn(expressionToken);
    if (children.size() == 2)
      return new Declaration(token, name, expression);

    if (children.get(2).getType() == LessLexer.IMPORTANT_SYM)
      return new Declaration(token, name, expression, true);

    throw new IncorrectTreeException();
  }

  public FontFace handleFontFace(HiddenTokenAwareTree token) {
    FontFace result = new FontFace(token);

    List<HiddenTokenAwareTree> children = token.getChildren();
    List<Declaration> declarations = new ArrayList<Declaration>();
    for (HiddenTokenAwareTree kid : children) {
      if (kid.getType() == LessLexer.DECLARATION)
        declarations.add(postprocess(handleDeclaration(kid)));
    }

    result.addDeclarations(declarations);
    return result;
  }

  public CharsetDeclaration handleCharsetDeclaration(HiddenTokenAwareTree token) {
    // FIXME: just an idea, what does less.js do if charset is followed by a lot
    // of declarations?
    List<HiddenTokenAwareTree> children = token.getChildren();
    if (children.isEmpty())
      throw new IncorrectTreeException();

    return new CharsetDeclaration(token, children.get(0).getText());
  }

  public RuleSet handleRuleSet(HiddenTokenAwareTree token) {
    RuleSet ruleSet = new RuleSet(token);

    List<Selector> selectors = new ArrayList<Selector>();
    List<HiddenTokenAwareTree> children = token.getChildren();
    
    ASTCssNode previousKid = null;
    for (HiddenTokenAwareTree kid : children) {
      if (kid.getType() == LessLexer.SELECTOR) {
        Selector selector = postprocess(handleSelector(kid));
        selectors.add(selector);
        previousKid = selector;
      }
      if (kid.getType() == LessLexer.BODY_OF_DECLARATIONS) {
        DeclarationsBody body = postprocess(handleDeclarationsBody(kid));
        ruleSet.setBody(body);
        previousKid = body;
      }
      if (kid.getType() == LessLexer.COMMA) {
        List<Comment> comments = convertToComments(kid.getPreceeding());
        previousKid.addTrailingComments(comments);
      }
    }

    ruleSet.addSelectors(selectors);
    return ruleSet;
  }

  public DeclarationsBody handleDeclarationsBody(HiddenTokenAwareTree token) {
    if (token.getChildren()==null)
      return new DeclarationsBody(token);
    
    List<Declaration> declarations = new ArrayList<Declaration>();
    for (HiddenTokenAwareTree kid : token.getChildren()) {
      if (kid.getType() == LessLexer.DECLARATION)
        declarations.add(postprocess(handleDeclaration(kid)));
    }

    return new DeclarationsBody(token, declarations);
  }

  public Selector handleSelector(HiddenTokenAwareTree token) {
    List<HiddenTokenAwareTree> members = token.getChildren();
    return postprocess(createSelector(token, members));
  }

  public Selector createSelector(HiddenTokenAwareTree parent, List<HiddenTokenAwareTree> members) {
    if (members.size() == 0)
      throw new IncorrectTreeException();

    // this must represent a simple selector. Otherwise we are doomed anyway.
    SimpleSelector head = postprocess(handleSimpleSelector(members.get(0)));

    SelectorCombinator combinator;
    if (members.size() == 1)
      combinator = null;
    else {
      HiddenTokenAwareTree token = members.get(1);
      combinator = postprocess(new SelectorCombinator(token, toSelectorCombinator(token)));
    }

    if (members.size() < 2)
      return postprocess(new Selector(parent, head, combinator, null));

    //FIXME: bad design, there should not be a need to add postprocess call everywhere 
    return new Selector(parent, head, combinator, createSelector(parent, members.subList(2, members.size())));
  }

  public SelectorCombinator.Combinator toSelectorCombinator(HiddenTokenAwareTree token) {
    switch (token.getType()) {
    case LessLexer.PLUS:
      return SelectorCombinator.Combinator.PLUS;
    case LessLexer.GREATER:
      return SelectorCombinator.Combinator.GREATER;
    case LessLexer.EMPTY_COMBINATOR:
      return SelectorCombinator.Combinator.EMPTY;
    }

    throw new IllegalStateException("Unknown: " + token.getType());
  }

  public SimpleSelector handleSimpleSelector(HiddenTokenAwareTree token) {
    List<HiddenTokenAwareTree> members = token.getChildren();
    HiddenTokenAwareTree theSelector = members.get(0);
    if (theSelector.getType() == LessLexer.STAR)
      return new SimpleSelector(token, null, true, new ArrayList<ASTCssNode>());

    int startIndex = 0;
    String elementName = null;
    if (theSelector.getType() == LessLexer.ELEMENT_NAME) {
      elementName = theSelector.getChildren().get(0).getText();
      startIndex++;
    }

    if (startIndex >= members.size())
      return new SimpleSelector(token, elementName, false, new ArrayList<ASTCssNode>());

    List<HiddenTokenAwareTree> subsequentMembers = members.subList(startIndex, members.size());
    List<ASTCssNode> subsequent = new ArrayList<ASTCssNode>();
    for (HiddenTokenAwareTree HiddenTokenAwareTree : subsequentMembers) {
      subsequent.add(switchOn(HiddenTokenAwareTree));
    }

    return new SimpleSelector(token, elementName, false, subsequent);
  }

  public CssClass handleCssClass(HiddenTokenAwareTree token) {
    List<HiddenTokenAwareTree> children = token.getChildren();
    CssClass result = new CssClass(token, children.get(0).getText());
    return result;
  }

  public SelectorAttribute handleSelectorAttribute(HiddenTokenAwareTree token) {
    List<HiddenTokenAwareTree> children = token.getChildren();
    if (children.size() == 0)
      throw new IncorrectTreeException();

    if (children.size() == 1)
      return new SelectorAttribute(token, children.get(0).getText());

    if (children.size() < 3)
      throw new IncorrectTreeException();

    return new SelectorAttribute(token, children.get(0).getText(), postprocess(handleSelectorOperator(children.get(1))), children.get(2).getText());
  }

  public SelectorOperator handleSelectorOperator(HiddenTokenAwareTree token) {
    return new SelectorOperator(token, toSelectorOperator(token));
  }

  private SelectorOperator.Operator toSelectorOperator(HiddenTokenAwareTree token) {
    switch (token.getType()) {
    case LessLexer.OPEQ:
      return SelectorOperator.Operator.EQUALS;

    case LessLexer.INCLUDES:
      return SelectorOperator.Operator.INCLUDES;

    case LessLexer.DASHMATCH:
      return SelectorOperator.Operator.SPECIAL_PREFIX;

    case LessLexer.PREFIXMATCH:
      return SelectorOperator.Operator.PREFIXMATCH;

    case LessLexer.SUFFIXMATCH:
      return SelectorOperator.Operator.SUFFIXMATCH;

    case LessLexer.SUBSTRINGMATCH:
      return SelectorOperator.Operator.SUBSTRINGMATCH;

    default:
      break;
    }

    throw new IncorrectTreeException();
  }

  public Pseudo handlePseudo(HiddenTokenAwareTree token) {
    List<HiddenTokenAwareTree> children = token.getChildren();
    if (children.size() == 0)
      throw new IncorrectTreeException();

    if (children.size() == 1)
      return new Pseudo(token, children.get(0).getText());

    if (children.size() == 2)
      return new Pseudo(token, children.get(0).getText(), children.get(1).getText());

    // FIXME: handle this case the same way as less.js
    throw new IncorrectTreeException();
  }

  public IdSelector handleIdSelector(HiddenTokenAwareTree token) {
    List<HiddenTokenAwareTree> children = token.getChildren();
    if (children.size() != 1)
      throw new IncorrectTreeException();

    String text = children.get(0).getText();
    if (text == null || text.length() < 1)
      throw new IncorrectTreeException();

    return new IdSelector(token, text.substring(1));
  }

  public Media handleMedia(HiddenTokenAwareTree token) {
    List<HiddenTokenAwareTree> children = token.getChildren();
    Media result = new Media(token);
    for (HiddenTokenAwareTree kid : children) {
      result.addChild(switchOn(kid));

    }
    return result;
  }

  public Medium handleMediumDeclaration(HiddenTokenAwareTree token) {
    Medium result = new Medium(token);
    List<HiddenTokenAwareTree> children = token.getChildren();
    for (HiddenTokenAwareTree kid : children) {
      result.addMedium(kid.getText());
    }

    return result;
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