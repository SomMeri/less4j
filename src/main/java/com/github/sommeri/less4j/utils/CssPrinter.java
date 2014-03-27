package com.github.sommeri.less4j.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.NotACssException;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.AnonymousExpression;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.CharsetDeclaration;
import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.Comment;
import com.github.sommeri.less4j.core.ast.ComposedExpression;
import com.github.sommeri.less4j.core.ast.CssClass;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.Declaration;
import com.github.sommeri.less4j.core.ast.Document;
import com.github.sommeri.less4j.core.ast.ElementSubsequent;
import com.github.sommeri.less4j.core.ast.EmbeddedScript;
import com.github.sommeri.less4j.core.ast.EmptyExpression;
import com.github.sommeri.less4j.core.ast.EscapedValue;
import com.github.sommeri.less4j.core.ast.ExpressionOperator;
import com.github.sommeri.less4j.core.ast.FaultyExpression;
import com.github.sommeri.less4j.core.ast.FaultyNode;
import com.github.sommeri.less4j.core.ast.FixedMediaExpression;
import com.github.sommeri.less4j.core.ast.FontFace;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.IdSelector;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.Import;
import com.github.sommeri.less4j.core.ast.InlineContent;
import com.github.sommeri.less4j.core.ast.InterpolableName;
import com.github.sommeri.less4j.core.ast.InterpolatedMediaExpression;
import com.github.sommeri.less4j.core.ast.Keyframes;
import com.github.sommeri.less4j.core.ast.KeyframesName;
import com.github.sommeri.less4j.core.ast.Media;
import com.github.sommeri.less4j.core.ast.MediaExpression;
import com.github.sommeri.less4j.core.ast.MediaExpressionFeature;
import com.github.sommeri.less4j.core.ast.MediaQuery;
import com.github.sommeri.less4j.core.ast.Medium;
import com.github.sommeri.less4j.core.ast.MediumModifier;
import com.github.sommeri.less4j.core.ast.MediumModifier.Modifier;
import com.github.sommeri.less4j.core.ast.MediumType;
import com.github.sommeri.less4j.core.ast.Name;
import com.github.sommeri.less4j.core.ast.NamedColorExpression;
import com.github.sommeri.less4j.core.ast.NamedExpression;
import com.github.sommeri.less4j.core.ast.Nth;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.Page;
import com.github.sommeri.less4j.core.ast.PageMarginBox;
import com.github.sommeri.less4j.core.ast.PseudoClass;
import com.github.sommeri.less4j.core.ast.PseudoElement;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.ast.SelectorAttribute;
import com.github.sommeri.less4j.core.ast.SelectorCombinator;
import com.github.sommeri.less4j.core.ast.SelectorOperator;
import com.github.sommeri.less4j.core.ast.SelectorPart;
import com.github.sommeri.less4j.core.ast.SimpleSelector;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.ast.Supports;
import com.github.sommeri.less4j.core.ast.SupportsCondition;
import com.github.sommeri.less4j.core.ast.SupportsConditionInParentheses;
import com.github.sommeri.less4j.core.ast.SupportsConditionNegation;
import com.github.sommeri.less4j.core.ast.SupportsLogicalCondition;
import com.github.sommeri.less4j.core.ast.SupportsLogicalOperator;
import com.github.sommeri.less4j.core.ast.SupportsQuery;
import com.github.sommeri.less4j.core.ast.SyntaxOnlyElement;
import com.github.sommeri.less4j.core.ast.UnicodeRangeExpression;
import com.github.sommeri.less4j.core.ast.Viewport;
import com.github.sommeri.less4j.core.output.ExtendedStringBuilder;
import com.github.sommeri.less4j.core.output.SourceMapBuilder;
//SelectorCombinator
//EscapedSelector
public class CssPrinter {

  private static final String ERROR = "!#error#!";
  protected ExtendedStringBuilder cssOnly = new ExtendedStringBuilder();
  protected SourceMapBuilder cssAndSM = new SourceMapBuilder(cssOnly, null);
  
  private LessSource lessSource;
  private LessSource cssDestination;

  public CssPrinter() {
    super();
  }

  public CssPrinter(LessSource lessSource, LessSource cssDestination) {
    this.lessSource = lessSource;
    this.cssDestination = cssDestination;
    this.cssAndSM = new SourceMapBuilder(cssOnly, cssDestination);
  }

