package com.github.sommeri.less4j.core.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.runtime.CommonToken;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ArgumentDeclaration;
import com.github.sommeri.less4j.core.ast.CharsetDeclaration;
import com.github.sommeri.less4j.core.ast.ComparisonExpression;
import com.github.sommeri.less4j.core.ast.ComparisonExpressionOperator;
import com.github.sommeri.less4j.core.ast.ComposedExpression;
import com.github.sommeri.less4j.core.ast.CssClass;
import com.github.sommeri.less4j.core.ast.Declaration;
import com.github.sommeri.less4j.core.ast.Document;
import com.github.sommeri.less4j.core.ast.ElementSubsequent;
import com.github.sommeri.less4j.core.ast.EscapedSelector;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.ExpressionOperator;
import com.github.sommeri.less4j.core.ast.FixedMediaExpression;
import com.github.sommeri.less4j.core.ast.FixedNamePart;
import com.github.sommeri.less4j.core.ast.FontFace;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.Guard;
import com.github.sommeri.less4j.core.ast.GuardCondition;
import com.github.sommeri.less4j.core.ast.IdSelector;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.Import;
import com.github.sommeri.less4j.core.ast.IndirectVariable;
import com.github.sommeri.less4j.core.ast.InterpolableName;
import com.github.sommeri.less4j.core.ast.InterpolatedMediaExpression;
import com.github.sommeri.less4j.core.ast.Keyframes;
import com.github.sommeri.less4j.core.ast.KeyframesName;
import com.github.sommeri.less4j.core.ast.Media;
import com.github.sommeri.less4j.core.ast.MediaExpressionFeature;
import com.github.sommeri.less4j.core.ast.MediaQuery;
import com.github.sommeri.less4j.core.ast.Medium;
import com.github.sommeri.less4j.core.ast.MediumModifier;
import com.github.sommeri.less4j.core.ast.MediumType;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.Name;
import com.github.sommeri.less4j.core.ast.NamedExpression;
import com.github.sommeri.less4j.core.ast.SupportsConditionNegation;
import com.github.sommeri.less4j.core.ast.NestedSelectorAppender;
import com.github.sommeri.less4j.core.ast.Nth;
import com.github.sommeri.less4j.core.ast.Nth.Form;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.Page;
import com.github.sommeri.less4j.core.ast.PageMarginBox;
import com.github.sommeri.less4j.core.ast.SupportsConditionInParentheses;
import com.github.sommeri.less4j.core.ast.Pseudo;
import com.github.sommeri.less4j.core.ast.PseudoClass;
import com.github.sommeri.less4j.core.ast.PseudoElement;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.ReusableStructureName;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.ast.SelectorAttribute;
import com.github.sommeri.less4j.core.ast.SelectorOperator;
import com.github.sommeri.less4j.core.ast.SignedExpression;
import com.github.sommeri.less4j.core.ast.SimpleSelector;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.ast.Supports;
import com.github.sommeri.less4j.core.ast.SupportsCondition;
import com.github.sommeri.less4j.core.ast.SupportsLogicalCondition;
import com.github.sommeri.less4j.core.ast.SupportsLogicalOperator;
import com.github.sommeri.less4j.core.ast.SupportsLogicalOperator.Operator;
import com.github.sommeri.less4j.core.ast.SupportsQuery;
import com.github.sommeri.less4j.core.ast.SyntaxOnlyElement;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.ast.VariableDeclaration;
import com.github.sommeri.less4j.core.ast.VariableNamePart;
import com.github.sommeri.less4j.core.ast.Viewport;
import com.github.sommeri.less4j.core.problems.BugHappened;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

//FIXME: better error message for required (...)+ loop did not match anything at input errors
//FIXME: better error message missing EOF at blahblah
class ASTBuilderSwitch extends TokenTypeSwitch<ASTCssNode> {

  private static final String GRAMMAR_MISMATCH = "ASTBuilderSwitch grammar mismatch";
  private final ProblemsHandler problemsHandler;
  private final MixinsParametersBuilder mixinsParametersBuilder;
  private final TermBuilder termBuilder;
  // as stated here: http://www.w3.org/TR/css3-selectors/#pseudo-elements
  private static Set<String> COLONLESS_PSEUDOELEMENTS = new HashSet<String>();
  static {
    COLONLESS_PSEUDOELEMENTS.add("first-line");
    COLONLESS_PSEUDOELEMENTS.add("first-letter");
    COLONLESS_PSEUDOELEMENTS.add("before");
    COLONLESS_PSEUDOELEMENTS.add("after");
  }

  public ASTBuilderSwitch(ProblemsHandler problemsHandler) {
    super();
    this.problemsHandler = problemsHandler;
    termBuilder = new TermBuilder(this, problemsHandler);
    mixinsParametersBuilder = new MixinsParametersBuilder(this, problemsHandler);
  }

  public StyleSheet handleStyleSheet(HiddenTokenAwareTree token) {
    StyleSheet result = new StyleSheet(token);
    if (token.getChildren() == null || token.getChildren().isEmpty())
      return result;

    for (HiddenTokenAwareTree kid : token.getChildren()) {
      result.addMember(switchOn(kid));
    }

    return result;
  }

