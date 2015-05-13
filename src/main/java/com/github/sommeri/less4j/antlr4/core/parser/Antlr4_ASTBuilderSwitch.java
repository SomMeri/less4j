package com.github.sommeri.less4j.antlr4.core.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.BinaryExpression;
import com.github.sommeri.less4j.core.ast.BinaryExpressionOperator;
import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.CssClass;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.Declaration;
import com.github.sommeri.less4j.core.ast.ElementSubsequent;
import com.github.sommeri.less4j.core.ast.EmbeddedScript;
import com.github.sommeri.less4j.core.ast.EmptyExpression;
import com.github.sommeri.less4j.core.ast.EscapedSelector;
import com.github.sommeri.less4j.core.ast.EscapedValue;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FaultyExpression;
import com.github.sommeri.less4j.core.ast.FixedNamePart;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.IdSelector;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.InterpolableName;
import com.github.sommeri.less4j.core.ast.InterpolableNamePart;
import com.github.sommeri.less4j.core.ast.KeywordExpression;
import com.github.sommeri.less4j.core.ast.ListExpression;
import com.github.sommeri.less4j.core.ast.ListExpressionOperator;
import com.github.sommeri.less4j.core.ast.ListExpressionOperator.Operator;
import com.github.sommeri.less4j.core.ast.NamedColorExpression;
import com.github.sommeri.less4j.core.ast.NamedExpression;
import com.github.sommeri.less4j.core.ast.NestedSelectorAppender;
import com.github.sommeri.less4j.core.ast.Nth;
import com.github.sommeri.less4j.core.ast.Nth.Form;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.ParenthesesExpression;
import com.github.sommeri.less4j.core.ast.PseudoClass;
import com.github.sommeri.less4j.core.ast.PseudoElement;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.ast.SelectorAttribute;
import com.github.sommeri.less4j.core.ast.SelectorCombinator;
import com.github.sommeri.less4j.core.ast.SelectorOperator;
import com.github.sommeri.less4j.core.ast.SelectorPart;
import com.github.sommeri.less4j.core.ast.SignedExpression;
import com.github.sommeri.less4j.core.ast.SimpleSelector;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.ast.SyntaxOnlyElement;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.ast.VariableDeclaration;
import com.github.sommeri.less4j.core.ast.VariableNamePart;
import com.github.sommeri.less4j.core.parser.ConversionUtils;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.parser.LessG4Lexer;
import com.github.sommeri.less4j.core.parser.LessG4Parser.AllNumberKindsContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.AttribContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.AttribOrPseudoContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.CombinatorContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Combinator_wsContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.CssClassContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.CssClassOrIdContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.DeclarationContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.DeclarationWithSemicolonContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.ElementNameContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.ElementNamePartContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.ElementSubsequentContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.EmbeddedScriptContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.EscapedSelectorOldSyntaxContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.EscapedValueContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Expr_in_parenthesesContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Expression_comma_separated_listContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Expression_fullContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Expression_space_separated_listContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.ExtendInDeclarationWithSemiContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.ExtendTargetSelectorsContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.FixedIdOrClassNamePartContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.FunctionContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.FunctionNameContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.FunctionParametersContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.General_bodyContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.General_body_memberContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.General_body_member_no_semiContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.HexColorContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.IdSelectorContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.IdentContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Ident_except_notContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Ident_general_pseudoContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Ident_keywordsContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Ident_nthContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Ident_special_pseudoclassesContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.IdentifierValueTermContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.IdentifierValueTermHelperContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Mandatory_wsContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.MathExprHighPriorNoWhitespaceListContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.MathExprHighPriorNoWhitespaceList_helperContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.MathExprLowPriorContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.MathOperatorHighPriorContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.NamedFunctionParameterContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.NestedAppenderContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Non_combinator_selector_blockContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.NthContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Nth_baseContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Nth_expressionContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.PlusOrMinusContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.PropertyContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.PropertyNamePartContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.PseudoContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Pseudoparameter_termValueContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.RuleSetContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.SelectorAttributeOperatorContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.SelectorContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.SimpleSelectorContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Special_functionContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.StyleSheetContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.TermContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Term_no_preceeding_whitespaceContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Term_only_functionContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Top_level_elementContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Unsigned_value_termContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Value_termContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.VariabledeclarationContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.VariabledeclarationWithSemicolonContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.VariablenameContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.VariablereferenceContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.WsContext;
import com.github.sommeri.less4j.core.parser.LessG4Visitor;
import com.github.sommeri.less4j.core.problems.BugHappened;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.platform.Constants;
import com.github.sommeri.less4j.utils.URIUtils;

public class Antlr4_ASTBuilderSwitch implements LessG4Visitor<ASTCssNode> {

  protected static final String GRAMMAR_MISMATCH = "ASTBuilderSwitch grammar mismatch";
  private static final String SHOULD_NOT_VISIT = "ASTBuilderSwitch should not visit this node";

  private static Set<String> COLONLESS_PSEUDOELEMENTS = new HashSet<String>();
  static {
    COLONLESS_PSEUDOELEMENTS.add("first-line");
    COLONLESS_PSEUDOELEMENTS.add("first-letter");
    COLONLESS_PSEUDOELEMENTS.add("before");
    COLONLESS_PSEUDOELEMENTS.add("after");
  }

  private final ProblemsHandler problemsHandler;
  private final Antlr4_TermBuilder termBuilder;

  protected Antlr4_ASTBuilderSwitch(ProblemsHandler problemsHandler) {
    this.problemsHandler = problemsHandler;
    this.termBuilder = new Antlr4_TermBuilder(this, problemsHandler);
  }

  @Override
  public ASTCssNode visitErrorNode(ErrorNode node) {
    // TODO Auto-generated method stub
    System.out.println("visitErrorNode(ErrorNode node)");
    return null;
  }

  @Override
  public ASTCssNode visitStyleSheet(StyleSheetContext ctx) {
    StyleSheet result = new StyleSheet(new HiddenTokenAwareTreeAdapter(ctx));
    if (ctx.getChildCount() == 0)
      return result;

    for (Top_level_elementContext child : ctx.top_level_element()) {
      result.addMember(child.accept(this));
    }

    return result;
  }