  public CssPrinter(CssPrinter configureFromPrinter) {
    this.lessSource = configureFromPrinter.lessSource;
    this.cssDestination = configureFromPrinter.cssDestination;
    this.cssOnly = new ExtendedStringBuilder(configureFromPrinter.cssOnly);
    this.cssAndSM = new SourceMapBuilder(cssOnly, cssDestination);
  }

  /**
   * returns whether the output changed as a result of the operation
   * 
   * @param node
   * @return
   */
  public boolean append(ASTCssNode node) {
    // opening comments should not be docked directly in front of following
    // thing
    if (node == null || node.isSilent())
      return false;

    appendComments(node.getOpeningComments(), true);
    boolean result = switchOnType(node);
    appendComments(node.getTrailingComments(), false);
    return result;
  }

  public boolean switchOnType(ASTCssNode node) {
    switch (node.getType()) {
    case RULE_SET:
      return appendRuleset((RuleSet) node); // TODOsm: source map

    case CSS_CLASS:
      return appendCssClass((CssClass) node); // TODOsm: source map

    case PSEUDO_CLASS:
      return appendPseudoClass((PseudoClass) node); // TODOsm: source map

    case PSEUDO_ELEMENT:
      return appendPseudoElement((PseudoElement) node); // TODOsm: source map

    case NTH:
      return appendNth((Nth) node); // TODOsm: source map

    case SELECTOR:
      return appendSelector((Selector) node); // TODOsm: source map

    case SIMPLE_SELECTOR:
      return appendSimpleSelector((SimpleSelector) node); // TODOsm: source map

    case SELECTOR_OPERATOR:
      return appendSelectorOperator((SelectorOperator) node); // TODOsm: source map

    case SELECTOR_COMBINATOR:
      return appendSelectorCombinator((SelectorCombinator) node); // TODOsm: source map

    case SELECTOR_ATTRIBUTE:
      return appendSelectorAttribute((SelectorAttribute) node); // TODOsm: source map

    case ID_SELECTOR:
      return appendIdSelector((IdSelector) node); 

    case CHARSET_DECLARATION:
      return appendCharsetDeclaration((CharsetDeclaration) node); // TODOsm: source map

    case FONT_FACE:
      return appendFontFace((FontFace) node); // TODOsm: source map

    case NAMED_EXPRESSION:
      return appendNamedExpression((NamedExpression) node); // TODOsm: source map

    case COMPOSED_EXPRESSION:
      return appendComposedExpression((ComposedExpression) node); // TODOsm: source map

    case EXPRESSION_OPERATOR:
      return appendExpressionOperator((ExpressionOperator) node); // TODOsm: source map

    case STRING_EXPRESSION:
      return appendCssString((CssString) node); // TODOsm: source map

    case EMPTY_EXPRESSION:
      return appendEmptyExpression((EmptyExpression) node); // TODOsm: source map

    case NUMBER:
      return appendNumberExpression((NumberExpression) node); // TODOsm: source map

    case IDENTIFIER_EXPRESSION:
      return appendIdentifierExpression((IdentifierExpression) node); // TODOsm: source map

    case UNICODE_RANGE_EXPRESSION:
      return appendUnicodeRangeExpression((UnicodeRangeExpression) node); // TODOsm: source map

    case COLOR_EXPRESSION:
      return appendColorExpression((ColorExpression) node); // TODOsm: source map

    case FUNCTION:
      return appendFunctionExpression((FunctionExpression) node); // TODOsm: source map

    case DECLARATION:
      return appendDeclaration((Declaration) node); // TODOsm: source map

    case MEDIA:
      return appendMedia((Media) node); // TODOsm: source map

    case MEDIA_QUERY:
      return appendMediaQuery((MediaQuery) node); // TODOsm: source map

    case MEDIUM:
      return appendMedium((Medium) node); // TODOsm: source map

    case MEDIUM_MODIFIER:
      return appendMediumModifier((MediumModifier) node); // TODOsm: source map

    case MEDIUM_TYPE:
      return appendMediumType((MediumType) node); // TODOsm: source map

    case FIXED_MEDIA_EXPRESSION:
      return appendMediaExpression((FixedMediaExpression) node); // TODOsm: source map

    case INTERPOLATED_MEDIA_EXPRESSION:
      return appendInterpolatedMediaExpression((InterpolatedMediaExpression) node); // TODOsm: source map

    case MEDIUM_EX_FEATURE:
      return appendMediaExpressionFeature((MediaExpressionFeature) node); // TODOsm: source map

    case STYLE_SHEET:
      return appendStyleSheet((StyleSheet) node); // TODOsm: source map

    case FAULTY_EXPRESSION:
      return appendFaultyExpression((FaultyExpression) node); // TODOsm: source map

    case FAULTY_NODE:
      return appendFaultyNode((FaultyNode) node); // TODOsm: source map

    case ESCAPED_VALUE:
      return appendEscapedValue((EscapedValue) node); // TODOsm: source map

    case EMBEDDED_SCRIPT:
      return appendEmbeddedScript((EmbeddedScript) node); // TODOsm: source map

    case KEYFRAMES:
      return appendKeyframes((Keyframes) node); // TODOsm: source map

    case KEYFRAMES_NAME:
      return appendKeyframesName((KeyframesName) node); // TODOsm: source map

    case DOCUMENT:
      return appendDocument((Document) node); // TODOsm: source map

    case VIEWPORT:
      return appendViewport((Viewport) node); // TODOsm: source map

    case GENERAL_BODY:
      return appendBodyOptimizeDuplicates((GeneralBody) node); // TODOsm: source map

    case PAGE:
      return appendPage((Page) node); // TODOsm: source map

    case PAGE_MARGIN_BOX:
      return appendPageMarginBox((PageMarginBox) node); // TODOsm: source map

    case NAME:
      return appendName((Name) node); // TODOsm: source map

    case IMPORT:
      return appendImport((Import) node); // TODOsm: source map

    case ANONYMOUS:
      return appendAnonymous((AnonymousExpression) node); // TODOsm: source map

    case SYNTAX_ONLY_ELEMENT:
      return appendSyntaxOnlyElement((SyntaxOnlyElement) node); // TODOsm: source map

    case SUPPORTS:
      return appendSupports((Supports) node); // TODOsm: source map

    case SUPPORTS_QUERY:
      return appendSupportsQuery((SupportsQuery) node); // TODOsm: source map

    case SUPPORTS_CONDITION_NEGATION:
      return appendSupportsConditionNegation((SupportsConditionNegation) node); // TODOsm: source map

    case SUPPORTS_CONDITION_PARENTHESES:
      return appendSupportsConditionParentheses((SupportsConditionInParentheses) node); // TODOsm: source map

    case SUPPORTS_CONDITION_LOGICAL:
      return appendSupportsConditionLogical((SupportsLogicalCondition) node); // TODOsm: source map

    case SUPPORTS_LOGICAL_OPERATOR:
      return appendSupportsLogicalOperator((SupportsLogicalOperator) node); // TODOsm: source map

    case INLINE_CONTENT:
      return appendInlineContent((InlineContent) node); // TODOsm: source map

    case ESCAPED_SELECTOR:
    case PARENTHESES_EXPRESSION:
    case SIGNED_EXPRESSION:
    case VARIABLE:
    case INDIRECT_VARIABLE:
    case VARIABLE_DECLARATION:
      throw new NotACssException(node);
      
    default:
      throw new IllegalStateException("Unknown: " + node.getType() + " " + node.getSourceLine() + ":" + node.getSourceColumn());
    }
  }