  public Expression handleTerm(HiddenTokenAwareTree token) {
    return termBuilder.buildFromTerm(token);
  }

  public Expression handleExpression(HiddenTokenAwareTree token) {
    LinkedList<HiddenTokenAwareTree> children = new LinkedList<HiddenTokenAwareTree>(token.getChildren());
    if (children.size() == 0)
      throw new BugHappened(GRAMMAR_MISMATCH, token);

    if (children.size() == 1) {
      Expression head = (Expression) switchOn(children.get(0));
      // we have to switch to parent token, because we would loose it otherwise.
      // for example, comments before simple expressions would not be accessible
      // anymore
      head.setUnderlyingStructure(token);
      return head;
    }

    return createExpression(token, children);
  }

  private Expression createExpression(HiddenTokenAwareTree parent, LinkedList<HiddenTokenAwareTree> members) {
    // this must represent a term. Otherwise we are doomed anyway.
    Expression head = (Expression) switchOn(members.removeFirst());
    while (!members.isEmpty()) {
      ExpressionOperator operator = readExpressionOperator(members);
      if (members.isEmpty())
        return new ComposedExpression(parent, head, operator, null);

      Expression next = (Expression) switchOn(members.removeFirst());
      head = new ComposedExpression(parent, head, operator, next);
    }
    return head;
  }

  public ExpressionOperator readExpressionOperator(LinkedList<HiddenTokenAwareTree> members) {
    HiddenTokenAwareTree token = members.removeFirst();
    ExpressionOperator operator = new ExpressionOperator(token, toExpressionOperator(token));
    return operator;
  }

  private ExpressionOperator.Operator toExpressionOperator(HiddenTokenAwareTree token) {
    switch (token.getType()) {
    case LessLexer.SOLIDUS:
      return ExpressionOperator.Operator.SOLIDUS;

    case LessLexer.COMMA:
      return ExpressionOperator.Operator.COMMA;

    case LessLexer.STAR:
      return ExpressionOperator.Operator.STAR;

    case LessLexer.MINUS:
      return ExpressionOperator.Operator.MINUS;

    case LessLexer.PLUS:
      return ExpressionOperator.Operator.PLUS;

    case LessLexer.EMPTY_SEPARATOR:
      return ExpressionOperator.Operator.EMPTY_OPERATOR;

    default:
      break;
    }

    throw new BugHappened(GRAMMAR_MISMATCH, token);
  }

  public Variable handleVariable(HiddenTokenAwareTree token) {
    return termBuilder.buildFromVariable(token);
  }

  public IndirectVariable handleIndirectVariable(HiddenTokenAwareTree token) {
    return termBuilder.buildFromIndirectVariable(token);
  }

  public Declaration handleDeclaration(HiddenTokenAwareTree token) {
    Iterator<HiddenTokenAwareTree> iterator = token.getChildren().iterator();
    HiddenTokenAwareTree nameToken = iterator.next();

    String name = nameToken.getText();
    if (nameToken.getType() == LessLexer.STAR) {
      // handling star prefix browser hack
      nameToken = iterator.next();
      name += nameToken.getText();
    }
    if (!iterator.hasNext())
      return new Declaration(token, name);

    HiddenTokenAwareTree expressionToken = iterator.next();
    if (expressionToken.getType() == LessLexer.IMPORTANT_SYM)
      return new Declaration(token, name, null, true);

    Expression expression = (Expression) switchOn(expressionToken);
    if (!iterator.hasNext())
      return new Declaration(token, name, expression);

    HiddenTokenAwareTree importantToken = iterator.next();
    if (importantToken.getType() == LessLexer.IMPORTANT_SYM)
      return new Declaration(token, name, expression, true);

    throw new BugHappened(GRAMMAR_MISMATCH, token);
  }

  public FontFace handleFontFace(HiddenTokenAwareTree token) {
    FontFace result = new FontFace(token);

    List<HiddenTokenAwareTree> children = token.getChildren();
    result.setBody(handleGeneralBody(children.get(0)));
    return result;
  }

  public CharsetDeclaration handleCharsetDeclaration(HiddenTokenAwareTree token) {
    List<HiddenTokenAwareTree> children = token.getChildren();
    if (children.isEmpty())
      throw new BugHappened(GRAMMAR_MISMATCH, token);

    return new CharsetDeclaration(token, children.get(0).getText());
  }

  public RuleSet handleRuleSet(HiddenTokenAwareTree token) {
    RuleSet ruleSet = new RuleSet(token);

    List<Selector> selectors = new ArrayList<Selector>();
    List<HiddenTokenAwareTree> children = token.getChildren();

    ASTCssNode previousKid = null;
    for (HiddenTokenAwareTree kid : children) {
      if (kid.getType() == LessLexer.SELECTOR) {
        Selector selector = handleSelector(kid);
        if (selector != null)
          selectors.add(selector);
        previousKid = selector;
      } else if (kid.getType() == LessLexer.BODY) {
        GeneralBody body = handleGeneralBody(kid);
        ruleSet.setBody(body);
        previousKid = body;
      } else if (kid.getType() == LessLexer.COMMA) {
        if (previousKid != null)
          previousKid.getUnderlyingStructure().addFollowing(kid.getPreceding());
      }
    }

    ruleSet.addSelectors(selectors);
    return ruleSet;
  }

