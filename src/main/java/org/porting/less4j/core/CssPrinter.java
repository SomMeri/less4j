package org.porting.less4j.core;

import java.util.Iterator;
import java.util.List;

import org.porting.less4j.ILessCompiler;
import org.porting.less4j.core.ast.ASTCssNode;
import org.porting.less4j.core.ast.CharsetDeclaration;
import org.porting.less4j.core.ast.ColorExpression;
import org.porting.less4j.core.ast.Comment;
import org.porting.less4j.core.ast.ComposedExpression;
import org.porting.less4j.core.ast.CssClass;
import org.porting.less4j.core.ast.CssString;
import org.porting.less4j.core.ast.Declaration;
import org.porting.less4j.core.ast.DeclarationsBody;
import org.porting.less4j.core.ast.ExpressionOperator;
import org.porting.less4j.core.ast.FontFace;
import org.porting.less4j.core.ast.FunctionExpression;
import org.porting.less4j.core.ast.IdSelector;
import org.porting.less4j.core.ast.IdentifierExpression;
import org.porting.less4j.core.ast.Media;
import org.porting.less4j.core.ast.MediaExpression;
import org.porting.less4j.core.ast.MediaQuery;
import org.porting.less4j.core.ast.Medium;
import org.porting.less4j.core.ast.Medium.MediumModifier;
import org.porting.less4j.core.ast.NamedColorExpression;
import org.porting.less4j.core.ast.NamedExpression;
import org.porting.less4j.core.ast.Nth;
import org.porting.less4j.core.ast.NumberExpression;
import org.porting.less4j.core.ast.PseudoClass;
import org.porting.less4j.core.ast.PseudoElement;
import org.porting.less4j.core.ast.RuleSet;
import org.porting.less4j.core.ast.Selector;
import org.porting.less4j.core.ast.SelectorAttribute;
import org.porting.less4j.core.ast.SelectorCombinator;
import org.porting.less4j.core.ast.SelectorOperator;
import org.porting.less4j.core.ast.SimpleSelector;
import org.porting.less4j.core.ast.StyleSheet;
import org.porting.less4j.core.parser.ANTLRParser;
import org.porting.less4j.core.parser.ASTBuilder;


// FIXME document: not matching spaces especially around terms expressions and
// comments
public class CssPrinter implements ILessCompiler {
  private ANTLRParser parser = new ANTLRParser();
  private ASTBuilder astBuilder = new ASTBuilder();

  @Override
  public String compile(String content) {
    // FIXME: ugly, clean hierarchy and dependencies
    ExtendedStringBuilder stringBuilder = new ExtendedStringBuilder("");

    ANTLRParser.ParseResult result = parser.parseStyleSheet(content);
    StyleSheet styleSheet = astBuilder.parse(result.getTree());
    Builder builder = new Builder(stringBuilder);
    builder.append(styleSheet);
    return stringBuilder.toString();
  };

}

class Builder {

  private final ExtendedStringBuilder builder;

  public Builder(ExtendedStringBuilder builder) {
    super();
    this.builder = builder;
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
    if (node==null)
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
      return appendDeclarationsBody((DeclarationsBody) node);

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

    case MEDIA_EXPRESSION:
      return appendMediaExpression((MediaExpression) node);

    case STYLE_SHEET:
      return appendStyleSheet((StyleSheet) node);

    default:
      throw new IllegalStateException("Unknown: " + node.getType());
    }
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

