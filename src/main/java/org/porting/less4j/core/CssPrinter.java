package org.porting.less4j.core;

import java.util.Iterator;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.porting.less4j.ILessCompiler;
import org.porting.less4j.core.ast.ASTCssNode;
import org.porting.less4j.core.ast.CharsetDeclaration;
import org.porting.less4j.core.ast.ColorExpression;
import org.porting.less4j.core.ast.ComposedExpression;
import org.porting.less4j.core.ast.CssClass;
import org.porting.less4j.core.ast.CssString;
import org.porting.less4j.core.ast.Declaration;
import org.porting.less4j.core.ast.FontFace;
import org.porting.less4j.core.ast.FunctionExpression;
import org.porting.less4j.core.ast.IdSelector;
import org.porting.less4j.core.ast.IdentifierExpression;
import org.porting.less4j.core.ast.Media;
import org.porting.less4j.core.ast.Medium;
import org.porting.less4j.core.ast.NumberExpression;
import org.porting.less4j.core.ast.Pseudo;
import org.porting.less4j.core.ast.RuleSet;
import org.porting.less4j.core.ast.Selector;
import org.porting.less4j.core.ast.SelectorAttribute;
import org.porting.less4j.core.ast.SimpleSelector;
import org.porting.less4j.core.ast.StyleSheet;
import org.porting.less4j.core.parser.ANTLRParser;
import org.porting.less4j.core.parser.ASTBuilder;

public class CssPrinter implements ILessCompiler {
  private ANTLRParser parser = new ANTLRParser();
  private ASTBuilder astBuilder = new ASTBuilder();

  @Override
  public String compile(String content) {
    // FIXME: ugly
    ExtendedStringBuilder stringBuilder = new ExtendedStringBuilder("");

    CommonTree ast = parser.parse(content);
    StyleSheet styleSheet = astBuilder.parse(ast);
    Builder builder = new Builder(stringBuilder);
    builder.appendStyleSheet(styleSheet);
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
    switch (node.getType()) {
    case RULE_SET:
      return appendRuleset((RuleSet) node);

    case CSS_CLASS:
      return appendCssClass((CssClass) node);

    case PSEUDO:
      return appendPseudo((Pseudo) node);

    case SELECTOR_ATTRIBUTE:
      return appendSelectorAttribute((SelectorAttribute) node);

    case ID_SELECTOR:
      return appendIdSelector((IdSelector) node);

    case CHARSET_DECLARATION:
      return appendCharsetDeclaration((CharsetDeclaration) node);

    case FONT_FACE:
      return appendFontFace((FontFace) node);

    case COMPOSED_EXPRESSION:
      return appendComposedExpression((ComposedExpression) node);

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

    case MEDIUM:
      return appendMedium((Medium) node);

    default:
      throw new IllegalStateException("Unknown: " + node.getType());
    }

  }

  // FIXME: does less.js keeps original cases in charsets and elsewhere or not?
  // I'm converting it all to
  // lowercases
  public boolean appendFontFace(FontFace node) {
    builder.append("@font-face {").newLine();
    builder.increaseIndentationLevel();
    appendAllChilds(node);
    builder.decreaseIndentationLevel();
    builder.append("}");

    return true;
  }

  // FIXME: does less.js keeps original cases in charsets and elsewhere or not?
  // I'm converting it all to
  // lowercases
  public boolean appendCharsetDeclaration(CharsetDeclaration node) {
    builder.append("@charset ");
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
    appendSelectorOperator(node.getOperator());
    builder.appendIgnoreNull(node.getValue());
    builder.append("]");

    return true;
  }