  public ReusableStructure handleReusableStructureDeclaration(HiddenTokenAwareTree token) {
    ReusableStructure result = new ReusableStructure(token);

    List<HiddenTokenAwareTree> children = token.getChildren();
    for (HiddenTokenAwareTree kid : children) {
      if (kid.getType() == LessLexer.REUSABLE_STRUCTURE_NAME) {
        result.addName(handleReusableStructureName(kid));
      } else if (kid.getType() == LessLexer.BODY) {
        result.setBody(handleGeneralBody(kid));
      } else if (kid.getType() == LessLexer.GUARD) {
        result.addGuard(handleGuard(kid));
      } else if (kid.getType() == LessLexer.SEMI_SPLIT_MIXIN_DECLARATION_ARGUMENTS) {
        mixinsParametersBuilder.handleMixinDeclarationArguments(kid, result);
      }
    }

    return result;
  }

  public ElementSubsequent handleElementSubsequent(HiddenTokenAwareTree token) {
    return (ElementSubsequent) switchOn(token.getChild(0));
  }

  public ReusableStructureName handleReusableStructureName(HiddenTokenAwareTree token) {
    ReusableStructureName result = new ReusableStructureName(token);
    List<HiddenTokenAwareTree> children = token.getChildren();
    for (HiddenTokenAwareTree kid : children) {
      result.addNamePart(handleElementSubsequent(kid));
    }
    return result;
  }

  public MixinReference handleMixinReference(HiddenTokenAwareTree token) {
    MixinReference result = new MixinReference(token);
    List<HiddenTokenAwareTree> children = token.getChildren();
    for (HiddenTokenAwareTree kid : children) {
      if (kid.getType() == LessLexer.REUSABLE_STRUCTURE_NAME) {
        result.setFinalName(handleReusableStructureName(kid));
      } else if (kid.getType() == LessLexer.IMPORTANT_SYM) {
        result.setImportant(true);
      } else if (kid.getType() == LessLexer.SEMI_SPLIT_MIXIN_REFERENCE_ARGUMENTS) {
        mixinsParametersBuilder.handleMixinReferenceArguments(kid, result);
      }
    }

    return result;
  }

  public MixinReference handleNamespaceReference(HiddenTokenAwareTree token) {
    MixinReference reference = null;
    List<ReusableStructureName> nameChain = new ArrayList<ReusableStructureName>();

    List<HiddenTokenAwareTree> children = token.getChildren();
    for (HiddenTokenAwareTree kid : children) {
      ASTCssNode buildKid = switchOn(kid);
      if (buildKid.getType() == ASTCssNodeType.MIXIN_REFERENCE) {
        reference = (MixinReference) switchOn(kid);
      } else if (buildKid.getType() == ASTCssNodeType.REUSABLE_STRUCTURE_NAME) {
        nameChain.add(handleReusableStructureName(kid));
      } else {
        throw new BugHappened(GRAMMAR_MISMATCH, token);
      }
    }

    reference.setUnderlyingStructure(token);
    reference.addNames(nameChain);
    return reference;
  }

  public Expression handleMixinPattern(HiddenTokenAwareTree token) {
    return termBuilder.buildFromTerm(token.getChild(0));
  }

  public Guard handleGuard(HiddenTokenAwareTree token) {
    Guard result = new Guard(token);
    Iterator<HiddenTokenAwareTree> iterator = token.getChildren().iterator();
    result.addCondition(handleGuardCondition(iterator.next()));
    while (iterator.hasNext()) {
      validateGuardAnd(iterator.next());
      result.addCondition(handleGuardCondition(iterator.next()));
    }

    return result;
  }

  public GuardCondition handleGuardCondition(HiddenTokenAwareTree token) {
    Iterator<HiddenTokenAwareTree> iterator = token.getChildren().iterator();
    HiddenTokenAwareTree kid = iterator.next();

    boolean isNegated = false;
    if (kid.getType() != LessLexer.EXPRESSION) {
      validateGuardNegation(kid);
      isNegated = true;
      kid = iterator.next();
    }

    Expression condition = handleExpression(kid);
    if (iterator.hasNext()) {
      HiddenTokenAwareTree operatorToken = iterator.next();
      Expression followingExpression = handleExpression(iterator.next());
      condition = new ComparisonExpression(token, condition, toComparisonOperator(operatorToken), followingExpression);
    }

    return new GuardCondition(token, isNegated, condition);
  }

  private ComparisonExpressionOperator toComparisonOperator(HiddenTokenAwareTree token) {
    switch (token.getType()) {
    case LessLexer.GREATER:
      return new ComparisonExpressionOperator(token, ComparisonExpressionOperator.Operator.GREATER);

    case LessLexer.GREATER_OR_EQUAL:
      return new ComparisonExpressionOperator(token, ComparisonExpressionOperator.Operator.GREATER_OR_EQUAL);

    case LessLexer.OPEQ:
      return new ComparisonExpressionOperator(token, ComparisonExpressionOperator.Operator.OPEQ);

    case LessLexer.LOWER_OR_EQUAL:
      return new ComparisonExpressionOperator(token, ComparisonExpressionOperator.Operator.LOWER_OR_EQUAL);

    case LessLexer.LOWER:
      return new ComparisonExpressionOperator(token, ComparisonExpressionOperator.Operator.LOWER);

    default:
      break;
    }

    throw new BugHappened(GRAMMAR_MISMATCH, token);
  }