  private void appendComments(List<Comment> comments, boolean ensureSeparator) {
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
    switch (operator.getOperator()) {
    case NONE:
      break;

    case EQUALS:
      builder.append("=");
      break;

    case INCLUDES:
      builder.append("~=");
      break;

    case SPECIAL_PREFIX:
      builder.append("|=");
      break;

    case PREFIXMATCH:
      builder.append("^=");
      break;

    case SUFFIXMATCH:
      builder.append("$=");
      break;

    case SUBSTRINGMATCH:
      builder.append("*=");
      break;

    default:
      throw new IllegalStateException("Unknown: " + operator);
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

  public boolean appendDeclarationsBody(DeclarationsBody body) {
    if (body.isEmpty())
      return false;

    builder.ensureSeparator().append("{").newLine();
    builder.increaseIndentationLevel();
    Iterator<Declaration> iterator = body.getChilds().iterator();
    while (iterator.hasNext()) {
      Declaration declaration = iterator.next();
      append(declaration);
      builder.ensureNewLine();
    }
    appendComments(body.getOrphanComments(), false);
    builder.decreaseIndentationLevel();
    builder.append("}");

    return true;
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
    builder.ensureSeparator().append("{").newLine();
    builder.increaseIndentationLevel();

    Iterator<ASTCssNode> declarations = node.getDeclarations().iterator();
    List<ASTCssNode> ruleSets = node.getRuleSets();
    while (declarations.hasNext()) {
      ASTCssNode body = declarations.next();
      append(body);
      if (declarations.hasNext() || ruleSets.isEmpty())
        builder.ensureNewLine();
    }
    for (ASTCssNode body : ruleSets) {
      append(body);
      builder.ensureNewLine();
    }
    builder.decreaseIndentationLevel();
    builder.append("}");

    return true;
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

  //FIXME: comments will not work!!!
  public boolean appendMedium(Medium medium) {
    MediumModifier modifier = medium.getModifier();
    switch (modifier) {
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
    
    String mediumType = medium.getMediumType();
    builder.ensureSeparator().append(mediumType);
    
    return true;
  }

  public boolean appendMediaExpression(MediaExpression expression) {
    builder.ensureSeparator().append("(");
    builder.append(expression.getFeature());
    if (expression.getExpression()!=null) {
      builder.append(":").ensureSeparator();
      append(expression.getExpression());
    }
    builder.append(")");
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
    switch (operator.getOperator()) {
    case COMMA:
      builder.append(",").ensureSeparator();
      break;

    case SOLIDUS:
      builder.append("/");
      break;

    case STAR:
      builder.ensureSeparator().append("*");
      break;

    case EMPTY_OPERATOR:
      builder.ensureSeparator();
      break;

    default:
      throw new IllegalStateException("Unknown: " + operator);
    }

    return true;
  }

  public boolean appendCssString(CssString expression) {
    builder.append(expression.getValue());

    return true;
  }

  public boolean appendIdentifierExpression(IdentifierExpression expression) {
    builder.append(expression.getValue());

    return true;
  }

  private boolean appendColorExpression(ColorExpression expression) {
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
    switch (node.getSign()) {
    case PLUS:
      builder.append("+");
      break;

    case MINUS:
      builder.append("-");
      break;
    }

    builder.append(node.getValueAsString());

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
    if (!selector.isCombined()) {
      append(selector.getHead());
      return true;
    }
    append(selector.getHead());
    append(selector.getCombinator());
    append(selector.getRight());
    return true;
  }

  private boolean appendSimpleSelector(SimpleSelector selector) {
    appendSimpleSelectorHead(selector);
    appendSimpleSelectorTail(selector);
    return true;
  }

  private void appendSimpleSelectorTail(SimpleSelector selector) {
    List<ASTCssNode> allChilds = selector.getSubsequent();
    for (ASTCssNode astCssNode : allChilds) {
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
      builder.appendIgnoreNull(selector.getElementName());
    }

  }

  public boolean appendSelectorCombinator(SelectorCombinator combinator) {
    switch (combinator.getCombinator()) {
    case ADJACENT_SIBLING:
      builder.ensureSeparator().append("+");
      break;

    case CHILD:
      builder.ensureSeparator().append(">");
      break;

    case GENERAL_SIBLING:
      builder.ensureSeparator().append("~");
      break;

    case DESCENDANT:
      builder.ensureSeparator();
      break;

    }

    return true;
  }

  private void appendAllChilds(ASTCssNode node) {
    List<? extends ASTCssNode> allChilds = node.getChilds();
    for (ASTCssNode kid : allChilds) {
      if (append(kid))
        builder.ensureNewLine();
    }
  }

}