  //TODO: what about source maps?
  private boolean appendInlineContent(InlineContent node) {
    cssOnly.appendAsIs(node.getValue());
    return false;
  }

  private boolean appendSyntaxOnlyElement(SyntaxOnlyElement node) {
    cssOnly.append(node.getSymbol());
    return true;
  }

  private boolean appendAnonymous(AnonymousExpression node) {
    cssOnly.append(node.getValue());
    return true;
  }

  private boolean appendImport(Import node) {
    cssOnly.append("@import").ensureSeparator();
    append(node.getUrlExpression());
    appendMediums(node.getMediums());
    cssOnly.append(";");

    return true;
  }

  private boolean appendName(Name node) {
    cssOnly.append(node.getName());
    return true;
  }

  private boolean appendPage(Page node) {
    cssOnly.append("@page").ensureSeparator();
    if (node.hasName()) {
      append(node.getName());
      if (!node.hasDockedPseudopage())
        cssOnly.ensureSeparator();
    }

    if (node.hasPseudopage()) {
      append(node.getPseudopage());
      cssOnly.ensureSeparator();
    }

    appendBodySortDeclarations(node.getBody());
    return true;
  }

  private boolean appendPageMarginBox(PageMarginBox node) {
    append(node.getName());
    appendBodySortDeclarations(node.getBody());
    return true;
  }