  public void validateGuardNegation(HiddenTokenAwareTree token) {
    String operator = token.getText().trim();
    if (!"not".equals(operator))
      throw new BugHappened(GRAMMAR_MISMATCH, token);
  }

  public void validateGuardAnd(HiddenTokenAwareTree token) {
    String operator = token.getText().trim();
    if (!"and".equals(operator))
      throw new BugHappened(GRAMMAR_MISMATCH, token);
  }

  public GeneralBody handleGeneralBody(HiddenTokenAwareTree token) {
    return createGeneralBody(token);
  }

  private List<ASTCssNode> handleBodyMembers(HiddenTokenAwareTree token) {
    if (token.getChildren() == null)
      return new ArrayList<ASTCssNode>();

    List<ASTCssNode> members = new ArrayList<ASTCssNode>();
    Iterator<HiddenTokenAwareTree> iterator = token.getChildren().iterator();

    while (iterator.hasNext()) {
      members.add(switchOn(iterator.next()));
    }
    return members;
  }

  public SyntaxOnlyElement handleLbrace(HiddenTokenAwareTree token) {
    return toSyntaxOnlyElement(token);
  }

  public SyntaxOnlyElement handleRbrace(HiddenTokenAwareTree token) {
    return toSyntaxOnlyElement(token);
  }

  public Selector handleSelector(HiddenTokenAwareTree token) {
    SelectorBuilder builder = new SelectorBuilder(token, this);
    return builder.buildSelector();
  }

  public CssClass handleCssClass(HiddenTokenAwareTree token) {
    List<HiddenTokenAwareTree> children = token.getChildren();
    return new CssClass(token, toInterpolableName(token, children));
    // HiddenTokenAwareTree nameToken = children.get(0);
    //
    // String name = nameToken.getText();
    // if (nameToken.getType() != LessLexer.IDENT && name.length() > 1) {
    // name = name.substring(1, name.length());
    // }
    //
    // CssClass result = new CssClass(token, name);
    // return result;
  }

  public SelectorAttribute handleSelectorAttribute(HiddenTokenAwareTree token) {
    List<HiddenTokenAwareTree> children = token.getChildren();
    if (children.size() == 0)
      throw new BugHappened(GRAMMAR_MISMATCH, token);

    if (children.size() == 1)
      return new SelectorAttribute(token, children.get(0).getText());

    if (children.size() < 3)
      throw new BugHappened(GRAMMAR_MISMATCH, token);

    return new SelectorAttribute(token, children.get(0).getText(), handleSelectorOperator(children.get(1)), children.get(2).getText());
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

    throw new BugHappened(GRAMMAR_MISMATCH, token);
  }

  public Pseudo handlePseudo(HiddenTokenAwareTree token) {
    List<HiddenTokenAwareTree> children = token.getChildren();
    if (children.size() == 0 || children.size() == 1)
      throw new BugHappened(GRAMMAR_MISMATCH, token);

    // the child number 0 is a :
    HiddenTokenAwareTree t = children.get(1);
    if (t.getType() == LessLexer.COLON) {
      return createPseudoElement(token, 2, false);
    }

    if (COLONLESS_PSEUDOELEMENTS.contains(t.getText().toLowerCase())) {
      return createPseudoElement(token, 1, true);
    }

    if (children.size() == 2)
      return new PseudoClass(token, children.get(1).getText());

    if (children.size() == 3) {
      HiddenTokenAwareTree parameter = children.get(2);
      if (parameter.getType() == LessLexer.NUMBER)
        return new PseudoClass(token, children.get(1).getText(), new NumberExpression(parameter, parameter.getText()));
      if (parameter.getType() == LessLexer.IDENT)
        return new PseudoClass(token, children.get(1).getText(), new IdentifierExpression(parameter, parameter.getText()));

      return new PseudoClass(token, children.get(1).getText(), switchOn(parameter));
    }

    throw new BugHappened(GRAMMAR_MISMATCH, token);
  }

  public Nth handleNth(HiddenTokenAwareTree token) {
    Expression first = null;
    Expression second = null;
    if (hasChildren(token.getChild(0))) {
      first = termBuilder.buildFromTerm(token.getChild(0));
      String sign = "";
      if (first.getType() == ASTCssNodeType.SIGNED_EXPRESSION) {
        SignedExpression negated = (SignedExpression) first;
        first = negated.getExpression();
        sign = negated.getSign().toSymbol();
      }
      if (first.getType() == ASTCssNodeType.IDENTIFIER_EXPRESSION) {
        IdentifierExpression ident = (IdentifierExpression) first;
        String lowerCaseValue = ident.getValue().toLowerCase();
        lowerCaseValue = sign + lowerCaseValue;
        if ("even".equals(lowerCaseValue)) {
          return new Nth(token, null, null, Form.EVEN);
        } else if ("odd".equals(lowerCaseValue)) {
          return new Nth(token, null, null, Form.ODD);
        } else if ("n".equals(lowerCaseValue) || "-n".equals(lowerCaseValue) || "+n".equals(lowerCaseValue)) {
          first = new NumberExpression(token.getChild(0), lowerCaseValue, NumberExpression.Dimension.REPEATER);
        } else
          throw new IllegalStateException("Unexpected identifier value for nth: " + ident.getValue());
      }
    }

    if (token.getChild(1) != null && hasChildren(token.getChild(1))) {
      second = termBuilder.buildFromTerm(token.getChild(1));
    }

    return new Nth(token, (NumberExpression) first, (NumberExpression) second);
  }