  @Override
  public ASTCssNode visitTop_level_element(Top_level_elementContext ctx) {
    if (ctx.getChildCount() != 1) {
      throw new BugHappened("Top_level_element should have only one child.", new HiddenTokenAwareTreeAdapter(ctx));
    }

    return ctx.getChild(0).accept(this);
  }

  @Override
  public ASTCssNode visitDeclaration(DeclarationContext ctx) {
    // FIXME (antlr4) (comments) push comments to used tokens
    TerminalNode colon = ctx.COLON();

    InterpolableName propertyName = visitProperty(ctx.property());
    ListExpressionOperator.Operator mergeOperator = toDeclarationMergeOperator(ctx);
    Expression expression = visitExpression_full(ctx.expression_full());

    Declaration declaration = new Declaration(new HiddenTokenAwareTreeAdapter(ctx.start), propertyName, expression, mergeOperator);
    return declaration;
  }

  @Override
  public ASTCssNode visitDeclarationWithSemicolon(DeclarationWithSemicolonContext ctx) {
    // FIXME (antlr4) (comments) push comments to used tokens
    TerminalNode semi = ctx.SEMI();

    return ctx.declaration().accept(this);
  }

  private ListExpressionOperator.Operator toDeclarationMergeOperator(DeclarationContext ctx) {
    if (ctx.PLUS() != null)
      return ListExpressionOperator.Operator.COMMA;
    if (ctx.UNDERSCORE() != null)
      return ListExpressionOperator.Operator.EMPTY_OPERATOR;

    return null;
  }

  @Override
  public InterpolableName visitProperty(PropertyContext ctx) {
    return toInterpolableName(ctx);
  }

  @Override
  public ASTCssNode visitPropertyNamePart(PropertyNamePartContext ctx) {
    throw new BugHappened(SHOULD_NOT_VISIT, new HiddenTokenAwareTreeAdapter(ctx));
  }

  private InterpolableName toInterpolableName(ParserRuleContext ctx) {
    return toInterpolableName(ctx, 0);
  }

  private InterpolableName toInterpolableName(ParserRuleContext ctx, int startIndex) {
    InterpolableName result = new InterpolableName(new HiddenTokenAwareTreeAdapter(ctx.start));
    for (int i = startIndex; i < ctx.getChildCount(); i++) {
      ParseTree child = ctx.getChild(i);
      InterpolableNamePart interpolableNamePart = toInterpolableNamePart(child);
      if (interpolableNamePart != null)
        result.add(interpolableNamePart);
    }
    return result;
  }

  /**
   * May return null! 
   */
  private InterpolableNamePart toInterpolableNamePart(ParseTree rule) {
    if (rule instanceof ParserRuleContext) {
      ParserRuleContext ruleContext = (ParserRuleContext) rule;
      if (1 != ruleContext.getChildCount()) {
        throw new BugHappened("Interpolable name part should have only one child.", new HiddenTokenAwareTreeAdapter((ParserRuleContext) rule));
      }
      return toInterpolableNamePart(rule.getChild(0));
    } else if (rule instanceof TerminalNode) {
      TerminalNode terminalNode = (TerminalNode) rule;
      Token token = terminalNode.getSymbol();
      HiddenTokenAwareTreeAdapter underlyingStructure = new HiddenTokenAwareTreeAdapter(token);
      String text = terminalNode.getText();
      if (token.getType() == LessG4Lexer.HASH)
        text = text.substring(1);

      if (token.getType() == LessG4Lexer.INTERPOLATED_VARIABLE) {
        return new VariableNamePart(underlyingStructure, toInterpolabledVariable(underlyingStructure, text));
      } else {
        if (token.getType() != LessG4Lexer.HASH_SYMBOL)
          return new FixedNamePart(underlyingStructure, text);
        else
          return null;
      }
    } else {
      throw new BugHappened("Unexpected class type in toInterpolableNamePart.", (HiddenTokenAwareTree) null);
    }
  }

  private Variable toInterpolabledVariable(ParseTree ctx, String text) {
    return toInterpolabledVariable(new HiddenTokenAwareTreeAdapter(ctx), text);
  }

  private Variable toInterpolabledVariable(HiddenTokenAwareTree token, String text) {
    return new Variable(token, "@" + text.substring(2, text.length() - 1), true);
  }

  // private InterpolableName toInterpolableName(HiddenTokenAwareTree token,
  // List<HiddenTokenAwareTree> children) {
  // InterpolableName result = new InterpolableName(token);
  // for (HiddenTokenAwareTree kid : children) {
  // String text = kid.getText();
  // if (text == null || text.length() < 1)
  // throw new BugHappened(GRAMMAR_MISMATCH, kid);
  //
  // if (kid.getGeneralType() == LessLexer.INTERPOLATED_VARIABLE) {
  // result.add(new VariableNamePart(kid, toInterpolabledVariable(kid, text)));
  // } else if (kid.getGeneralType() == LessLexer.HASH_SYMBOL) {
  // // do nothing
  // } else {
  // result.add(new FixedNamePart(kid, toFixedName(kid.getGeneralType(),
  // text)));
  // }
  // }
  // return result;
  // }

  @Override
  public Expression visitExpression_full(Expression_fullContext ctx) {
    return visitExpression_comma_separated_list(ctx.expression_comma_separated_list());
  }

  @Override
  public Expression visitExpression_comma_separated_list(Expression_comma_separated_listContext ctx) {
    return doVisitExpressionsList(ctx, ctx.expression_space_separated_list(), ListExpressionOperator.Operator.COMMA);
  }

  private Expression doVisitExpressionsList(ParseTree ctx, List<? extends ParseTree> original, Operator operator) {
    List<Expression> members = new ArrayList<Expression>();
    for (ParseTree memberO : original) {
      Expression memberN = (Expression) memberO.accept(this);
      members.add(memberN);
    }

    if (members.size() == 1)
      return members.get(0);

    ListExpression result = toListExpression(ctx, members, operator);
    return result;
  }