  private boolean appendKeyframesName(KeyframesName node) {
    cssOnly.append(node.getName());
    return true;
  }

  private boolean appendKeyframes(Keyframes node) {
    cssOnly.append(node.getDialect()).ensureSeparator();

    Iterator<KeyframesName> names = node.getNames().iterator();
    if (names.hasNext()) {
      append(names.next());
    }
    while (names.hasNext()) {
      cssOnly.append(",").ensureSeparator();
      append(names.next());
    }

    append(node.getBody());
    return true;
  }

  private boolean appendSupports(Supports node) {
    cssOnly.append(node.getDialect()).ensureSeparator();
    append(node.getCondition());
    cssOnly.ensureSeparator();
    append(node.getBody());
    return true;
  }

  private boolean appendSupportsConditionNegation(SupportsConditionNegation node) {
    append(node.getNegation());
    cssOnly.ensureSeparator();
    append(node.getCondition());
    return true;
  }

  private boolean appendSupportsConditionParentheses(SupportsConditionInParentheses node) {
    append(node.getOpeningParentheses());
    append(node.getCondition());
    append(node.getClosingParentheses());
    return true;
  }

  private boolean appendSupportsConditionLogical(SupportsLogicalCondition node) {
    Iterator<SupportsLogicalOperator> operators = node.getLogicalOperators().iterator();
    Iterator<SupportsCondition> conditions = node.getConditions().iterator();

    append(conditions.next());

    while (operators.hasNext()) {
      cssOnly.ensureSeparator();
      append(operators.next());
      cssOnly.ensureSeparator();
      append(conditions.next());
    }
    return true;
  }

  private boolean appendSupportsLogicalOperator(SupportsLogicalOperator node) {
    if (node.getOperator() == null) {
      cssOnly.append(ERROR);
    }
    cssOnly.append(node.getOperator().getSymbol());
    return true;
  }

  private boolean appendSupportsQuery(SupportsQuery node) {
    append(node.getOpeningParentheses());
    append(node.getDeclaration());
    append(node.getClosingParentheses());
    return true;
  }

  private boolean appendDocument(Document node) {
    cssOnly.append(node.getDialect()).ensureSeparator();

    Iterator<FunctionExpression> urlMatchFunctions = node.getUrlMatchFunctions().iterator();
    if (urlMatchFunctions.hasNext()) {
      append(urlMatchFunctions.next());
    }
    while (urlMatchFunctions.hasNext()) {
      cssOnly.append(",").ensureSeparator();
      append(urlMatchFunctions.next());
    }

    append(node.getBody());
    return true;
  }

  private boolean appendViewport(Viewport node) {
    cssOnly.append(node.getDialect());
    append(node.getBody());
    return true;
  }

  private boolean appendFaultyNode(FaultyNode node) {
    cssOnly.append(ERROR);
    return true;
  }

  private boolean appendFaultyExpression(FaultyExpression node) {
    cssOnly.append(ERROR);
    return true;
  }

  private boolean appendNth(Nth node) {
    switch (node.getForm()) {
    case EVEN:
      cssOnly.append("even");
      return true;

    case ODD:
      cssOnly.append("odd");
      return true;

    case STANDARD:
      if (node.getRepeater() != null)
        append(node.getRepeater());
      if (node.getMod() != null)
        append(node.getMod());

    }

    return true;
  }

  protected void appendComments(List<Comment> comments, boolean ensureSeparator) {
    if (comments == null || comments.isEmpty())
      return;

    cssOnly.ensureSeparator();

    for (Comment comment : comments) {
      cssOnly.append(comment.getComment());
      if (comment.hasNewLine())
        cssOnly.newLine();
    }

    if (ensureSeparator)
      cssOnly.ensureSeparator();
  }

  public boolean appendFontFace(FontFace node) {
    cssOnly.append("@font-face").ensureSeparator();
    append(node.getBody());

    return true;
  }

  public boolean appendCharsetDeclaration(CharsetDeclaration node) {
    cssOnly.append("@charset").ensureSeparator();
    cssOnly.append(node.getCharset());
    cssOnly.append(";");

    return true;
  }

  public boolean appendIdSelector(IdSelector node) {
    cssAndSM.append(node.getFullName(), node.getUnderlyingStructure());
    return true;
  }