  private boolean hasChildren(HiddenTokenAwareTree token) {
    return token.getChildren() != null && !token.getChildren().isEmpty();
  }

  private PseudoElement createPseudoElement(HiddenTokenAwareTree token, int startIndex, boolean level12Form) {
    List<HiddenTokenAwareTree> children = token.getChildren();
    String name = children.get(startIndex).getText();
    return new PseudoElement(token, name, level12Form);
  }

  public IdSelector handleIdSelector(HiddenTokenAwareTree token) {
    List<HiddenTokenAwareTree> children = token.getChildren();
    return new IdSelector(token, toInterpolableName(token, children));
  }

  private InterpolableName toInterpolableName(HiddenTokenAwareTree token, List<HiddenTokenAwareTree> children) {
    InterpolableName result = new InterpolableName(token);
    for (HiddenTokenAwareTree kid : children) {
      String text = kid.getText();
      if (text == null || text.length() < 1)
        throw new BugHappened(GRAMMAR_MISMATCH, kid);

      if (kid.getType() == LessLexer.INTERPOLATED_VARIABLE) {
        result.add(new VariableNamePart(kid, new Variable(kid, "@" + text.substring(2, text.length() - 1))));
      } else if (kid.getType() == LessLexer.HASH_SYMBOL) {
        // do nothing
      } else {
        result.add(new FixedNamePart(kid, toFixedName(kid.getType(), text)));
      }
    }
    return result;
  }

  private String toFixedName(int typeCode, String text) {
    if (typeCode == LessLexer.HASH)
      return text.substring(1);

    return text;
  }

  public Media handleMedia(HiddenTokenAwareTree token) {
    Iterator<HiddenTokenAwareTree> children = token.getChildren().iterator();

    Media result = new Media(token);
    handleMediaDeclaration(result, children.next());
    result.setBody(handleGeneralBody(children.next()));

    return result;
  }

  private void handleMediaDeclaration(Media result, HiddenTokenAwareTree declaration) {
    declaration.pushHiddenToKids();
    List<HiddenTokenAwareTree> children = declaration.getChildren();

    MediaQuery previousKid = null;
    for (HiddenTokenAwareTree kid : children) {
      if (kid.getType() == LessLexer.COMMA) {
        previousKid.getUnderlyingStructure().addFollowing(kid.getPreceding());
      } else {
        previousKid = handleMediaQuery(kid);
        result.addMediaQuery(previousKid);
      }
    }
  }

  public MediaQuery handleMediaQuery(HiddenTokenAwareTree token) {
    MediaQuery result = new MediaQuery(token);
    List<HiddenTokenAwareTree> originalChildren = token.getChildren();

    // each AND identifier may hold preceding comments must be pushed to other
    // tokens
    LinkedList<HiddenTokenAwareTree> children = new LinkedList<HiddenTokenAwareTree>();
    for (HiddenTokenAwareTree kid : originalChildren) {
      if (kid.getType() == LessLexer.IDENT) {
        HiddenTokenAwareTree lastKid = children.peekLast();
        if (lastKid != null) {
          lastKid.addFollowing(kid.getPreceding());
        }
      } else {
        children.add(kid);
      }
    }
    // we have three types of children:
    // * MEDIUM_TYPE
    // * identifier AND whose only function is to hold comments
    // * FIXED_MEDIA_EXPRESSION
    for (HiddenTokenAwareTree kid : children) {
      if (kid.getType() != LessLexer.IDENT) {
        result.addMember(switchOn(kid));
      } else {
        // we have to copy comments from the AND identifier to surrounding
        // elements.
      }
    }
    return result;
  }

  public Medium handleMedium(HiddenTokenAwareTree token) {
    List<HiddenTokenAwareTree> children = token.getChildren();
    if (children.size() == 1) {
      HiddenTokenAwareTree type = children.get(0);
      return new Medium(token, new MediumModifier(type), new MediumType(type, type.getText()));
    }

    HiddenTokenAwareTree type = children.get(1);
    return new Medium(token, toMediumModifier(children.get(0)), new MediumType(type, type.getText()));
  }

