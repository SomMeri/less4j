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
import org.porting.less4j.core.ast.NumberExpression;
import org.porting.less4j.core.ast.Pseudo;
import org.porting.less4j.core.ast.RuleSet;
import org.porting.less4j.core.ast.Selector;
import org.porting.less4j.core.ast.SelectorAttribute;
import org.porting.less4j.core.ast.SimpleSelector;
import org.porting.less4j.core.ast.StyleSheet;
import org.porting.less4j.core.parser.ANTLRParser;
import org.porting.less4j.core.parser.ASTBuilder;
import org.porting.less4j.platform.Constants;

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

  public void append(ASTCssNode node) {
    switch (node.getType()) {
    case RULE_SET:
      appendRuleset((RuleSet) node);
      break;

    case CSS_CLASS:
      appendCssClass((CssClass) node);
      break;

    case PSEUDO:
      appendPseudo((Pseudo) node);
      break;

    case SELECTOR_ATTRIBUTE:
      appendSelectorAttribute((SelectorAttribute) node);
      break;

    case ID_SELECTOR:
      appendIdSelector((IdSelector) node);
      break;

    case CHARSET_DECLARATION:
      appendCharsetDeclaration((CharsetDeclaration) node);
      break;

    case FONT_FACE:
      appendFontFace((FontFace) node);
      break;

    case COMPOSED_EXPRESSION:
      appendComposedExpression((ComposedExpression) node);
      break;

    case STRING_EXPRESSION:
      appendCssString((CssString) node);
      break;

    case NUMBER:
      appendNumberExpression((NumberExpression) node);
      break;

    case IDENTIFIER_EXPRESSION:
      appendIdentifierExpression((IdentifierExpression) node);
      break;

    case COLOR_EXPRESSION:
      appendColorExpression((ColorExpression) node);
      break;

    case FUNCTION:
      appendFunctionExpression((FunctionExpression) node);
      break;

    case DECLARATION:
      appendDeclaration((Declaration) node);
      break;

    default:
      throw new IllegalStateException("Unknown: " + node.getType());
    }

  }

  // FIXME: does less.js keeps original cases in charsets and elsewhere or not?
  // I'm converting it all to
  // lowercases
  public void appendFontFace(FontFace node) {
    builder.append("@font-face {").newLine();
    appendAllChilds(node, Constants.NEW_LINE);
    builder.append("}").newLine();
  }

  // FIXME: does less.js keeps original cases in charsets and elsewhere or not?
  // I'm converting it all to
  // lowercases
  public void appendCharsetDeclaration(CharsetDeclaration node) {
    builder.append("@charset ");
    builder.append(node.getCharset());
    builder.append(";").newLine();
  }

  public void appendIdSelector(IdSelector node) {
    builder.append("#");
    builder.append(node.getName());
  }

  public void appendSelectorAttribute(SelectorAttribute node) {
    builder.append("[");
    builder.append(node.getName());
    appendSelectorOperator(node.getOperator());
    builder.appendIgnoreNull(node.getValue());
    builder.append("]");
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

  public void appendPseudo(Pseudo node) {
    builder.append(":");
    builder.append(node.getName());
    if (node.hasParameters()) {
      builder.append(node.getParameter());
    }
  }

  public void appendStyleSheet(StyleSheet styleSheet) {
    appendAllChilds(styleSheet);
  }

  // TODO test with empty selector e.g.:
  // 1.) "something, , else"
  // 2.) "something, , ,"
  // 3.) "" - what happen is some rule set has no selector?
  public void appendRuleset(RuleSet ruleSet) {
    if (ruleSet.hasEmptyBody())
      return;

    appendSelectors(ruleSet.getSelectors());
    builder.append(" {").newLine();
    Iterator<Declaration> iterator = ruleSet.getDeclarations().iterator();
    while (iterator.hasNext()) {
      Declaration declaration = iterator.next();
      append(declaration);
      builder.newLine();
    }
    builder.append("}").newLine();
  }

  public void appendDeclaration(Declaration declaration) {
    builder.appendIgnoreNull("  ");
    builder.appendIgnoreNull(declaration.getName());
    builder.appendIgnoreNull(": ");
    if (declaration.getExpression() != null)
      append(declaration.getExpression());
    // FIXME: zdokumentontovat: less.js prints important as it was, e.g. it may
    // not have leading space or it may be ! important
    if (declaration.isImportant())
      builder.appendIgnoreNull(" !important");
    builder.appendIgnoreNull(";");
  }

  public void appendComposedExpression(ComposedExpression expression) {
    append(expression.getLeft());
    appendExpressionOperator(expression.getOperator());
    append(expression.getRight());
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

  public void appendCssString(CssString expression) {
    builder.append(expression.getValue());
  }

  public void appendIdentifierExpression(IdentifierExpression expression) {
    builder.append(expression.getName());
  }

  private void appendColorExpression(ColorExpression expression) {
    builder.append(expression.getValue());
  }

  private void appendFunctionExpression(FunctionExpression node) {
    builder.append(node.getName());
    builder.append("(");
    append(node.getParameter());
    builder.append(")");
  }

  private void appendNumberExpression(NumberExpression node) {
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

  private void appendCssClass(CssClass cssClass) {
    builder.append(".").append(cssClass.getName());
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
    appendAllChilds(node, null);
  }

  private void appendAllChilds(ASTCssNode node, String delimiter) {
    List<? extends ASTCssNode> allChilds = node.getChilds();
    for (ASTCssNode kid : allChilds) {
      append(kid);
      builder.appendIgnoreNull(delimiter);
    }
  }

}