  @Override
  public Expression visitExpression_space_separated_list(Expression_space_separated_listContext ctx) {
    List<ParseTree> members = new ArrayList<ParseTree>();
    for (int i = 0; i < ctx.getChildCount(); i++) {
      ParseTree child = ctx.getChild(i);
      if ((child instanceof MathExprHighPriorNoWhitespaceListContext) || (child instanceof Term_no_preceeding_whitespaceContext)) {
        members.add(child);
      }
    }
    return doVisitExpressionsList(ctx, members, ListExpressionOperator.Operator.EMPTY_OPERATOR);
  }

  @Override
  public ASTCssNode visitWs(WsContext ctx) {
    throw new BugHappened(SHOULD_NOT_VISIT, new HiddenTokenAwareTreeAdapter(ctx));
  }

  @Override
  public ASTCssNode visitMandatory_ws(Mandatory_wsContext ctx) {
    throw new BugHappened(SHOULD_NOT_VISIT, new HiddenTokenAwareTreeAdapter(ctx));
  }

  @Override
  public ASTCssNode visitIdent_nth(Ident_nthContext ctx) {
    throw new BugHappened(SHOULD_NOT_VISIT, new HiddenTokenAwareTreeAdapter(ctx));
  }

  @Override
  public ASTCssNode visitIdent_keywords(Ident_keywordsContext ctx) {
    throw new BugHappened(SHOULD_NOT_VISIT, new HiddenTokenAwareTreeAdapter(ctx));
  }

  @Override
  public ASTCssNode visitIdent_special_pseudoclasses(Ident_special_pseudoclassesContext ctx) {
    // TODO Auto-generated method stub
    System.out.println("visitIdent_special_pseudoclasses(Ident_special_pseudoclassesContext ctx)");
    return null;
  }

  @Override
  public ASTCssNode visitIdent(IdentContext ctx) {
    //FIXME: not really nice, this threw SHOULD_NOT_VISIT exception normally
    //however I had to have it create expression cause term builder
    return createIdentifierExpression(ctx, ctx.getText());
  }

  @Override
  public ASTCssNode visitIdent_except_not(Ident_except_notContext ctx) {
    throw new BugHappened(SHOULD_NOT_VISIT, new HiddenTokenAwareTreeAdapter(ctx));
  }

  @Override
  public ASTCssNode visitIdent_general_pseudo(Ident_general_pseudoContext ctx) {
    throw new BugHappened(SHOULD_NOT_VISIT, new HiddenTokenAwareTreeAdapter(ctx));
  }

  @Override
  public ASTCssNode visit(ParseTree tree) {
    return tree.accept(this);
  }

  @Override
  public ASTCssNode visitChildren(RuleNode node) {
    throw new BugHappened(SHOULD_NOT_VISIT, new HiddenTokenAwareTreeAdapter(node));
  }

  @Override
  public ASTCssNode visitTerminal(TerminalNode node) {
    throw new BugHappened(SHOULD_NOT_VISIT, new HiddenTokenAwareTreeAdapter(node));
  }

  @Override
  public GeneralBody visitGeneral_body(General_bodyContext ctx) {
    // FIXME (antlr) (comments) heavy comments handling in original
    TerminalNode lbraceToken = ctx.LBRACE();
    SyntaxOnlyElement lbrace = new SyntaxOnlyElement(new HiddenTokenAwareTreeAdapter(lbraceToken.getSymbol()), lbraceToken.getText().trim());
    TerminalNode rbraceToken = ctx.RBRACE();
    SyntaxOnlyElement rbrace = new SyntaxOnlyElement(new HiddenTokenAwareTreeAdapter(rbraceToken.getSymbol()), rbraceToken.getText().trim());

    List<ASTCssNode> members = new ArrayList<ASTCssNode>();
    for (General_body_memberContext memberCtx : ctx.general_body_member()) {
      members.add(memberCtx.accept(this));
    }

    if (ctx.lastMember != null) {
      members.add(ctx.lastMember.accept(this));
    }

    GeneralBody result = new GeneralBody(new HiddenTokenAwareTreeAdapter(ctx), lbrace, rbrace, members);
    return result;
  }

  @Override
  public ASTCssNode visitGeneral_body_member(General_body_memberContext ctx) {
    if (ctx.getChildCount() != 1) {
      throw new BugHappened("Top_level_element should have only one child.", new HiddenTokenAwareTreeAdapter(ctx));
    }

    return ctx.getChild(0).accept(this);
  }

  @Override
  public ASTCssNode visitGeneral_body_member_no_semi(General_body_member_no_semiContext ctx) {
    if (ctx.getChildCount() != 1) {
      throw new BugHappened("Top_level_element should have only one child.", new HiddenTokenAwareTreeAdapter(ctx));
    }

    return ctx.getChild(0).accept(this);
  }

  @Override
  public ASTCssNode visitVariablereference(VariablereferenceContext ctx) {
    if (null != ctx.variablename())
      return ctx.variablename().accept(this);

    TerminalNode indirect = ctx.INDIRECT_VARIABLE();
    if (null != indirect) {
      // FIXME: (antlr4) used to be in term builder
      return new Variable(new HiddenTokenAwareTreeAdapter(indirect.getSymbol()), indirect.getText());
    }

    throw new BugHappened("Unexpected variable reference type.", new HiddenTokenAwareTreeAdapter(ctx));
  }

  @Override
  public Variable visitVariablename(VariablenameContext ctx) {
    // FIXME: (antlr4) used to be in term builder
    return new Variable(new HiddenTokenAwareTreeAdapter(ctx), ctx.getText());
  }

  @Override
  public ParenthesesExpression visitExpr_in_parentheses(Expr_in_parenthesesContext ctx) {
    // expr_in_parentheses: LPAREN ws expression_full ws RPAREN;
    // FIXME: (antlr4) (comments) - deal with this
    // first.addBeforeFollowing(first.getLastChild().getFollowing());
    Expression nestedExpression = visitExpression_full(ctx.expression_full());
    return new ParenthesesExpression(new HiddenTokenAwareTreeAdapter(ctx), nestedExpression);
  }