  public FixedMediaExpression handleMediaExpression(HiddenTokenAwareTree token) {
    List<HiddenTokenAwareTree> children = token.getChildren();
    HiddenTokenAwareTree featureNode = children.get(0);
    if (children.size() == 1)
      return new FixedMediaExpression(token, new MediaExpressionFeature(featureNode, featureNode.getText()), null);

    if (children.size() == 2)
      throw new BugHappened(GRAMMAR_MISMATCH, token);

    HiddenTokenAwareTree colonNode = children.get(1);
    featureNode.addFollowing(colonNode.getPreceding());

    HiddenTokenAwareTree expressionNode = children.get(2);
    Expression expression = (Expression) switchOn(expressionNode);
    return new FixedMediaExpression(token, new MediaExpressionFeature(featureNode, featureNode.getText()), expression);
  }

  public InterpolatedMediaExpression handleInterpolatedMediaExpression(HiddenTokenAwareTree token) {
    return new InterpolatedMediaExpression(token, (Variable) switchOn(token.getChild(0)));
  }

  private MediumModifier toMediumModifier(HiddenTokenAwareTree token) {
    String modifier = token.getText().toLowerCase();

    if ("not".equals(modifier))
      return new MediumModifier(token, MediumModifier.Modifier.NOT);

    if ("only".equals(modifier))
      return new MediumModifier(token, MediumModifier.Modifier.ONLY);

    throw new IllegalStateException("Unexpected medium modifier: " + modifier);
  }

  // this handles both variable declaration and a mixin parameter with default
  // value
  public VariableDeclaration handleVariableDeclaration(HiddenTokenAwareTree token) {
    List<HiddenTokenAwareTree> children = token.getChildren();
    HiddenTokenAwareTree name = children.get(0);
    HiddenTokenAwareTree colon = children.get(1);
    HiddenTokenAwareTree expression = children.get(2);
    if (children.size() > 3) {
      HiddenTokenAwareTree semi = children.get(3);
      colon.giveHidden(name, expression);
      semi.giveHidden(expression, null);
      token.addBeforeFollowing(semi.getFollowing());
    }

    return new VariableDeclaration(token, new Variable(name, name.getText()), (Expression) switchOn(expression));
  }

  // FIXME: fail on wrong distribution of arguments (e.g. collector must be
  // last, those with default must be first)
  public ArgumentDeclaration handleArgumentDeclaration(HiddenTokenAwareTree token) {
    List<HiddenTokenAwareTree> children = token.getChildren();
    HiddenTokenAwareTree firstChild = children.get(0);
    if (firstChild.getType() == LessLexer.DOT3)
      return new ArgumentDeclaration(firstChild, new Variable(firstChild, "@"), null, true);

    HiddenTokenAwareTree name = firstChild;

    if (children.size() == 1)
      return new ArgumentDeclaration(token, new Variable(name, name.getText()), null);

    HiddenTokenAwareTree separator = children.get(1);
    if (separator.getType() == LessLexer.DOT3) {
      return new ArgumentDeclaration(token, new Variable(name, name.getText()), null, true);
    }
    HiddenTokenAwareTree expression = children.get(2);
    separator.giveHidden(name, expression);

    return new ArgumentDeclaration(token, new Variable(name, name.getText()), (Expression) switchOn(expression));
  }

  @Override
  public ASTCssNode handleNestedAppender(HiddenTokenAwareTree token) {
    boolean directlyBefore = true;
    boolean directlyAfter = true;
    if (token.getChildren().size() == 2) {
      directlyBefore = isMeaningfullWhitespace(token);
      directlyAfter = !directlyBefore;
    } else if (token.getChildren().size() == 3) {
      directlyBefore = false;
      directlyAfter = false;
    }

    return new NestedSelectorAppender(token, directlyBefore, directlyAfter);
  }

  public ASTCssNode handleSimpleSelector(HiddenTokenAwareTree token) {
    SimpleSelector result = null;
    token.pushHiddenToKids();
    Iterator<HiddenTokenAwareTree> iterator = token.getChildren().iterator();
    HiddenTokenAwareTree kid = iterator.next();
    if (kid.getType() == LessLexer.ELEMENT_NAME) {
      List<HiddenTokenAwareTree> elementNameParts = kid.getChildren();
      InterpolableName interpolableName = toInterpolableName(kid, elementNameParts);
      result = new SimpleSelector(kid, interpolableName, isStarElementName(elementNameParts));
      if (iterator.hasNext())
        kid = iterator.next();
      else
        return result;
    } else {
      result = new SimpleSelector(kid, null, true);
      result.setEmptyForm(true);
    }

    do {
      result.addSubsequent((ElementSubsequent) switchOn(kid));

      if (iterator.hasNext())
        kid = iterator.next();
      else
        kid = null;
    } while (kid != null);

    return result;
  }

  private boolean isStarElementName(List<HiddenTokenAwareTree> elementNameParts) {
    if (elementNameParts.size() != 1)
      return false;

    return elementNameParts.get(0).getType() == LessLexer.STAR;
  }

  public EscapedSelector handleEscapedSelector(HiddenTokenAwareTree token) {
    token.pushHiddenToKids();
    HiddenTokenAwareTree valueToken = token.getChild(0);
    String quotedText = valueToken.getText();
    return new EscapedSelector(valueToken, quotedText.substring(2, quotedText.length() - 1), "" + quotedText.charAt(1));
  }

  private boolean isMeaningfullWhitespace(HiddenTokenAwareTree kid) {
    int type = kid.getChild(0).getType();
    return type == LessLexer.MEANINGFULL_WHITESPACE || type == LessLexer.DUMMY_MEANINGFULL_WHITESPACE;
  }

