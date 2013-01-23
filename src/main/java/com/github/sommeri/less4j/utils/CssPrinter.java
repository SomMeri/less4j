package com.github.sommeri.less4j.utils;

import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;

import com.github.sommeri.less4j.core.ExtendedStringBuilder;
import com.github.sommeri.less4j.core.NotACssException;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.AnonymousExpression;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.CharsetDeclaration;
import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.Comment;
import com.github.sommeri.less4j.core.ast.ComposedExpression;
import com.github.sommeri.less4j.core.ast.CssClass;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.Declaration;
import com.github.sommeri.less4j.core.ast.ElementSubsequent;
import com.github.sommeri.less4j.core.ast.EscapedValue;
import com.github.sommeri.less4j.core.ast.ExpressionOperator;
import com.github.sommeri.less4j.core.ast.FaultyExpression;
import com.github.sommeri.less4j.core.ast.FaultyNode;
import com.github.sommeri.less4j.core.ast.FontFace;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.IdSelector;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.Import;
import com.github.sommeri.less4j.core.ast.Keyframes;
import com.github.sommeri.less4j.core.ast.KeyframesBody;
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
import com.github.sommeri.less4j.core.ast.RuleSetsBody;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.ast.SelectorAttribute;
import com.github.sommeri.less4j.core.ast.SelectorCombinator;
import com.github.sommeri.less4j.core.ast.SelectorOperator;
import com.github.sommeri.less4j.core.ast.SimpleSelector;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.ast.Viewport;

public class CssPrinter {

  protected ExtendedStringBuilder builder = new ExtendedStringBuilder();
  private static final DecimalFormat FORMATTER = new DecimalFormat("#.##################");

  public CssPrinter() {
    super();
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
    if (node == null)
      return false;

    appendComments(node.getOpeningComments(), true);
    boolean result = switchOnType(node);
    appendComments(node.getTrailingComments(), false);
    return result;
  }

  public boolean switchOnType(ASTCssNode node) {
    switch (node.getType()) {
    case RULE_SET:
      return appendRuleset((RuleSet) node);

    case DECLARATIONS_BODY:
      return appendBodyOptimizeDuplicates((RuleSetsBody) node);

    case CSS_CLASS:
      return appendCssClass((CssClass) node);

    case PSEUDO_CLASS:
      return appendPseudoClass((PseudoClass) node);

    case PSEUDO_ELEMENT:
      return appendPseudoElement((PseudoElement) node);

    case NTH:
      return appendNth((Nth) node);

    case SELECTOR:
      return appendSelector((Selector) node);

    case SIMPLE_SELECTOR:
      return appendSimpleSelector((SimpleSelector) node);

    case SELECTOR_OPERATOR:
      return appendSelectorOperator((SelectorOperator) node);

    case SELECTOR_COMBINATOR:
      return appendSelectorCombinator((SelectorCombinator) node);

    case SELECTOR_ATTRIBUTE:
      return appendSelectorAttribute((SelectorAttribute) node);

    case ID_SELECTOR:
      return appendIdSelector((IdSelector) node);

    case CHARSET_DECLARATION:
      return appendCharsetDeclaration((CharsetDeclaration) node);

    case FONT_FACE:
      return appendFontFace((FontFace) node);

    case NAMED_EXPRESSION:
      return appendNamedExpression((NamedExpression) node);

    case COMPOSED_EXPRESSION:
      return appendComposedExpression((ComposedExpression) node);

    case EXPRESSION_OPERATOR:
      return appendExpressionOperator((ExpressionOperator) node);

    case STRING_EXPRESSION:
      return appendCssString((CssString) node);

    case NUMBER:
      return appendNumberExpression((NumberExpression) node);

    case IDENTIFIER_EXPRESSION:
      return appendIdentifierExpression((IdentifierExpression) node);

    case COLOR_EXPRESSION:
      return appendColorExpression((ColorExpression) node);

    case FUNCTION:
      return appendFunctionExpression((FunctionExpression) node);

    case DECLARATION:
      return appendDeclaration((Declaration) node);

    case MEDIA:
      return appendMedia((Media) node);

    case MEDIA_QUERY:
      return appendMediaQuery((MediaQuery) node);

    case MEDIUM:
      return appendMedium((Medium) node);

    case MEDIUM_MODIFIER:
      return appendMediumModifier((MediumModifier) node);

    case MEDIUM_TYPE:
      return appendMediumType((MediumType) node);

    case MEDIA_EXPRESSION:
      return appendMediaExpression((MediaExpression) node);

    case MEDIUM_EX_FEATURE:
      return appendMediaExpressionFeature((MediaExpressionFeature) node);

    case STYLE_SHEET:
      return appendStyleSheet((StyleSheet) node);

    case FAULTY_EXPRESSION:
      return appendFaultyExpression((FaultyExpression) node);

    case FAULTY_NODE:
      return appendFaultyNode((FaultyNode) node);

    case ESCAPED_VALUE:
      return appendEscapedValue((EscapedValue) node);

    case KEYFRAMES:
      return appendKeyframes((Keyframes) node);

    case KEYFRAMES_NAME:
      return appendKeyframesName((KeyframesName) node);

    case KEYFRAMES_BODY:
      return appendBodyOptimizeDuplicates((KeyframesBody) node);
      
    case VIEWPORT:
      return appendViewport((Viewport) node);

    case GENERAL_BODY:
      return appendBodyOptimizeDuplicates((GeneralBody) node);

    case PAGE:
      return appendPage((Page) node);

    case PAGE_MARGIN_BOX:
      return appendPageMarginBox((PageMarginBox) node);

    case NAME:
      return appendName((Name) node);

    case IMPORT:
      return appendImport((Import) node);
      
    case ANONYMOUS:
      return appendAnonymous((AnonymousExpression) node);

    case ESCAPED_SELECTOR:
    case PARENTHESES_EXPRESSION:
    case SIGNED_EXPRESSION:
    case VARIABLE:
    case INDIRECT_VARIABLE:
    case VARIABLE_DECLARATION:
      throw new NotACssException(node);

    default:
      throw new IllegalStateException("Unknown: " + node.getType() + " " + node.getSourceLine() + ":" + node.getCharPositionInSourceLine());
    }
  }
  