  public boolean appendSelectorAttribute(SelectorAttribute node) {
    cssOnly.append("[");
    cssOnly.append(node.getName());
    append(node.getOperator());
    append(node.getValue());
    cssOnly.append("]");

    return true;
  }

  private boolean appendSelectorOperator(SelectorOperator operator) {
    SelectorOperator.Operator realOperator = operator.getOperator();
    switch (realOperator) {
    case NONE:
      break;

    default:
      cssOnly.append(realOperator.getSymbol());
    }
    return true;
  }

  public boolean appendPseudoClass(PseudoClass node) {
    cssOnly.append(":");
    cssOnly.append(node.getName());
    if (node.hasParameters()) {
      cssOnly.append("(");
      append(node.getParameter());
      cssOnly.append(")");
    }

    return true;
  }

  public boolean appendPseudoElement(PseudoElement node) {
    cssOnly.append(":");
    if (!node.isLevel12Form())
      cssOnly.append(":");

    cssOnly.append(node.getName());

    return true;
  }

  public boolean appendStyleSheet(StyleSheet styleSheet) {
    appendComments(styleSheet.getOrphanComments(), false);
    appendAllChilds(styleSheet);
    return true;
  }

  public boolean appendRuleset(RuleSet ruleSet) {
    if (ruleSet.hasEmptyBody())
      return false;

    appendSelectors(ruleSet.getSelectors());
    append(ruleSet.getBody());

    return true;
  }

  private boolean appendBodyOptimizeDuplicates(Body body) {
    if (body.isEmpty())
      return false;

    cssOnly.ensureSeparator();
    append(body.getOpeningCurlyBrace());
    cssOnly.ensureNewLine().increaseIndentationLevel();
    Iterable<CssPrinter> declarationsBuilders = collectUniqueBodyMembersStrings(body);
    for (CssPrinter miniBuilder : declarationsBuilders) {
      append(miniBuilder);
    }

    appendComments(body.getOrphanComments(), false);
    cssOnly.decreaseIndentationLevel();
    append(body.getClosingCurlyBrace());

    return true;
  }

  private void append(CssPrinter miniBuilder) {
    cssAndSM.append(miniBuilder.cssAndSM);
  }

  private Iterable<CssPrinter> collectUniqueBodyMembersStrings(Body body) {
    // the same declaration must be printed only once
    LastOfKindSet<String, CssPrinter> declarationsStrings = new LastOfKindSet<String, CssPrinter>();
    for (ASTCssNode declaration : body.getMembers()) {
      CssPrinter miniPrinter = new CssPrinter(this);

      miniPrinter.append(declaration);
      miniPrinter.cssOnly.ensureNewLine();

      declarationsStrings.add(miniPrinter.toCss(), miniPrinter);
    }

    return declarationsStrings;
  }

  public boolean appendDeclaration(Declaration declaration) {
    cssOnly.appendIgnoreNull(declaration.getNameAsString());
    cssOnly.append(":").ensureSeparator();
    if (declaration.getExpression() != null)
      append(declaration.getExpression());

    if (declaration.isImportant())
      cssOnly.ensureSeparator().append("!important");

    if (shouldHaveSemicolon(declaration))
      cssOnly.appendIgnoreNull(";");

    return true;
  }

  private boolean shouldHaveSemicolon(Declaration declaration) {
    if (null == declaration.getParent() || declaration.getParent().getType() != ASTCssNodeType.SUPPORTS_QUERY)
      return true;

    return false;
  }

  private boolean appendMedia(Media node) {
    cssOnly.append("@media");
    appendMediums(node.getMediums());
    appendBodySortDeclarations(node.getBody());

    return true;
  }

  private void appendBodySortDeclarations(Body node) {
    // this is sort of hack, bypass the usual append method
    appendComments(node.getOpeningComments(), true);

    cssOnly.ensureSeparator();
    append(node.getOpeningCurlyBrace());
    cssOnly.ensureNewLine().increaseIndentationLevel();

    Iterator<ASTCssNode> declarations = node.getDeclarations().iterator();
    List<ASTCssNode> notDeclarations = node.getNotDeclarations();
    while (declarations.hasNext()) {
      ASTCssNode declaration = declarations.next();
      append(declaration);
      if (declarations.hasNext() || notDeclarations.isEmpty())
        cssOnly.ensureNewLine();
    }
    for (ASTCssNode body : notDeclarations) {
      boolean changedAnything = append(body);
      if (changedAnything)
        cssOnly.ensureNewLine();
    }

    appendComments(node.getOrphanComments(), false);
    cssOnly.decreaseIndentationLevel();
    append(node.getClosingCurlyBrace());

    // this is sort of hack, bypass the usual append method
    appendComments(node.getTrailingComments(), false);
  }