  @Override
  public EscapedValue visitEscapedValue(EscapedValueContext ctx) {
    // FIXME: (antlr4) (comments) - deal with this
    //    token.pushHiddenToKids();
    //    offsetChild.pushHiddenToKids();
    ParseTree valueToken = ctx.VALUE_ESCAPE();
    String fullText = valueToken.getText();
    String quoteType = fullText.substring(1, 1);
    String text = fullText.substring(2, fullText.length() - 1);
    return new EscapedValue(new HiddenTokenAwareTreeAdapter(ctx), text, quoteType);
  }

  @Override
  public ASTCssNode visitEmbeddedScript(EmbeddedScriptContext ctx) {
    HiddenTokenAwareTreeAdapter token = new HiddenTokenAwareTreeAdapter(ctx);
    if (ctx.EMBEDDED_SCRIPT() != null) {
      String text = ctx.EMBEDDED_SCRIPT().getText();
      text = text.substring(1, text.length() - 1);
      return new FunctionExpression(token, "`", new EmbeddedScript(token, text));

    } else if (ctx.ESCAPED_SCRIPT() != null) {
      String text = ctx.ESCAPED_SCRIPT().getText();
      text = text.substring(2, text.length() - 1);
      return new FunctionExpression(token, "~`", new EmbeddedScript(token, text));

    } else {
      throw new BugHappened("Unexpected emvedded script type.", new HiddenTokenAwareTreeAdapter(ctx));
    }
  }

  @Override
  public FunctionExpression visitFunction(FunctionContext ctx) {
    //function: functionName LPAREN ws (functionParameters ws)? RPAREN;
    HiddenTokenAwareTreeAdapter token = new HiddenTokenAwareTreeAdapter(ctx);
    String name = ctx.functionName().getText();

    FunctionParametersContext functionParameters = ctx.functionParameters();
    if (functionParameters == null) {
      /* No arguments to the function */
      return new FunctionExpression(token, name, new EmptyExpression(token));
    }

    Expression parameter = visitFunctionParameters(functionParameters);
    //FIXME: (API) this is a hack - if what come out is not comma separated list, add it to comma separated list. - once there is API changing version it will be better to store parameters list in the function
    if (!isListOfParameters(parameter)) {
      parameter = packIntoListExpression(parameter);
    }
    return new FunctionExpression(token, name, parameter);
  }

  //FIXME: (antlr4) (cleanup) move following to some helper
  private Expression packIntoListExpression(Expression parameter) {
    ListExpressionOperator operator = new ListExpressionOperator(parameter.getUnderlyingStructure(), ListExpressionOperator.Operator.COMMA);
    return new ListExpression(parameter.getUnderlyingStructure(), asList(parameter), operator);
  }

  private List<Expression> asList(Expression... parameter) {
    List<Expression> result = new ArrayList<Expression>();
    for (Expression expression : parameter) {
      result.add(expression);
    }
    return result;
  }

  private boolean isListOfParameters(Expression parameter) {
    if (parameter.getType() != ASTCssNodeType.LIST_EXPRESSION)
      return false;

    ListExpression list = (ListExpression) parameter;
    return list.getOperator().getOperator() == ListExpressionOperator.Operator.COMMA;
  }

  @Override
  public FunctionExpression visitSpecial_function(Special_functionContext ctx) {
    //special_function: URI | URL_PREFIX | DOMAIN;

    String function = "";
    if (null != ctx.URI()) {
      function = "url";
    } else if (null != ctx.DOMAIN()) {
      function = "domain";
    } else if (null != ctx.URL_PREFIX()) {
      function = "url-prefix";
    }

    HiddenTokenAwareTreeAdapter token = new HiddenTokenAwareTreeAdapter(ctx);
    Expression parameter = extractUrlParameter(ctx, function, normalizeNewLineSymbols(ctx.getText()));
    parameter = packIntoListExpression(parameter);
    return new FunctionExpression(token, function, parameter);
  }

  // some places (e.g. only url) allow new lines in them. Less.js tend to
  // translate them into
  private String normalizeNewLineSymbols(String text) {
    return text.replaceAll("\r?\n", Constants.NEW_LINE);
  }

  private Expression extractUrlParameter(ParseTree token, String function, String text) {
    if (text == null)
      return null;

    if (text.length() <= function.length() + 2)
      return new CssString(new HiddenTokenAwareTreeAdapter(token), "", "");

    String string = text.substring(function.length() + 1, text.length() - 1);
    if (!URIUtils.isQuotedUrl(string))
      return new CssString(new HiddenTokenAwareTreeAdapter(token), string, "");

    String quote = String.valueOf(string.charAt(0));
    return new CssString(new HiddenTokenAwareTreeAdapter(token), string.substring(1, string.length() - 1), quote);
  }

  @Override
  public ASTCssNode visitIdentifierValueTerm(IdentifierValueTermContext ctx) {
    StringBuilder text = new StringBuilder();
    int childCount = ctx.getChildCount();
    for (int i = 0; i < childCount; i++) {
      ParseTree child = ctx.getChild(i);
      text.append(child.getText().trim());
    }
    if (childCount == 1) {
      ParseTree child = ctx.getChild(0);
      if (child instanceof TerminalNode) {
        if (LessG4Lexer.IMPORTANT_SYM == ((TerminalNode) child).getSymbol().getType()) {
          return new KeywordExpression(new HiddenTokenAwareTreeAdapter(ctx), child.getText().trim(), true);
        }
      }
    }

    return createIdentifierExpression(ctx, text.toString());
  }

  private Expression createIdentifierExpression(ParseTree parent, String text) {
    if (NamedColorExpression.isColorName(text))
      return NamedColorExpression.createNamedColorExpression(new HiddenTokenAwareTreeAdapter(parent), text);

    return new IdentifierExpression(new HiddenTokenAwareTreeAdapter(parent), text);
  }

  @Override
  public Expression visitTerm(TermContext ctx) {
    PlusOrMinusContext sign = ctx.sign;
    //last child is value
    int valueIndx = ctx.getChildCount() - 1;
    ParseTree value = ctx.getChild(valueIndx);
    Expression expression = termBuilder.buildFromSignedTerm(sign, value);
    return expression;
  }

  @Override
  public NamedExpression visitNamedFunctionParameter(NamedFunctionParameterContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    Expression value = (Expression) ctx.term().accept(this);

    return new NamedExpression(token, ctx.ident().getText(), value);
  }