  private boolean appendAnonymous(AnonymousExpression node) {
    builder.append(node.getValue());
    return true;
  }

  private boolean appendImport(Import node) {
    builder.append("@import").ensureSeparator();
    append(node.getUrlExpression());
    appendMediums(node.getMediums());
    builder.append(";");
    
    return true;
  }

  private boolean appendName(Name node) {
    builder.append(node.getName());
    return true;
  }

  private boolean appendPage(Page node) {
    builder.append("@page").ensureSeparator();
    if (node.hasName()) {
      append(node.getName());
      if (!node.hasDockedPseudopage())
        builder.ensureSeparator();
    }

    if (node.hasPseudopage()) {
      append(node.getPseudopage());
      builder.ensureSeparator();
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
    builder.append(node.getName());
    return true;
  }

  private boolean appendKeyframes(Keyframes node) {
    builder.append(node.getDialect()).ensureSeparator();
    
    Iterator<KeyframesName> names = node.getNames().iterator();
    if (names.hasNext()) {
      append(names.next());
    }
    while (names.hasNext()) {
      builder.append(",").ensureSeparator();
      append(names.next());
    }
    
    append(node.getBody());
    return true;
  }
  
  private boolean appendViewport(Viewport node) {
    builder.append("@viewport");
    append(node.getBody());
    return true;
  }



  private boolean appendFaultyNode(FaultyNode node) {
    builder.append("!#error#!");
    return true;
  }

  private boolean appendFaultyExpression(FaultyExpression node) {
    builder.append("!#error#!");
    return true;
  }

  private boolean appendNth(Nth node) {
    switch (node.getForm()) {
    case EVEN:
      builder.append("even");
      return true;

    case ODD:
      builder.append("odd");
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

    builder.ensureSeparator();

    for (Comment comment : comments) {
      builder.append(comment.getComment());
      if (comment.hasNewLine())
        builder.newLine();
    }

    if (ensureSeparator)
      builder.ensureSeparator();
  }

  public boolean appendFontFace(FontFace node) {
    builder.append("@font-face {").newLine();
    builder.increaseIndentationLevel();
    appendAllChilds(node);
    builder.decreaseIndentationLevel();
    builder.append("}");

    return true;
  }

  public boolean appendCharsetDeclaration(CharsetDeclaration node) {
    builder.append("@charset").ensureSeparator();
    builder.append(node.getCharset());
    builder.append(";");

    return true;
  }

  public boolean appendIdSelector(IdSelector node) {
    builder.append("#");
    builder.append(node.getName());

    return true;
  }

  public boolean appendSelectorAttribute(SelectorAttribute node) {
    builder.append("[");
    builder.append(node.getName());
    append(node.getOperator());
    builder.appendIgnoreNull(node.getValue());
    builder.append("]");

    return true;
  }

  private boolean appendSelectorOperator(SelectorOperator operator) {
    SelectorOperator.Operator realOperator = operator.getOperator();
    switch (realOperator) {
    case NONE:
      break;

    default:
      builder.append(realOperator.getSymbol());
    }
    return true;
  }

  public boolean appendPseudoClass(PseudoClass node) {
    builder.append(":");
    builder.append(node.getName());
    if (node.hasParameters()) {
      builder.append("(");
      append(node.getParameter());
      builder.append(")");
    }

    return true;
  }

  public boolean appendPseudoElement(PseudoElement node) {
    builder.append(":");
    if (!node.isLevel12Form())
      builder.append(":");

    builder.append(node.getName());

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

  public boolean appendBodyOptimizeDuplicates(Body<? extends ASTCssNode> body) {
    if (body.isEmpty())
      return false;

    builder.ensureSeparator().append("{").newLine();
    builder.increaseIndentationLevel();
    LinkedHashSet<String> declarationsBuilders = collectUniqueBodyMembersStrings(body);
    for (String miniBuilder : declarationsBuilders) {
      builder.appendAsIs(miniBuilder);
    }
    
    appendComments(body.getOrphanComments(), false);
    builder.decreaseIndentationLevel();
    builder.append("}");

    return true;
  }

  private LinkedHashSet<String> collectUniqueBodyMembersStrings(Body<? extends ASTCssNode> body) {
    //the same declaration must be printed only once
    ExtendedStringBuilder storedBuilder = builder;
    LinkedHashSet<String> declarationsStrings = new LinkedHashSet<String>();
    for (ASTCssNode declaration : body.getChilds()) {
      builder = new ExtendedStringBuilder(builder);
      append(declaration);
      builder.ensureNewLine();
      String declarationSnipped = builder.toString();
      if (declarationsStrings.contains(declarationSnipped)) {
        declarationsStrings.remove(declarationSnipped);
      }
      declarationsStrings.add(declarationSnipped);
    }
    
    storedBuilder.configureFrom(builder);
    builder = storedBuilder;
    return declarationsStrings;
  }

  public boolean appendDeclaration(Declaration declaration) {
    builder.appendIgnoreNull(declaration.getName());
    builder.append(":").ensureSeparator();
    if (declaration.getExpression() != null)
      append(declaration.getExpression());

    if (declaration.isImportant())
      builder.ensureSeparator().append("!important");
    builder.appendIgnoreNull(";");

    return true;
  }

  public boolean appendMedia(Media node) {
    builder.append("@media");
    appendMediums(node.getMediums());
    appendBodySortDeclarations(node);

    return true;
  }

  //TODO: the way this is used ignores comments in front of the body
  private void appendBodySortDeclarations(Body<ASTCssNode> node) {
    builder.ensureSeparator().append("{").newLine();
    builder.increaseIndentationLevel();

    Iterator<ASTCssNode> declarations = node.getDeclarations().iterator();
    List<ASTCssNode> notDeclarations = node.getNotDeclarations();
    while (declarations.hasNext()) {
      ASTCssNode declaration = declarations.next();
      append(declaration);
      if (declarations.hasNext() || notDeclarations.isEmpty())
        builder.ensureNewLine();
    }
    for (ASTCssNode body : notDeclarations) {
      boolean changedAnything = append(body);
      if (changedAnything)
        builder.ensureNewLine();
    }
    builder.decreaseIndentationLevel();
    builder.append("}");
  }

  private void appendMediums(List<MediaQuery> mediums) {
    if (mediums == null || mediums.isEmpty())
      return;

    boolean needComma = false;
    for (MediaQuery mediaQuery : mediums) {
      if (needComma)
        builder.append(",").ensureSeparator();
      append(mediaQuery);
      needComma = true;
    }
  }

  public boolean appendMediaQuery(MediaQuery mediaQuery) {
    builder.ensureSeparator();
    append(mediaQuery.getMedium());
    boolean needSeparator = (mediaQuery.getMedium() != null);
    for (MediaExpression mediaExpression : mediaQuery.getExpressions()) {
      if (needSeparator) {
        builder.ensureSeparator().append("and");
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
      builder.ensureSeparator().append("only");
      break;

    case NOT:
      builder.ensureSeparator().append("not");
      break;

    case NONE:
      break;

    default:
      throw new IllegalStateException("Unknown modifier type.");
    }

    return true;
  }

  public boolean appendMediumType(MediumType medium) {
    builder.ensureSeparator().append(medium.getName());

    return true;
  }

  public boolean appendMediaExpression(MediaExpression expression) {
    builder.ensureSeparator().append("(");
    append(expression.getFeature());
    if (expression.getExpression() != null) {
      builder.append(":").ensureSeparator();
      append(expression.getExpression());
    }
    builder.append(")");
    return true;
  }

  public boolean appendMediaExpressionFeature(MediaExpressionFeature feature) {
    builder.append(feature.getFeature());
    return true;
  }

  public boolean appendNamedExpression(NamedExpression expression) {
    builder.append(expression.getName());
    builder.append("=");
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
      builder.append(realOperator.getSymbol()).ensureSeparator();
      break;

    case EMPTY_OPERATOR:
      builder.ensureSeparator();
      break;

    //TODO this is a huge hack which goes around "we do not parse fonts and less.js does" lack of feature
    //left here intentionally, so we can have correct unit test and can come back to it later
    case MINUS:
      builder.ensureSeparator().append("-");
      break;

    default:
      builder.append(realOperator.getSymbol());
    }

    return true;
  }

  public boolean appendCssString(CssString expression) {
    String quoteType = expression.getQuoteType();
    builder.append(quoteType).append(expression.getValue()).append(quoteType);

    return true;
  }

  public boolean appendEscapedValue(EscapedValue escaped) {
    builder.append(escaped.getValue());

    return true;
  }

  public boolean appendIdentifierExpression(IdentifierExpression expression) {
    builder.append(expression.getValue());

    return true;
  }

  protected boolean appendColorExpression(ColorExpression expression) {
    // if it is named color expression, write out the name
    if (expression instanceof NamedColorExpression) {
      NamedColorExpression named = (NamedColorExpression) expression;
      builder.append(named.getColorName());
    } else {
      builder.append(expression.getValue());
    }

    return true;
  }

  private boolean appendFunctionExpression(FunctionExpression node) {
    builder.append(node.getName());
    builder.append("(");
    append(node.getParameter());
    builder.append(")");

    return true;
  }

  private boolean appendNumberExpression(NumberExpression node) {
    if (node.hasOriginalString()) {
      builder.append(node.getOriginalString());
    } else {
      if (node.hasExpliciteSign()) {
        if (0 < node.getValueAsDouble())
          builder.append("+");
        else
          builder.append("-");
      }
      builder.append(format(node.getValueAsDouble()) + node.getSuffix());
    }

    return true;
  }

  public void appendSelectors(List<Selector> selectors) {
    Iterator<Selector> iterator = selectors.iterator();
    while (iterator.hasNext()) {
      Selector selector = iterator.next();
      append(selector);

      if (iterator.hasNext())
        builder.append(",").newLine();
    }
  }

  public boolean appendSelector(Selector selector) {
    if (selector.hasLeadingCombinator())
      append(selector.getLeadingCombinator());

    if (!selector.isCombined()) {
      append(selector.getHead());
      return true;
    }
    append(selector.getHead());
    append(selector.getRight());
    return true;
  }

  private boolean appendSimpleSelector(SimpleSelector selector) {
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
    builder.append(".").append(cssClass.getName());
    return true;
  }

  private void appendSimpleSelectorHead(SimpleSelector selector) {
    builder.ensureSeparator();
    if (selector.isStar()) {
      if (!selector.isEmptyForm())
        builder.append("*");
    } else {
      builder.appendIgnoreNull(selector.getElementName().getName());
    }

  }

  public boolean appendSelectorCombinator(SelectorCombinator combinator) {
    SelectorCombinator.Combinator realCombinator = combinator.getCombinator();
    switch (realCombinator) {
    case DESCENDANT:
      builder.ensureSeparator();
      break;

    default:
      builder.ensureSeparator().append(realCombinator.getSymbol());

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
        builder.ensureNewLine();
    }
  }

  private String format(Double valueAsDouble) {
    if (valueAsDouble.isNaN())
      return "NaN";
    
    return FORMATTER.format(valueAsDouble);
  }

  public String toString() {
    return builder.toString();
  }

}