  private void appendMediums(List<MediaQuery> mediums) {
    if (mediums == null || mediums.isEmpty())
      return;

    boolean needComma = false;
    for (MediaQuery mediaQuery : mediums) {
      if (needComma)
        cssOnly.append(",").ensureSeparator();
      append(mediaQuery);
      needComma = true;
    }
  }

  public boolean appendMediaQuery(MediaQuery mediaQuery) {
    cssOnly.ensureSeparator();
    append(mediaQuery.getMedium());
    boolean needSeparator = (mediaQuery.getMedium() != null);
    for (MediaExpression mediaExpression : mediaQuery.getExpressions()) {
      if (needSeparator) {
        cssOnly.ensureSeparator().append("and");
      }
      append(mediaExpression);
      needSeparator = true;
    }

    return true;
  }

  public boolean appendMedium(Medium medium) {
    append(medium.getModifier());
    append(medium.getMediumType());

    return true;
  }

  public boolean appendMediumModifier(MediumModifier modifier) {
    Modifier kind = modifier.getModifier();
    switch (kind) {
    case ONLY:
      cssOnly.ensureSeparator().append("only");
      break;

    case NOT:
      cssOnly.ensureSeparator().append("not");
      break;

    case NONE:
      break;

    default:
      throw new IllegalStateException("Unknown modifier type.");
    }

    return true;
  }

  public boolean appendMediumType(MediumType medium) {
    cssOnly.ensureSeparator().append(medium.getName());

    return true;
  }

  public boolean appendMediaExpression(FixedMediaExpression expression) {
    cssOnly.ensureSeparator().append("(");
    append(expression.getFeature());
    if (expression.getExpression() != null) {
      cssOnly.append(":").ensureSeparator();
      append(expression.getExpression());
    }
    cssOnly.append(")");
    return true;
  }

  private boolean appendInterpolatedMediaExpression(InterpolatedMediaExpression expression) {
    cssOnly.ensureSeparator();
    return append(expression.getExpression());
  }

  public boolean appendMediaExpressionFeature(MediaExpressionFeature feature) {
    cssOnly.append(feature.getFeature());
    return true;
  }

  public boolean appendNamedExpression(NamedExpression expression) {
    cssOnly.append(expression.getName());
    cssOnly.append("=");
    append(expression.getExpression());

    return true;
  }

  public boolean appendComposedExpression(ComposedExpression expression) {
    append(expression.getLeft());
    append(expression.getOperator());
    append(expression.getRight());

    return true;
  }

  public boolean appendExpressionOperator(ExpressionOperator operator) {
    ExpressionOperator.Operator realOperator = operator.getOperator();
    switch (realOperator) {
    case COMMA:
      cssOnly.append(realOperator.getSymbol()).ensureSeparator();
      break;

    case EMPTY_OPERATOR:
      cssOnly.ensureSeparator();
      break;

    // TODO this is a huge hack which goes around
    // "we do not parse fonts and less.js does" lack of feature
    // left here intentionally, so we can have correct unit test and can come
    // back to it later
    case MINUS:
      cssOnly.ensureSeparator().append("-");
      break;

    default:
      cssOnly.append(realOperator.getSymbol());
    }

    return true;
  }

  public boolean appendCssString(CssString expression) {
    String quoteType = expression.getQuoteType();
    cssOnly.append(quoteType).append(expression.getValue()).append(quoteType);

    return true;
  }

  public boolean appendEmptyExpression(EmptyExpression node){
    return true;
  }
  
  public boolean appendEscapedValue(EscapedValue escaped) {
    cssOnly.append(escaped.getValue());

    return true;
  }

  public boolean appendEmbeddedScript(EmbeddedScript escaped) {
    cssOnly.append(escaped.getValue());

    return true;
  }

  public boolean appendIdentifierExpression(IdentifierExpression expression) {
    cssOnly.append(expression.getValue());

    return true;
  }

  public boolean appendUnicodeRangeExpression(UnicodeRangeExpression expression) {
    cssOnly.append(expression.getValue());

    return true;
  }