  @Override
  public Expression visitFunctionParameters(FunctionParametersContext ctx) {
    if (null != ctx.expression_full()) {
      return visitExpression_full(ctx.expression_full());
    }
    //FIXME: (antlr4) (comments) move comments on commas
    List<NamedFunctionParameterContext> namedFunctionParameter = ctx.namedFunctionParameter();
    List<Expression> parameters = new ArrayList<Expression>();
    for (NamedFunctionParameterContext parameter : namedFunctionParameter) {
      parameters.add(visitNamedFunctionParameter(parameter));
    }
    ListExpression result = toCommaSeparatedList(ctx, parameters);
    return result;
  }

  private ListExpression toCommaSeparatedList(ParseTree ctx, List<Expression> parameters) {
    ListExpressionOperator operator = new ListExpressionOperator(new HiddenTokenAwareTreeAdapter(ctx), ListExpressionOperator.Operator.COMMA);
    ListExpression result = new ListExpression(new HiddenTokenAwareTreeAdapter(ctx), parameters, operator);
    return result;
  }

  private ListExpression toListExpression(ParseTree ctx, List<Expression> parameters, ListExpressionOperator.Operator listOperator) {
    ListExpressionOperator operator = new ListExpressionOperator(new HiddenTokenAwareTreeAdapter(ctx), listOperator);
    ListExpression result = new ListExpression(new HiddenTokenAwareTreeAdapter(ctx), parameters, operator);
    return result;
  }

  @Override
  public Expression visitMathExprHighPriorNoWhitespaceList(MathExprHighPriorNoWhitespaceListContext ctx) {
    Expression result = (Expression) ctx.mathExprLowPrior().accept(this);
    List<MathExprHighPriorNoWhitespaceList_helperContext> continuation = ctx.mathExprHighPriorNoWhitespaceList_helper();
    if (continuation.isEmpty())
      return result;

    for (MathExprHighPriorNoWhitespaceList_helperContext next : continuation) {
      BinaryExpressionOperator operator = visitPlusOrMinus(next.operator);
      Expression value = (Expression) next.mathExprLowPrior().accept(this);
      result = new BinaryExpression(new HiddenTokenAwareTreeAdapter(next), result, operator, value);
    }

    return result;
  }

  @Override
  public BinaryExpressionOperator visitPlusOrMinus(PlusOrMinusContext ctx) {
    return doVisitBinaryExpressionOperatorNode(ctx);
  }

  @Override
  public BinaryExpressionOperator visitMathOperatorHighPrior(MathOperatorHighPriorContext ctx) {
    return doVisitBinaryExpressionOperatorNode(ctx);
  }

  private BinaryExpressionOperator doVisitBinaryExpressionOperatorNode(ParserRuleContext ctx) {
    ParseTree node = ctx.getChild(0);
    if (!(node instanceof TerminalNode))
      throw new BugHappened(GRAMMAR_MISMATCH, new HiddenTokenAwareTreeAdapter(ctx));

    Token token = ((TerminalNode) node).getSymbol();
    BinaryExpressionOperator.Operator operator = toExpressionOperator(token);
    return new BinaryExpressionOperator(new HiddenTokenAwareTreeAdapter(ctx), operator);
  }

  private BinaryExpressionOperator.Operator toExpressionOperator(Token token) {
    switch (token.getType()) {
    case LessG4Lexer.SOLIDUS:
      return BinaryExpressionOperator.Operator.SOLIDUS;

    case LessG4Lexer.STAR:
      return BinaryExpressionOperator.Operator.STAR;

    case LessG4Lexer.MINUS:
      return BinaryExpressionOperator.Operator.MINUS;

    case LessG4Lexer.PLUS:
      return BinaryExpressionOperator.Operator.PLUS;

    default:
      break;
    }

    throw new BugHappened(GRAMMAR_MISMATCH, new HiddenTokenAwareTreeAdapter(token));
  }

  @Override
  public ASTCssNode visitMathExprHighPriorNoWhitespaceList_helper(MathExprHighPriorNoWhitespaceList_helperContext ctx) {
    throw new BugHappened(SHOULD_NOT_VISIT, new HiddenTokenAwareTreeAdapter(ctx));
  }

  @Override
  public Expression visitMathExprLowPrior(MathExprLowPriorContext ctx) {
    Iterator<TermContext> terms = ctx.term().iterator();
    Iterator<MathOperatorHighPriorContext> operators = ctx.mathOperatorHighPrior().iterator();
    Expression result = (Expression) terms.next().accept(this);

    while (terms.hasNext() && operators.hasNext()) {
      BinaryExpressionOperator operator = visitMathOperatorHighPrior(operators.next());
      TermContext term = terms.next();
      Expression value = (Expression) term.accept(this);
      result = new BinaryExpression(new HiddenTokenAwareTreeAdapter(term), result, operator, value);
    }
    if (terms.hasNext() || operators.hasNext()) {
      throw new BugHappened(GRAMMAR_MISMATCH, new HiddenTokenAwareTreeAdapter(ctx));
    }

    return result;
  }

  @Override
  public ASTCssNode visitHexColor(HexColorContext ctx) {
    String text = ctx.getText();
    ColorExpression parsedColor = ConversionUtils.parseColor(new HiddenTokenAwareTreeAdapter(ctx), text);
    if (parsedColor == null) {
      FaultyExpression faultyExpression = new FaultyExpression(new HiddenTokenAwareTreeAdapter(ctx));
      problemsHandler.notAColor(faultyExpression, text);
      return faultyExpression;
    }

    return parsedColor;
  }

  @Override
  public Expression visitUnsigned_value_term(Unsigned_value_termContext ctx) {
    IdentifierValueTermContext identifierValueTerm = ctx.identifierValueTerm();
    if (identifierValueTerm != null)
      return termBuilder.createIdentifierExpression(ctx, ctx.getText());

    return termBuilder.buildFromTerm(ctx.getChild(0));
  }

  @Override
  public Expression visitTerm_no_preceeding_whitespace(Term_no_preceeding_whitespaceContext ctx) {
    return (Expression) ctx.getChild(0).accept(this);
  }

