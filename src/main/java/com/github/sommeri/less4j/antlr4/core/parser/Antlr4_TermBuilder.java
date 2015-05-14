package com.github.sommeri.less4j.antlr4.core.parser;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FaultyExpression;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.IndirectVariable;
import com.github.sommeri.less4j.core.ast.NamedColorExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.SignedExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression.Dimension;
import com.github.sommeri.less4j.core.ast.UnicodeRangeExpression;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.parser.ConversionUtils;
import com.github.sommeri.less4j.core.parser.LessG4Lexer;
import com.github.sommeri.less4j.core.parser.LessG4Parser.PlusOrMinusContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.TermContext;
import com.github.sommeri.less4j.core.problems.BugHappened;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class Antlr4_TermBuilder {

  private final Antlr4_ASTBuilderSwitch parentBuilder;
  private ProblemsHandler problemsHandler;

  public Antlr4_TermBuilder(Antlr4_ASTBuilderSwitch astBuilderSwitch, ProblemsHandler problemsHandler) {
    this.parentBuilder = astBuilderSwitch;
    this.problemsHandler = problemsHandler;
  }

  public Expression buildFromTerm(ParseTree token) {
    if (token instanceof TerminalNode) {
      Token symbol = ((TerminalNode) token).getSymbol();

      switch (symbol.getType()) {
      case LessG4Lexer.IDENT:
      case LessG4Lexer.PERCENT:
        return identOrPercent(token, token);

      case LessG4Lexer.STRING:
        return buildFromString(token, token);

      case LessG4Lexer.HASH:
        return buildFromColorHash(token, token);

      case LessG4Lexer.NUMBER:
      case LessG4Lexer.PERCENTAGE:
      case LessG4Lexer.UNKNOWN_DIMENSION:
      case LessG4Lexer.REPEATER:
      case LessG4Lexer.LENGTH:
      case LessG4Lexer.EMS:
      case LessG4Lexer.EXS:
      case LessG4Lexer.ANGLE:
      case LessG4Lexer.TIME:
      case LessG4Lexer.FREQ:
        return buildFromNumber(token, token);

      case LessG4Lexer.AT_NAME:
        return buildFromVariable(token, token);

      case LessG4Lexer.INDIRECT_VARIABLE:
        return buildFromIndirectVariable(token, token);

      case LessG4Lexer.UNICODE_RANGE:
        return buildFromUnicodeRange(token, token);

      default:
        //FIXME (antlr4) (error)
        //throw new BugHappened("type number: " + PrintUtils.toName(offsetChild.getGeneralType()) + "(" + offsetChild.getGeneralType() + ") for " + offsetChild.getText(), offsetChild);
        throw new BugHappened("!!!!!!!!!!!!!!!!!!!!!!", new HiddenTokenAwareTreeAdapter(token));

      }

    } else if (token instanceof ParserRuleContext) {
      //System.out.println(" ------- " + token.getClass());
      ASTCssNode result = token.accept(parentBuilder);
      if (!(result instanceof Expression)) {
        throw new BugHappened("Term child should have been expression.", new HiddenTokenAwareTreeAdapter(((ParserRuleContext) token).start));
      }
      return (Expression) result;
    } else {
      throw new BugHappened("Unknown parse tree type", (com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree) null);
    }
  }

  public Expression buildFromSignedTerm(PlusOrMinusContext sign, ParseTree value) {
    Expression expression = buildFromTerm(value);
    return sign == null ? expression : negate(sign, expression);
  }

  private Expression negate(PlusOrMinusContext sign, Expression value) {
    if (value instanceof NumberExpression) {
      NumberExpression number = (NumberExpression) value;
      number.setExpliciteSign(true);

      if (sign.MINUS() != null) {
        number.negate();
        number.setOriginalString("-" + number.getOriginalString());
      } else if (sign.PLUS() != null) {
        number.setOriginalString("+" + number.getOriginalString());
      }

      return number;
    }

    if (sign.MINUS() != null) {
      return new SignedExpression(new HiddenTokenAwareTreeAdapter(sign), SignedExpression.Sign.MINUS, value);
    }

    return new SignedExpression(new HiddenTokenAwareTreeAdapter(sign), SignedExpression.Sign.PLUS, value);
  }

  private Expression buildFromColorHash(ParseTree token, ParseTree first) {
    String text = first.getText();
    ColorExpression parsedColor = ConversionUtils.parseColor(toToken(token), text);
    if (parsedColor == null) {
      FaultyExpression faultyExpression = new FaultyExpression(toToken(token));
      problemsHandler.notAColor(faultyExpression, text);
      return faultyExpression;
    }

    return parsedColor;
  }

  private NumberExpression buildFromNumber(ParseTree token, ParseTree actual) {
    NumberExpression result = new NumberExpression(toToken(token));
    String valueAsString = actual.getText().trim();
    setDoubleAndType(result, valueAsString);
    result.setOriginalString(valueAsString);
    if (actual instanceof TerminalNode) {
      result.setDimension(toDimension((TerminalNode) actual));
    } else {
      //FIXME: (antlr4) (error)
      throw new BugHappened("what to do nere", new HiddenTokenAwareTreeAdapter(actual));
    }
    return result;
  }

  private void setDoubleAndType(NumberExpression result, String value) {
    value = value.toLowerCase().trim();
    String numberPart = value.replaceAll("[^0-9\\.]*", "");
    result.setValueAsDouble(Double.valueOf(numberPart));
    if (numberPart.length() < value.length())
      result.setSuffix(value.substring(numberPart.length()));
    else
      result.setSuffix("");
  }

  private Dimension toDimension(TerminalNode actual) {
    switch (actual.getSymbol().getType()) {
    case LessG4Lexer.NUMBER:
      return Dimension.NUMBER;
    case LessG4Lexer.PERCENTAGE:
      return Dimension.PERCENTAGE;
    case LessG4Lexer.UNKNOWN_DIMENSION:
      return Dimension.UNKNOWN;
    case LessG4Lexer.REPEATER:
      return Dimension.REPEATER;
    case LessG4Lexer.LENGTH:
      return Dimension.LENGTH;
    case LessG4Lexer.EMS:
      return Dimension.EMS;
    case LessG4Lexer.EXS:
      return Dimension.EXS;
    case LessG4Lexer.ANGLE:
      return Dimension.ANGLE;
    case LessG4Lexer.TIME:
      return Dimension.TIME;
    case LessG4Lexer.FREQ:
      return Dimension.FREQ;

    default:
      //FIXME (antlr4) (error)
      //throw new BugHappened("Unknown dimension type: " + actual.getGeneralType() + " " + actual.getText(), actual);
      throw new BugHappened("Unknown dimension type: " + actual.getText(), new HiddenTokenAwareTreeAdapter(actual));

    }
  }

  private Expression buildFromString(ParseTree token, ParseTree first) {
    String text = first.getText();
    return createCssString(token, text);
  }

  public CssString createCssString(ParseTree token, String quotedText) {
    return new CssString(toToken(token), quotedText.substring(1, quotedText.length() - 1), quotedText.substring(0, 1));
  }

  private Expression identOrPercent(ParseTree parent, ParseTree first) {
    String text = first.getText();
    return createIdentifierExpression(parent, text);
  }

  public Expression createIdentifierExpression(ParseTree parent, String text) {
    if (NamedColorExpression.isColorName(text))
      return NamedColorExpression.createNamedColorExpression(toToken(parent), text);

    return new IdentifierExpression(toToken(parent), text);
  }

  private Expression buildFromUnicodeRange(ParseTree parent, ParseTree first) {
    return new UnicodeRangeExpression(toToken(parent), first.getText());
  }

  public Variable buildFromVariable(ParseTree variableToken) {
    return buildFromVariable(null, variableToken);
  }

  private Variable buildFromVariable(ParseTree realOwner, ParseTree variableToken) {
    if (realOwner != null) {
      //FIXME: (antlr4) (comments)
      //realOwner.addFollowing(variableToken.getFollowing());
      return new Variable(toToken(realOwner), variableToken.getText());
    }
    return new Variable(toToken(variableToken), variableToken.getText());
  }

  public IndirectVariable buildFromIndirectVariable(ParseTree variableToken) {
    return buildFromIndirectVariable(null, variableToken);
  }

  private IndirectVariable buildFromIndirectVariable(ParseTree expressionToken, ParseTree variableToken) {
    if (expressionToken != null) {
      //FIXME: (antlr4) (comments)
      //expressionToken.addFollowing(variableToken.getFollowing());
      return new IndirectVariable(toToken(expressionToken), variableToken.getText().substring(1));
    }
    return new IndirectVariable(toToken(variableToken), variableToken.getText().substring(1));
  }

  private HiddenTokenAwareTreeAdapter toToken(ParseTree token) {
    return new HiddenTokenAwareTreeAdapter(token);
  }

}
