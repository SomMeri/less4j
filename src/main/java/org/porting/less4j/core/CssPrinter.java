package org.porting.less4j.core;

import java.util.Iterator;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.porting.less4j.ILessCompiler;
import org.porting.less4j.core.ast.ASTCssNode;
import org.porting.less4j.core.ast.Combinator;
import org.porting.less4j.core.ast.CssClass;
import org.porting.less4j.core.ast.Declaration;
import org.porting.less4j.core.ast.Expression;
import org.porting.less4j.core.ast.IdSelector;
import org.porting.less4j.core.ast.Pseudo;
import org.porting.less4j.core.ast.RuleSet;
import org.porting.less4j.core.ast.Selector;
import org.porting.less4j.core.ast.SelectorAttribute;
import org.porting.less4j.core.ast.SelectorAttribute.Operator;
import org.porting.less4j.core.ast.SimpleSelector;
import org.porting.less4j.core.ast.StyleSheet;
import org.porting.less4j.core.parser.ANTLRParser;
import org.porting.less4j.core.parser.ASTBuilder;
import org.porting.less4j.debugutils.DebugPrint;

public class CssPrinter implements ILessCompiler {
  private ANTLRParser parser = new ANTLRParser();
  private ASTBuilder astBuilder = new ASTBuilder(); 

  @Override
  public String compile(String content) {
    // FIXME: ugly
    ExtendedStringBuilder stringBuilder = new ExtendedStringBuilder("");

    CommonTree ast = parser.parse(content);
    StyleSheet styleSheet = astBuilder.parse(ast);
    DebugPrint.print(ast);
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
      appendRuleset((RuleSet)node);
      break;

    case CSS_CLASS:
      appendCssClass((CssClass)node);
      break;

    case PSEUDO:
      appendPseudo((Pseudo)node);
      break;

    case SELECTOR_ATTRIBUTE:
      appendSelectorAttribute((SelectorAttribute)node);
      break;

    case ID_SELECTOR:
      appendIdSelector((IdSelector)node);
      break;

    default:
      throw new IllegalStateException("Unknown");
    }
    
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

  public void appendSelectorOperator(Operator operator) {
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
      break;
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
    List<ASTCssNode> allChilds = styleSheet.getChilds();
    for (ASTCssNode node : allChilds) {
      append(node);
    }
  }

  // TODO test with empty selector e.g.:
  // 1.) "something, , else"
  // 2.) "something, , ,"
  // 3.) "" - what happen is some rule set has no selector?
  public void appendRuleset(RuleSet ruleSet) {
    if (ruleSet.hasEmptyBody())
      return;

    appendSelectors(ruleSet.getSelectors());
    builder.append(" {\n");
    Iterator<Declaration> iterator = ruleSet.getDeclarations().iterator();
    while (iterator.hasNext()) {
      Declaration declaration = iterator.next();
      appendDeclaration(declaration);
      builder.append("\n");
    }
    builder.append("}\n");
  }

  private void appendDeclaration(Declaration declaration) {
    builder.appendIgnoreNull("  ");
    builder.appendIgnoreNull(declaration.getName());
    builder.appendIgnoreNull(": ");
    appendExpression(declaration.getExpression());
    if(declaration.hasPriority())
      builder.appendIgnoreNull("!");
    builder.appendIgnoreNull(";");
  }

  private void appendExpression(Expression expression) {
    if (expression==null)
      return ;
    
    //FIXME: only temporary measure
    List<ASTCssNode> allChilds = expression.getChilds();
    for (ASTCssNode astCssNode : allChilds) {
      builder.append(astCssNode.getUnderlyingText());
    }
  }

  public void appendSelectors(List<Selector> selectors) {
    Iterator<Selector> iterator = selectors.iterator();
    while (iterator.hasNext()) {
      Selector selector = iterator.next();
      appendSelector(selector);

      if (iterator.hasNext())
        builder.append(",\n");
    }
  }

  public void appendSelector(Selector selector) {
    if (!selector.isCombined()) {
      appendSimpleSelector(selector.getHead());
      return ;
    }
    appendSimpleSelector(selector.getHead());
    appendCombinator(selector.getCombinator());
    appendSelector(selector.getRight());
  }

  //TODO add test to all cases
  private void appendSimpleSelector(SimpleSelector selector) {
    appendSimpleSelectorHead(selector);
    appendSimpleSelectorTail(selector);
  }

  private void appendSimpleSelectorTail(SimpleSelector selector) {
    //FIXME: here I assume that only CSS classes are possible
    List<ASTCssNode> allChilds = selector.getSubsequent();
    for (ASTCssNode astCssNode : allChilds) {
      append(astCssNode);
    }
  }

  private void appendCssClass(CssClass cssClass) {
    //TODO: things like this could be known by the class itself?
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
  public void appendCombinator(Combinator combinator) {
    switch (combinator) {
    case PLUS:
      builder.append("+");
      break;

    case GREATER:
      builder.append(" > ");
      break;

    case EMPTY:
      builder.append(" ");
      break;

    }

  }

}