  @Override
  public ASTCssNode visitIdentifierValueTermHelper(IdentifierValueTermHelperContext ctx) {
    throw new BugHappened(SHOULD_NOT_VISIT, new HiddenTokenAwareTreeAdapter(ctx));
  }

  @Override
  public Expression visitTerm_only_function(Term_only_functionContext ctx) {
    return (Expression) ctx.getChild(0).accept(this);
  }

  @Override
  public ASTCssNode visitValue_term(Value_termContext ctx) {
    return termBuilder.buildFromTerm(ctx.getChild(0));
  }

  @Override
  public ASTCssNode visitFunctionName(FunctionNameContext ctx) {
    throw new BugHappened(SHOULD_NOT_VISIT, new HiddenTokenAwareTreeAdapter(ctx));
  }

  @Override
  public FixedNamePart visitFixedIdOrClassNamePart(FixedIdOrClassNamePartContext ctx) {
    return new FixedNamePart(new HiddenTokenAwareTreeAdapter(ctx), ctx.getText());
  }

  @Override
  public IdSelector visitIdSelector(IdSelectorContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    return new IdSelector(token, toInterpolableName(ctx));
  }

  @Override
  public CssClass visitCssClass(CssClassContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    return new CssClass(token, toInterpolableName(ctx, 1));
  }

  @Override
  public ElementSubsequent visitCssClassOrId(CssClassOrIdContext ctx) {
    CssClassContext cssClass = ctx.cssClass();
    if (cssClass != null)
      return visitCssClass(cssClass);

    IdSelectorContext idSelector = ctx.idSelector();
    if (idSelector != null)
      return visitIdSelector(idSelector);

    throw new BugHappened(GRAMMAR_MISMATCH, new HiddenTokenAwareTreeAdapter(ctx));
  }

  @Override
  public ElementSubsequent visitElementSubsequent(ElementSubsequentContext ctx) {
    AttribOrPseudoContext attribOrPseudo = ctx.attribOrPseudo();
    if (attribOrPseudo != null)
      return visitAttribOrPseudo(attribOrPseudo);

    CssClassOrIdContext cssClassOrId = ctx.cssClassOrId();
    if (cssClassOrId != null)
      return visitCssClassOrId(cssClassOrId);

    throw new BugHappened(GRAMMAR_MISMATCH, new HiddenTokenAwareTreeAdapter(ctx));
  }

  @Override
  public ElementSubsequent visitAttribOrPseudo(AttribOrPseudoContext ctx) {
    AttribContext attrib = ctx.attrib();
    if (attrib != null)
      return visitAttrib(attrib);

    PseudoContext pseudo = ctx.pseudo();
    if (pseudo != null)
      return visitPseudo(pseudo);

    throw new BugHappened(GRAMMAR_MISMATCH, new HiddenTokenAwareTreeAdapter(ctx));
  }

  @Override
  public ElementSubsequent visitAttrib(AttribContext ctx) {
    String name = ctx.ident().getText();

    SelectorAttributeOperatorContext operatorCntx = ctx.selectorAttributeOperator();
    SelectorOperator operator = operatorCntx != null ? visitSelectorAttributeOperator(operatorCntx) : null;

    Expression value = null;
    TermContext termCntx = ctx.term();
    if (termCntx != null) {
      value = visitTerm(termCntx);
      //FIXME: (antlr4) (review) review all on usages of problemsHandler in original - almost missed this one
      switch (value.getType()) {
      case IDENTIFIER_EXPRESSION:
      case STRING_EXPRESSION:
      case NUMBER:
        //those are OK
        break;

      default:
        problemsHandler.warnLessjsIncompatibleSelectorAttributeValue(value);
        break;
      }
    }

    return new SelectorAttribute(new HiddenTokenAwareTreeAdapter(ctx), name, operator, value);
  }

  @Override
  public SelectorOperator visitSelectorAttributeOperator(SelectorAttributeOperatorContext ctx) {
    SelectorOperator.Operator operator = toSelectorOperator((TerminalNode) ctx.getChild(0));
    return new SelectorOperator(new HiddenTokenAwareTreeAdapter(ctx), operator);
  }

  private SelectorOperator.Operator toSelectorOperator(TerminalNode token) {
    switch (token.getSymbol().getType()) {
    case LessG4Lexer.OPEQ:
      return SelectorOperator.Operator.EQUALS;

    case LessG4Lexer.INCLUDES:
      return SelectorOperator.Operator.INCLUDES;

    case LessG4Lexer.DASHMATCH:
      return SelectorOperator.Operator.SPECIAL_PREFIX;

    case LessG4Lexer.PREFIXMATCH:
      return SelectorOperator.Operator.PREFIXMATCH;

    case LessG4Lexer.SUFFIXMATCH:
      return SelectorOperator.Operator.SUFFIXMATCH;

    case LessG4Lexer.SUBSTRINGMATCH:
      return SelectorOperator.Operator.SUBSTRINGMATCH;

    default:
      break;
    }

    throw new BugHappened(GRAMMAR_MISMATCH, new HiddenTokenAwareTreeAdapter(token));
  }

  @Override
  public ElementSubsequent visitPseudo(PseudoContext ctx) {
    //    pseudo
    //    : (COLON COLON? ( 
    //          (ident_nth LPAREN ws (nth | variablereference | INTERPOLATED_VARIABLE) ws RPAREN)
    //        | (IDENT_EXTEND LPAREN ws (extendTargetSelectors) ws RPAREN)
    //        | (IDENT_NOT LPAREN ws (selector) ws RPAREN)
    //        | (ident_general_pseudo LPAREN ws pseudoparameters ws RPAREN)
    //        |  ident
    //    ));
    HiddenTokenAwareTreeAdapter token = new HiddenTokenAwareTreeAdapter(ctx);

    List<TerminalNode> colons = ctx.COLON();
    int colonsCnt = colons.size();
    String pseudoName = ctx.getChild(colonsCnt).getText();

    if (colonsCnt == 2) {
      return new PseudoElement(token, pseudoName, false);
    }

    if (COLONLESS_PSEUDOELEMENTS.contains(pseudoName.toLowerCase())) {
      return new PseudoElement(token, pseudoName, true);
    }

    int paramenterIndx = colonsCnt + 3; //that should be parameter
    if (ctx.getChildCount() <= paramenterIndx) {
      return new PseudoClass(token, pseudoName);
    }
    ParseTree parameter = ctx.getChild(paramenterIndx);
    if (isTokenOfType(parameter, LessG4Lexer.INDIRECT_VARIABLE))
      return new PseudoClass(token, pseudoName, toInterpolabledVariable(parameter, parameter.getText()));

    return new PseudoClass(token, pseudoName, parameter.accept(this));
  }