  public void appendSelectorOperator(SelectorAttribute.Operator operator) {
    switch (operator) {
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
  }

  public boolean appendPseudo(Pseudo node) {
    builder.append(":");
    builder.append(node.getName());
    if (node.hasParameters()) {
      builder.append(node.getParameter());
    }

    return true;
  }

  public void appendStyleSheet(StyleSheet styleSheet) {
    appendAllChilds(styleSheet);
  }

  // TODO test with empty selector e.g.:
  // 1.) "something, , else"
  // 2.) "something, , ,"
  // 3.) "" - what happen is some rule set has no selector?
  public boolean appendRuleset(RuleSet ruleSet) {
    if (ruleSet.hasEmptyBody())
      return false;

    appendSelectors(ruleSet.getSelectors());
    builder.append(" {").newLine();
    builder.increaseIndentationLevel();
    Iterator<Declaration> iterator = ruleSet.getDeclarations().iterator();
    while (iterator.hasNext()) {
      Declaration declaration = iterator.next();
      append(declaration);
      builder.newLine();
    }
    builder.decreaseIndentationLevel();
    builder.append("}");

    return true;
  }

  public boolean appendDeclaration(Declaration declaration) {
    builder.appendIgnoreNull(declaration.getName());
    builder.appendIgnoreNull(": ");
    if (declaration.getExpression() != null)
      append(declaration.getExpression());
    // FIXME: zdokumentontovat: less.js prints important as it was, e.g. it may
    // not have leading space or it may be ! important
    if (declaration.isImportant())
      builder.appendIgnoreNull(" !important");
    builder.appendIgnoreNull(";");

    return true;
  }

  public boolean appendMedia(Media node) {
    builder.append("@media ");
    appendMedium(node.getMedium());
    builder.append(" {").newLine();
    builder.increaseIndentationLevel();
    //FIXME: DOCUMENTATION less.js reorders statements in @media. It prints declarations first and rulesets second 
    //FIXME: DOCUMENTATION no new line for the last declaration 
    //FIXME: DOCUMENTATION media-simple.txt 
    Iterator<ASTCssNode> declarations = node.getDeclarations().iterator();
    List<ASTCssNode> ruleSets = node.getRuleSets();
    while (declarations.hasNext()) {
      ASTCssNode body = declarations.next();
      append(body);
      if (declarations.hasNext() || ruleSets.isEmpty())
        builder.newLine();
    }
    for (ASTCssNode body : ruleSets) {
      append(body);
      builder.newLine();
    }
    builder.decreaseIndentationLevel();
    builder.append("}");

    return true;
  }

  public boolean appendMedium(Medium medium) {
    Iterator<String> iterator = medium.getMediums().iterator();
    if (!iterator.hasNext())
      return false;
    builder.append(iterator.next());
    while (iterator.hasNext()) {
      builder.append(", ").append(iterator.next());
    }

    return true;
  }

  public boolean appendComposedExpression(ComposedExpression expression) {
    append(expression.getLeft());
    appendExpressionOperator(expression.getOperator());
    append(expression.getRight());

    return true;
  }

  public void appendExpressionOperator(ComposedExpression.Operator operator) {
    switch (operator) {
    case COMMA:
      builder.append(", ");
      break;

    case SOLIDUS:
      builder.append("/");
      break;

    case STAR:
      builder.append(" * ");
      break;

    case EMPTY_OPERATOR:
      builder.append(" ");
      break;

    default:
      throw new IllegalStateException("Unknown: " + operator);
    }
  }

  public boolean appendCssString(CssString expression) {
    builder.append(expression.getValue());

    return true;
  }

  public boolean appendIdentifierExpression(IdentifierExpression expression) {
    builder.append(expression.getName());

    return true;
  }

  private boolean appendColorExpression(ColorExpression expression) {
    builder.append(expression.getValue());

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

    // FIXME: why does the number contain also the next space????
    builder.append(node.getValueAsString().trim());

    return true;
  }

  public void appendSelectors(List<Selector> selectors) {
    Iterator<Selector> iterator = selectors.iterator();
    while (iterator.hasNext()) {
      Selector selector = iterator.next();
      appendSelector(selector);

      if (iterator.hasNext())
        builder.append(",").newLine();
    }
  }

  public void appendSelector(Selector selector) {
    if (!selector.isCombined()) {
      appendSimpleSelector(selector.getHead());
      return;
    }
    appendSimpleSelector(selector.getHead());
    appendCombinator(selector.getCombinator());
    appendSelector(selector.getRight());
  }

  // TODO add test to all cases
  private void appendSimpleSelector(SimpleSelector selector) {
    appendSimpleSelectorHead(selector);
    appendSimpleSelectorTail(selector);
  }

  private void appendSimpleSelectorTail(SimpleSelector selector) {
    // FIXME: here I assume that only CSS classes are possible
    List<ASTCssNode> allChilds = selector.getSubsequent();
    for (ASTCssNode astCssNode : allChilds) {
      append(astCssNode);
    }
  }

  private boolean appendCssClass(CssClass cssClass) {
    builder.append(".").append(cssClass.getName());
    return true;
  }

  public void appendSimpleSelectorHead(SimpleSelector selector) {
    if (selector.isStar()) {
      builder.append("*");
    } else {
      builder.appendIgnoreNull(selector.getElementName());
    }

  }

  // TODO add test on plus greated and empty!!!!!!
  public void appendCombinator(Selector.Combinator combinator) {
    switch (combinator) {
    case PLUS:
      builder.append(" + ");
      break;

    case GREATER:
      builder.append(" > ");
      break;

    case EMPTY:
      builder.append(" ");
      break;

    }

  }

  private void appendAllChilds(ASTCssNode node) {
    List<? extends ASTCssNode> allChilds = node.getChilds();
    for (ASTCssNode kid : allChilds) {
      if (append(kid))
        builder.newLine();
    }
  }

}