  @Override
  public Keyframes handleKeyframes(HiddenTokenAwareTree token) {
    Iterator<HiddenTokenAwareTree> children = token.getChildren().iterator();
    Keyframes result = new Keyframes(token, children.next().getText());
    result.addNames(handleKeyframesDeclaration(children.next()));
    result.setBody(handleGeneralBody(children.next()));

    return result;
  }

  @Override
  public Supports handleSupports(HiddenTokenAwareTree token) {
    Iterator<HiddenTokenAwareTree> children = token.getChildren().iterator();
    Supports result = new Supports(token, children.next().getText());
    result.setCondition((SupportsCondition) switchOn(children.next()));
    result.setBody(handleGeneralBody(children.next()));

    return result;
  }

  
  public SupportsCondition handleSupportsCondition(HiddenTokenAwareTree token) {
    Iterator<HiddenTokenAwareTree> children = token.getChildren().iterator();
    if (token.getChildCount() == 1)
      return (SupportsCondition) switchOn(children.next());
    
    SupportsLogicalCondition result = new SupportsLogicalCondition(token, (SupportsCondition) switchOn(children.next()));
    while (children.hasNext()) {
      SupportsLogicalOperator logicalOperator =  toSupportsLogicalOperator(children.next());
      if (!children.hasNext())
        throw new BugHappened(GRAMMAR_MISMATCH, token);
      SupportsCondition condition = (SupportsCondition) switchOn(children.next());
      result.addCondition(logicalOperator, condition);
    }
    return result;
  }

  private SupportsLogicalOperator toSupportsLogicalOperator(HiddenTokenAwareTree token) {
    String text = token.getText();
    Map<String, Operator> operatorsBySymbol = SupportsLogicalOperator.Operator.getSymbolsMap();
    if (text==null || !operatorsBySymbol.containsKey(text.toLowerCase())) {
      SupportsLogicalOperator result = new SupportsLogicalOperator(token, null);
      problemsHandler.errWrongSupportsLogicalOperator(result, token.getText());
      return result;
    }

    SupportsLogicalOperator result = new SupportsLogicalOperator(token, operatorsBySymbol.get(text.toLowerCase()));
    return result;
  }

  public SupportsCondition handleSupportsSimpleCondition(HiddenTokenAwareTree token) {
    Iterator<HiddenTokenAwareTree> children = token.getChildren().iterator();
    HiddenTokenAwareTree first = children.next();
    if (first.getType()==LessLexer.LPAREN) {
      SyntaxOnlyElement openingParentheses = toSyntaxOnlyElement(first);
      SupportsCondition condition = (SupportsCondition)switchOn(children.next());
      SyntaxOnlyElement closingParentheses = toSyntaxOnlyElement(children.next());
      
      SupportsConditionInParentheses result = new SupportsConditionInParentheses(token, openingParentheses, condition, closingParentheses);
      return result;
    } else if (first.getType()==LessLexer.IDENT) {
      //TODO: warning on wrong operator (anything that is not 'not')
      SyntaxOnlyElement negation = toSyntaxOnlyElement(first);
      SupportsCondition condition = (SupportsCondition)switchOn(children.next());

      SupportsConditionNegation result = new SupportsConditionNegation(token, negation, condition);
      return result;
    } 
    
    return (SupportsCondition) switchOn(first);

  }
  
  public SupportsCondition handleSupportsQuery(HiddenTokenAwareTree token) {
    Iterator<HiddenTokenAwareTree> children = token.getChildren().iterator();
    SyntaxOnlyElement openingParentheses = toSyntaxOnlyElement(children.next());
    Declaration declaration = (Declaration)switchOn(children.next());
    SyntaxOnlyElement closingParentheses = toSyntaxOnlyElement(children.next());
    SupportsQuery result = new SupportsQuery(token, openingParentheses, closingParentheses, declaration);
    return result;
  }

  private SyntaxOnlyElement toSyntaxOnlyElement(HiddenTokenAwareTree token) {
    return new SyntaxOnlyElement(token, token.getText());
  }

  @Override
  public Document handleDocument(HiddenTokenAwareTree token) {
    Iterator<HiddenTokenAwareTree> children = token.getChildren().iterator();
    Document result = new Document(token, children.next().getText());
    result.addUrlMatchFunctions(handleUrlMatchFunctionsDeclaration(children.next()));
    result.setBody(handleGeneralBody(children.next()));

    return result;
  }

  private List<FunctionExpression> handleUrlMatchFunctionsDeclaration(HiddenTokenAwareTree declaration) {
    List<FunctionExpression> result = new ArrayList<FunctionExpression>();
    Iterator<HiddenTokenAwareTree> iterator = declaration.getChildren().iterator();
    while (iterator.hasNext()) {
      HiddenTokenAwareTree token = iterator.next();
      if (token.getType() == LessLexer.COMMA) {
        token.pushHiddenToSiblings();
      } else {
        ASTCssNode urlMatchFunction = switchOn(token);
        if (urlMatchFunction.getType() == ASTCssNodeType.FUNCTION)
          result.add((FunctionExpression) termBuilder.buildFromTerm(token));
        else
          throw new BugHappened(GRAMMAR_MISMATCH, token);
      }

    }

    return result;
  }