  private boolean isTokenOfType(ParseTree treeNode, int type) {
    if (treeNode instanceof TerminalNode) {
      TerminalNode token = (TerminalNode) treeNode;
      return token.getSymbol().getType() == type;
    }

    return false;
  }

  @Override
  public ASTCssNode visitRuleSet(RuleSetContext ctx) {
    RuleSet ruleSet = new RuleSet(new HiddenTokenAwareTreeAdapter(ctx));
    List<SelectorContext> selectorsCtx = ctx.selector();

    //FIXME: (antlr4) (comments) around commas
    for (SelectorContext selectorContext : selectorsCtx) {
      Selector selector = visitSelector(selectorContext);
      ruleSet.addSelector(selector);
    }

    General_bodyContext general_body = ctx.general_body();
    ruleSet.setBody(visitGeneral_body(general_body));

    return ruleSet;
  }

  @Override
  public Selector visitSelector(SelectorContext ctx) {
    Selector result = new Selector(new HiddenTokenAwareTreeAdapter(ctx));

    Iterator<Combinator_wsContext> combinators = ctx.combinator_ws().iterator();
    Iterator<Non_combinator_selector_blockContext> blocks = ctx.non_combinator_selector_block().iterator();

    if (ctx.leading == null && blocks.hasNext()) {
      Non_combinator_selector_blockContext blockCtx = blocks.next();
      Selector block = visitNon_combinator_selector_block(blockCtx);
      result.addParts(block.getParts());
    }

    while (combinators.hasNext() && blocks.hasNext()) {
      Combinator_wsContext combinatorCtx = combinators.next();
      SelectorCombinator leadingCombinator = visitCombinator_ws(combinatorCtx);

      Non_combinator_selector_blockContext blockCtx = blocks.next();
      Selector block = visitNon_combinator_selector_block(blockCtx);

      List<SelectorPart> parts = block.getParts();
      if (parts.isEmpty()) {
        throw new BugHappened(GRAMMAR_MISMATCH, new HiddenTokenAwareTreeAdapter(blockCtx));
      }
      parts.get(0).setLeadingCombinator(leadingCombinator);
      result.addParts(parts);
    }

    if (combinators.hasNext() || blocks.hasNext()) {
      throw new BugHappened(GRAMMAR_MISMATCH, new HiddenTokenAwareTreeAdapter(ctx));
    }

    return result;
  }

  @Override
  public SelectorCombinator visitCombinator(CombinatorContext ctx) {
    HiddenTokenAwareTreeAdapter token = new HiddenTokenAwareTreeAdapter(ctx);
    SelectorCombinator.CombinatorType combinator = safeToSelectorCombinator(ctx.getChild(0));

    return new SelectorCombinator(token, combinator, ctx.getText());
  }

  @Override
  public SelectorCombinator visitCombinator_ws(Combinator_wsContext ctx) {
    HiddenTokenAwareTreeAdapter token = new HiddenTokenAwareTreeAdapter(ctx);
    if (ctx.mandatory_ws() != null) {
      SelectorCombinator.CombinatorType combinator = SelectorCombinator.CombinatorType.DESCENDANT;
      return new SelectorCombinator(token, combinator, ctx.getText());
    }

    return visitCombinator(ctx.combinator());
  }

  //FIXME (antlr4) this was in conversions utils, may put it back there?
  private SelectorCombinator.CombinatorType safeToSelectorCombinator(ParseTree ctx) {
    if (!(ctx instanceof TerminalNode))
      return null;

    TerminalNode token = (TerminalNode) ctx;
    switch (token.getSymbol().getType()) {
    case LessG4Lexer.PLUS:
      return SelectorCombinator.CombinatorType.ADJACENT_SIBLING;
    case LessG4Lexer.GREATER:
      return SelectorCombinator.CombinatorType.CHILD;
    case LessG4Lexer.TILDE:
      return SelectorCombinator.CombinatorType.GENERAL_SIBLING;
    case LessG4Lexer.HAT:
      return SelectorCombinator.CombinatorType.HAT;
    case LessG4Lexer.CAT:
      return SelectorCombinator.CombinatorType.CAT;
    case LessG4Lexer.SOLIDUS:
      return SelectorCombinator.CombinatorType.NAMED;
    default:
      return null;
    }
  }

  @Override
  public Selector visitNon_combinator_selector_block(Non_combinator_selector_blockContext ctx) {
    Selector result = new Selector(new HiddenTokenAwareTreeAdapter(ctx));
    int childCount = ctx.getChildCount();
    for (int i = 0; i < childCount; i++) {
      ParseTree child = ctx.getChild(i);
      SelectorPart part = (SelectorPart) child.accept(this);
      result.addPart(part);
    }
    return result;
  }

  @Override
  public SimpleSelector visitSimpleSelector(SimpleSelectorContext ctx) {
    HiddenTokenAwareTreeAdapter token = new HiddenTokenAwareTreeAdapter(ctx);
    SimpleSelector result = null;
    SelectorCombinator leadingCombinator = null;

    ElementNameContext elementNameCtx = ctx.elementName();
    if (elementNameCtx != null) {
      InterpolableName elementName = visitElementName(elementNameCtx);
      result = new SimpleSelector(token, leadingCombinator, elementName, false);
    } else {
      result = new SimpleSelector(token, leadingCombinator, null, true);
      result.setEmptyForm(true);
    }

    List<ElementSubsequentContext> elementSubsequent = ctx.elementSubsequent();
    for (ElementSubsequentContext esCntx : elementSubsequent) {
      ElementSubsequent subsequent = visitElementSubsequent(esCntx);
      result.addSubsequent(subsequent);
    }

    return result;
  }