  protected boolean appendColorExpression(ColorExpression expression) {
    // if it is named color expression, write out the name
    if (expression instanceof NamedColorExpression) {
      NamedColorExpression named = (NamedColorExpression) expression;
      cssOnly.append(named.getColorName());
    } else {
      cssOnly.append(expression.getValue());
    }

    return true;
  }

  private boolean appendFunctionExpression(FunctionExpression node) {
    cssOnly.append(node.getName());
    cssOnly.append("(");
    append(node.getParameter());
    cssOnly.append(")");

    return true;
  }

  private boolean appendNumberExpression(NumberExpression node) {
    if (node.hasOriginalString()) {
      cssOnly.append(node.getOriginalString());
    } else {
      if (node.hasExpliciteSign()) {
        if (0 < node.getValueAsDouble())
          cssOnly.append("+");
        else
          cssOnly.append("-");
      }
      cssOnly.append(PrintUtils.formatNumber(node.getValueAsDouble()) + node.getSuffix());
    }

    return true;
  }

  public void appendSelectors(List<Selector> selectors) {
    selectors = filterSilent(selectors);
    // follow less.js formatting in special case - only one empty selector
    if (selectors.size() == 1 && isEmptySelector(selectors.get(0))) {
      cssOnly.append(" ");
    }

    Iterator<Selector> iterator = selectors.iterator();
    while (iterator.hasNext()) {
      Selector selector = iterator.next();
      append(selector);

      if (iterator.hasNext())
        cssOnly.append(",").newLine();
    }
  }

  private <T  extends ASTCssNode> List<T> filterSilent(List<T> nodes) {
    List<T> result = new ArrayList<T>();
    for (T t : nodes) {
      if (!t.isSilent())
        result.add(t);
    }
    return result;
  }

  private boolean isEmptySelector(Selector selector) {
    if (selector.isCombined())
      return false;

    SelectorPart head = selector.getHead();
    if (head.getType() != ASTCssNodeType.SIMPLE_SELECTOR)
      return false;

    SimpleSelector simpleHead = (SimpleSelector) head;
    if (!simpleHead.isEmptyForm() || !simpleHead.isStar()) {
      return false;
    }

    if (simpleHead.hasSubsequent())
      return false;

    return true;
  }

  public boolean appendSelector(Selector selector) {
    for (SelectorPart part : selector.getParts()) {
      append(part);
    }
    return true;
  }

  private boolean appendSimpleSelector(SimpleSelector selector) {
    if (selector.hasLeadingCombinator())
      append(selector.getLeadingCombinator());

    appendSimpleSelectorHead(selector);
    appendSimpleSelectorTail(selector);
    return true;
  }

  private void appendSimpleSelectorTail(SimpleSelector selector) {
    List<ElementSubsequent> allChilds = selector.getSubsequent();
    for (ElementSubsequent astCssNode : allChilds) {
      append(astCssNode);
    }
  }

  private boolean appendCssClass(CssClass cssClass) {
    //cssOnly.append(cssClass.getFullName());
    cssAndSM.append(cssClass.getFullName(), cssClass.getUnderlyingStructure());
    return true;
  }

  private void appendSimpleSelectorHead(SimpleSelector selector) {
    cssOnly.ensureSeparator();
    if (!selector.isStar() || !selector.isEmptyForm()) {
      InterpolableName elementName = selector.getElementName();
      cssAndSM.appendIgnoreNull(elementName.getName(), elementName.getUnderlyingStructure());
    }
  }

  public boolean appendSelectorCombinator(SelectorCombinator combinator) {
    SelectorCombinator.Combinator realCombinator = combinator.getCombinator();
    switch (realCombinator) {
    case DESCENDANT:
      cssOnly.ensureSeparator();
      break;

    default:
      cssOnly.ensureSeparator().append(realCombinator.getSymbol());

    }

    return true;
  }

  private void appendAllChilds(ASTCssNode node) {
    List<? extends ASTCssNode> allChilds = node.getChilds();
    appendAll(allChilds);
  }

  private void appendAll(List<? extends ASTCssNode> all) {
    for (ASTCssNode kid : all) {
      if (append(kid))
        cssOnly.ensureNewLine();
    }
  }

  public String toString() {
    return cssOnly.toString();
  }

  public String toCss() {
    return cssOnly.toString();
  }

  public String toSourceMap() {
    return cssAndSM.toSourceMap();
  }
}