  @Override
  public Viewport handleViewport(HiddenTokenAwareTree token) {
    Viewport result = new Viewport(token);
    // HiddenTokenAwareTree atName = token.getChild(0);
    HiddenTokenAwareTree body = token.getChild(1);
    result.setBody(createGeneralBody(body));
    return result;
  }

  private GeneralBody createGeneralBody(HiddenTokenAwareTree token) {
    List<ASTCssNode> list = handleBodyMembers(token);

    if (list.size() < 2)
      throw new BugHappened(GRAMMAR_MISMATCH, token);

    SyntaxOnlyElement lbrace = (SyntaxOnlyElement) list.remove(0);
    SyntaxOnlyElement rbrace = (SyntaxOnlyElement) list.remove(list.size() - 1);
    List<CommonToken> orphansOrFollowLastMember = rbrace.getUnderlyingStructure().chopPreceedingUpToLastOfType(LessLexer.NEW_LINE);
    if (list.isEmpty()) {
      token.addOrphans(orphansOrFollowLastMember);
    } else {
      list.get(list.size() - 1).getUnderlyingStructure().addFollowing(orphansOrFollowLastMember);
    }
    return new GeneralBody(token, lbrace, rbrace, list);
  }

  private List<KeyframesName> handleKeyframesDeclaration(HiddenTokenAwareTree declaration) {
    List<KeyframesName> result = new ArrayList<KeyframesName>();
    Iterator<HiddenTokenAwareTree> iterator = declaration.getChildren().iterator();
    while (iterator.hasNext()) {
      HiddenTokenAwareTree token = iterator.next();
      if (token.getType() == LessLexer.COMMA) {
        token.pushHiddenToSiblings();
      } else if (token.getType() == LessLexer.IDENT) {
        result.add(new KeyframesName(token, token.getText()));
      } else {
        throw new BugHappened(GRAMMAR_MISMATCH, token);
      }

    }

    return result;
  }

  public Page handlePage(HiddenTokenAwareTree token) {
    Page result = new Page(token);
    List<HiddenTokenAwareTree> children = token.getChildren();
    for (HiddenTokenAwareTree kid : children) {
      if (kid.getType() == LessLexer.IDENT) {
        result.setName(new Name(kid, kid.getText()));
      } else if (kid.getType() == LessLexer.PSEUDO_PAGE) {
        int pseudoPageIndex = 1;
        if (kid.getChild(0).getType() == LessLexer.MEANINGFULL_WHITESPACE) {
          pseudoPageIndex = 2;
          result.setDockedPseudopage(false);
        }
        result.setPseudopage(new Name(kid, ":" + kid.getChild(pseudoPageIndex).getText()));
      } else if (kid.getType() == LessLexer.BODY) {
        result.setBody(createGeneralBody(kid));
      } else {
        throw new BugHappened(GRAMMAR_MISMATCH, kid);
      }
    }
    return result;
  }

  public PageMarginBox handlePageMarginBox(HiddenTokenAwareTree token) {
    PageMarginBox result = new PageMarginBox(token);
    List<HiddenTokenAwareTree> children = token.getChildren();
    for (HiddenTokenAwareTree kid : children) {
      if (kid.getType() == LessLexer.AT_NAME) {
        result.setName(new Name(kid, kid.getText()));
      } else if (kid.getType() == LessLexer.BODY) {
        result.setBody(createGeneralBody(kid));
      } else {
        throw new BugHappened(GRAMMAR_MISMATCH, kid);
      }
    }
    return result;
  }

  public Import handleImport(HiddenTokenAwareTree token) {
    Import result = new Import(token);
    switch (token.getType()) {
    case LessLexer.IMPORT_SYM:
      result.setKind(Import.ImportKind.IMPORT);
      break;
    case LessLexer.IMPORT_ONCE_SYM:
      result.setKind(Import.ImportKind.IMPORT_ONCE);
      break;
    case LessLexer.IMPORT_MULTIPLE_SYM:
      result.setKind(Import.ImportKind.IMPORT_MULTIPLE);
      break;
    default:
      throw new BugHappened(GRAMMAR_MISMATCH, token);
    }

    Iterator<HiddenTokenAwareTree> children = token.getChildren().iterator();
    result.setUrlExpression(handleTerm(children.next()));
    while (children.hasNext()) {
      HiddenTokenAwareTree kid = children.next();
      if (kid.getType() == LessLexer.COMMA) {
        kid.pushHiddenToSiblings();
      } else if (kid.getType() == LessLexer.MEDIA_QUERY) {
        result.add(handleMediaQuery(kid));
      } else {
        throw new BugHappened(GRAMMAR_MISMATCH, token);
      }
    }

    return result;
  }

  public NamedExpression handleNamedExpression(HiddenTokenAwareTree token) {
    HiddenTokenAwareTree nameToken = token.getChild(0);
    HiddenTokenAwareTree valueToken = token.getChild(1);
    Expression value = (Expression) switchOn(valueToken);

    return new NamedExpression(token, nameToken.getText(), value);

  }

}