  @Override
  public InterpolableName visitElementName(ElementNameContext ctx) {
    InterpolableName interpolableName = toInterpolableName(ctx);
    return interpolableName;
  }

  @Override
  public ASTCssNode visitElementNamePart(ElementNamePartContext ctx) {
    throw new BugHappened(SHOULD_NOT_VISIT, new HiddenTokenAwareTreeAdapter(ctx));
  }

  @Override
  public ASTCssNode visitAllNumberKinds(AllNumberKindsContext ctx) {
    throw new BugHappened(SHOULD_NOT_VISIT, new HiddenTokenAwareTreeAdapter(ctx));
  }

  @Override
  public ASTCssNode visitPseudoparameter_termValue(Pseudoparameter_termValueContext ctx) {
    if (ctx.getChildCount() != 1) {
      throw new BugHappened(GRAMMAR_MISMATCH, new HiddenTokenAwareTreeAdapter(ctx));
    }
    return termBuilder.buildFromTerm(ctx.getChild(0));
  }

  @Override
  public EscapedSelector visitEscapedSelectorOldSyntax(EscapedSelectorOldSyntaxContext ctx) {
    //FIXME: (antlr4) (comments)
    //token.pushHiddenToKids();
    HiddenTokenAwareTree valueToken = new HiddenTokenAwareTreeAdapter(ctx);
    String quotedText = ctx.VALUE_ESCAPE().getText();
    return new EscapedSelector(valueToken, quotedText.substring(2, quotedText.length() - 1), "" + quotedText.charAt(1), null);
  }

  @Override
  public ASTCssNode visitNestedAppender(NestedAppenderContext ctx) {
    //FIXME: (antlr4) (deal with spaces before and after)
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    boolean directlyBefore = false;
    boolean directlyAfter = false;
    SelectorCombinator leadingCombinator = null;
    return new NestedSelectorAppender(token, directlyBefore, directlyAfter, leadingCombinator);
  }

  @Override
  public ASTCssNode visitNth(NthContext ctx) {
    //    nth: (((leadingSign=PLUS | leadingSign=MINUS) ws )? (repeater=REPEATER | name=ident) (ws (secondSign=PLUS | secondSign=MINUS) ws secondNumber=NUMBER)?
    //        | ((onlyNumberSign=PLUS | onlyNumberSign=MINUS) ws)? onlyNumber=NUMBER);

    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    Expression first = null;
    Expression second = null;

    Nth_baseContext nth_baseCtx = ctx.nth_base();
    if (nth_baseCtx != null) {
      first = visitNth_base(nth_baseCtx);
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
          boolean expliciteSign = !"n".equals(lowerCaseValue);
          first = new NumberExpression(token, lowerCaseValue, NumberExpression.Dimension.REPEATER, expliciteSign);
        } else
          throw new IllegalStateException("Unexpected identifier value for nth: " + ident.getValue());
      }
    }

    Nth_expressionContext nth_expressionCtx = ctx.nth_expression();
    if (nth_expressionCtx != null) {
      second = visitNth_expression(nth_expressionCtx);
    }

    return new Nth(token, (NumberExpression) first, (NumberExpression) second);
  }

  @Override
  public Expression visitNth_expression(Nth_expressionContext ctx) {
    PlusOrMinusContext sign = ctx.sign;
    //last child is value
    int valueIndx = ctx.getChildCount() - 1;
    ParseTree value = ctx.getChild(valueIndx);
    Expression expression = termBuilder.buildFromSignedTerm(sign, value);
    return expression;
  }

  @Override
  public Expression visitNth_base(Nth_baseContext ctx) {
    PlusOrMinusContext sign = ctx.sign;
    //last child is value
    int valueIndx = ctx.getChildCount() - 1;
    ParseTree value = ctx.getChild(valueIndx);
    Expression expression = termBuilder.buildFromSignedTerm(sign, value);
    return expression;
  }

  @Override
  public ASTCssNode visitVariabledeclarationWithSemicolon(VariabledeclarationWithSemicolonContext ctx) {
    return visitVariabledeclaration(ctx.variabledeclaration());
  }

  @Override
  public ASTCssNode visitVariabledeclaration(VariabledeclarationContext ctx) {
    HiddenTokenAwareTreeAdapter token = new HiddenTokenAwareTreeAdapter(ctx);

    Variable variable = visitVariablename(ctx.variablename());
    Expression value = visitExpression_full(ctx.expression_full());
    return new VariableDeclaration(token, variable, value);
  }

  /*
   * ***************************UNFINISHED HERE*******************************************
   */
  @Override
  public ASTCssNode visitExtendTargetSelectors(ExtendTargetSelectorsContext ctx) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public ASTCssNode visitExtendInDeclarationWithSemi(ExtendInDeclarationWithSemiContext ctx) {
    // TODO Auto-generated method stub
    return null;
  }

}

class HiddenTokenAwareTreeAdapter extends HiddenTokenAwareTree {

  private Token start;

  public HiddenTokenAwareTreeAdapter(ParseTree ctx) {
    this(toToken(ctx));
  }

  private static Token toToken(ParseTree ctx) {
    if (ctx instanceof Token) {
      return (Token) ctx;
    }
    if (ctx instanceof TerminalNode) {
      return ((TerminalNode) ctx).getSymbol();
    }
    if (ctx instanceof ParserRuleContext) {
      return ((ParserRuleContext) ctx).start;
    }
    throw new BugHappened("Unexpected parse tree kind.", (HiddenTokenAwareTree) null);
  }

  public HiddenTokenAwareTreeAdapter(ParserRuleContext ctx) {
    this(ctx.start);
  }

  public HiddenTokenAwareTreeAdapter(Token start) {
    super(null);
    this.start = start;
  }

  public LessSource getSource() {
    if (start instanceof Antlr4_HiddenTokenAwareTree) {
      return ((Antlr4_HiddenTokenAwareTree) start).getSource();
    }
    throw new BugHappened("Less source not available.", (HiddenTokenAwareTree) null);
  }

  @Override
  public String getText() {
    return start.getText();
  }

  @Override
  public int getLine() {
    return start.getLine();
  }
  
  @Override
  public int getCharPositionInLine() {
    return start.getCharPositionInLine();
  }
  
}