package com.github.sommeri.less4j.antlr4.core.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.BinaryExpressionOperator;
import com.github.sommeri.less4j.core.ast.Comment;
import com.github.sommeri.less4j.core.ast.ComparisonExpressionOperator;
import com.github.sommeri.less4j.core.ast.CssClass;
import com.github.sommeri.less4j.core.ast.Declaration;
import com.github.sommeri.less4j.core.ast.DetachedRuleset;
import com.github.sommeri.less4j.core.ast.DetachedRulesetReference;
import com.github.sommeri.less4j.core.ast.Document;
import com.github.sommeri.less4j.core.ast.ElementSubsequent;
import com.github.sommeri.less4j.core.ast.EscapedSelector;
import com.github.sommeri.less4j.core.ast.EscapedValue;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.Extend;
import com.github.sommeri.less4j.core.ast.FixedNamePart;
import com.github.sommeri.less4j.core.ast.FontFace;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.Guard;
import com.github.sommeri.less4j.core.ast.GuardCondition;
import com.github.sommeri.less4j.core.ast.IdSelector;
import com.github.sommeri.less4j.core.ast.InterpolableName;
import com.github.sommeri.less4j.core.ast.InterpolableNamePart;
import com.github.sommeri.less4j.core.ast.KeyframesName;
import com.github.sommeri.less4j.core.ast.ListExpression;
import com.github.sommeri.less4j.core.ast.Media;
import com.github.sommeri.less4j.core.ast.MediaExpression;
import com.github.sommeri.less4j.core.ast.MediaExpressionFeature;
import com.github.sommeri.less4j.core.ast.MediaQuery;
import com.github.sommeri.less4j.core.ast.Medium;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.Name;
import com.github.sommeri.less4j.core.ast.NamedExpression;
import com.github.sommeri.less4j.core.ast.PageMarginBox;
import com.github.sommeri.less4j.core.ast.ParenthesesExpression;
import com.github.sommeri.less4j.core.ast.Pseudo;
import com.github.sommeri.less4j.core.ast.ReusableStructureName;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.ast.SelectorCombinator;
import com.github.sommeri.less4j.core.ast.SelectorOperator;
import com.github.sommeri.less4j.core.ast.SimpleSelector;
import com.github.sommeri.less4j.core.ast.Supports;
import com.github.sommeri.less4j.core.ast.SupportsCondition;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.ast.Viewport;
import com.github.sommeri.less4j.core.parser.*;
import com.github.sommeri.less4j.core.parser.LessG4Parser.AllNumberKindsContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.AttribContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.AttribOrPseudoContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.CharSetContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.CollectorContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.CombinatorContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Combinator_wsContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.CommaSplitMixinReferenceArgumentsContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.CommaSplitReusableStructureArgumentsContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.CompareOperatorContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.CssClassContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.CssClassOrIdContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.CssMediaExpressionContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.DeclarationContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.DeclarationWithSemicolonContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.DetachedRulesetContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.DetachedRulesetReferenceContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.DocumentContext;
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
import com.github.sommeri.less4j.core.parser.LessG4Parser.FontfaceContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.FunctionContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.FunctionNameContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.FunctionParametersContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.General_bodyContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.General_body_memberContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.General_body_member_no_semiContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.GuardConditionContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.GuardContext;
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
import com.github.sommeri.less4j.core.parser.LessG4Parser.ImportoptionsContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.ImportsContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.InterpolatedMediaExpressionContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.KeyframesContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.KeyframesnameContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Mandatory_wsContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.MathExprHighPriorContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.MathExprHighPriorNoWhitespaceListContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.MathExprHighPriorNoWhitespaceList_helperContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.MathExprLowPriorContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.MathOperatorHighPriorContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.MediaExpressionContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.MediaFeatureContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.MediaQueryContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Media_in_general_bodyContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Media_top_levelContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.MediumContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.MixinReferenceArgument_no_commaContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.MixinReferenceArgument_with_commaContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.MixinReferenceArgumentsContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.MixinReferenceContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.MixinReferenceWithSemiContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.NamedFunctionParameterContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.NamespaceReferenceContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.NamespaceReferenceWithSemiContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.NestedAppenderContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Non_combinator_selector_blockContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.NthContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Nth_baseContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Nth_expressionContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.PageContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.PageMarginBoxContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.PlusOrMinusContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.PropertyContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.PropertyNamePartContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.PseudoContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.PseudoPageContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Pseudoparameter_termValueContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.ReferenceSeparatorContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.ReusableStructureArgumentsContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.ReusableStructureContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.ReusableStructureGuardsContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.ReusableStructureNameContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.RsAtNameContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.RsParameterWithDefault_no_commaContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.RsParameterWithDefault_with_commaContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.RsParameterWithoutDefaultContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.RsParameter_no_commaContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.RsParameter_with_commaContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.RsPatternContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.RuleSetContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.SelectorAttributeOperatorContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.SelectorContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.SemiSplitMixinReferenceArgumentsContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.SemiSplitReusableStructureArgumentsContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.SimpleSelectorContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Special_functionContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.SscDeclarationContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.SscNestedConditionContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.SscNotConditionContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.StyleSheetContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.SupportsConditionContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.SupportsContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.SupportsQueryContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.TermContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Term_no_preceeding_whitespaceContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Term_only_functionContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Top_level_bodyContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Top_level_body_with_declarationContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Top_level_body_with_declaration_memberContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Top_level_elementContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.UnknownAtRuleContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.UnknownAtRuleNamesSetContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Unsigned_value_termContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Value_termContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.VariabledeclarationContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.VariabledeclarationWithSemicolonContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.VariablenameContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.VariablereferenceContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.ViewportContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.WsContext;
import com.github.sommeri.less4j.core.problems.BugHappened;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class CommentsShiftingBuilderSwitch extends Antlr4_ASTBuilderSwitch {

  private final TreeComments treeComments;

  public CommentsShiftingBuilderSwitch(ProblemsHandler problemsHandler, TreeComments treeComments) {
    super(problemsHandler, treeComments);
    this.treeComments = treeComments;
  }

  @Override
  public ASTCssNode visitTop_level_element(Top_level_elementContext ctx) {
    return handleComments(super.visitTop_level_element(ctx), ctx);
  }

  @Override
  public Declaration visitDeclaration(DeclarationContext ctx) {
    return handleComments(super.visitDeclaration(ctx), ctx);
  }

  @Override
  public ASTCssNode visitGeneral_body_member(General_body_memberContext ctx) {
    return handleComments(super.visitGeneral_body_member(ctx), ctx);
  }

  @Override
  public ASTCssNode visitStyleSheet(StyleSheetContext ctx) {
    return handleComments(super.visitStyleSheet(ctx), ctx);
  }

  @Override
  public ASTCssNode visitDeclarationWithSemicolon(DeclarationWithSemicolonContext ctx) {
    return handleComments(super.visitDeclarationWithSemicolon(ctx), ctx);
  }

  @Override
  public InterpolableName visitProperty(PropertyContext ctx) {
    return handleComments(super.visitProperty(ctx), ctx);
  }

  @Override
  public InterpolableNamePart visitPropertyNamePart(PropertyNamePartContext ctx) {
    return handleComments(super.visitPropertyNamePart(ctx), ctx);
  }

  @Override
  public Expression visitExpression_full(Expression_fullContext ctx) {
    return handleComments(super.visitExpression_full(ctx), ctx);
  }

  @Override
  public Expression visitExpression_comma_separated_list(Expression_comma_separated_listContext ctx) {
    return handleComments(super.visitExpression_comma_separated_list(ctx), ctx);
  }

  @Override
  public Expression visitExpression_space_separated_list(Expression_space_separated_listContext ctx) {
    return handleComments(super.visitExpression_space_separated_list(ctx), ctx);
  }

  @Override
  public ASTCssNode visitWs(WsContext ctx) {
    return handleComments(super.visitWs(ctx), ctx);
  }

  @Override
  public ASTCssNode visitMandatory_ws(Mandatory_wsContext ctx) {
    return handleComments(super.visitMandatory_ws(ctx), ctx);
  }

  @Override
  public ASTCssNode visitIdent_nth(Ident_nthContext ctx) {
    return handleComments(super.visitIdent_nth(ctx), ctx);
  }

  @Override
  public ASTCssNode visitIdent_keywords(Ident_keywordsContext ctx) {
    return handleComments(super.visitIdent_keywords(ctx), ctx);
  }

  @Override
  public ASTCssNode visitIdent_special_pseudoclasses(Ident_special_pseudoclassesContext ctx) {
    return handleComments(super.visitIdent_special_pseudoclasses(ctx), ctx);
  }

  @Override
  public ASTCssNode visitIdent(IdentContext ctx) {
    return handleComments(super.visitIdent(ctx), ctx);
  }

  @Override
  public ASTCssNode visitIdent_except_not(Ident_except_notContext ctx) {
    return handleComments(super.visitIdent_except_not(ctx), ctx);
  }

  @Override
  public ASTCssNode visitIdent_general_pseudo(Ident_general_pseudoContext ctx) {
    return handleComments(super.visitIdent_general_pseudo(ctx), ctx);
  }

  @Override
  public ASTCssNode visitChildren(RuleNode node) {
    //    return handleComments(super.visitChildren(node), node);
    return super.visitChildren(node);
  }

  @Override
  public ASTCssNode visitTerminal(TerminalNode node) {
    //    return handleComments(super.visitTerminal(node), node);
    return super.visitTerminal(node);
  }

  @Override
  public GeneralBody visitGeneral_body(General_bodyContext ctx) {
    return handleComments(super.visitGeneral_body(ctx), ctx);
  }

  @Override
  public ASTCssNode visitGeneral_body_member_no_semi(General_body_member_no_semiContext ctx) {
    return handleComments(super.visitGeneral_body_member_no_semi(ctx), ctx);
  }

  @Override
  public Variable visitVariablereference(VariablereferenceContext ctx) {
    return handleComments(super.visitVariablereference(ctx), ctx);
  }

  @Override
  public Variable visitVariablename(VariablenameContext ctx) {
    return handleComments(super.visitVariablename(ctx), ctx);
  }

  @Override
  public ParenthesesExpression visitExpr_in_parentheses(Expr_in_parenthesesContext ctx) {
    return handleComments(super.visitExpr_in_parentheses(ctx), ctx);
  }

  @Override
  public EscapedValue visitEscapedValue(EscapedValueContext ctx) {
    return handleComments(super.visitEscapedValue(ctx), ctx);
  }

  @Override
  public ASTCssNode visitEmbeddedScript(EmbeddedScriptContext ctx) {
    return handleComments(super.visitEmbeddedScript(ctx), ctx);
  }

  @Override
  public FunctionExpression visitFunction(FunctionContext ctx) {
    return handleComments(super.visitFunction(ctx), ctx);
  }

  @Override
  public FunctionExpression visitSpecial_function(Special_functionContext ctx) {
    return handleComments(super.visitSpecial_function(ctx), ctx);
  }

  @Override
  public ASTCssNode visitIdentifierValueTerm(IdentifierValueTermContext ctx) {
    return handleComments(super.visitIdentifierValueTerm(ctx), ctx);
  }

  @Override
  public Expression visitTerm(TermContext ctx) {
    return handleComments(super.visitTerm(ctx), ctx);
  }

  @Override
  public NamedExpression visitNamedFunctionParameter(NamedFunctionParameterContext ctx) {
    return handleComments(super.visitNamedFunctionParameter(ctx), ctx);
  }

  @Override
  public Expression visitFunctionParameters(FunctionParametersContext ctx) {
    return handleComments(super.visitFunctionParameters(ctx), ctx);
  }

  @Override
  public Expression visitMathExprHighPriorNoWhitespaceList(MathExprHighPriorNoWhitespaceListContext ctx) {
    return handleComments(super.visitMathExprHighPriorNoWhitespaceList(ctx), ctx);
  }

  @Override
  public Expression visitMathExprHighPrior(MathExprHighPriorContext ctx) {
    return handleComments(super.visitMathExprHighPrior(ctx), ctx);
  }

  @Override
  public BinaryExpressionOperator visitPlusOrMinus(PlusOrMinusContext ctx) {
    return handleComments(super.visitPlusOrMinus(ctx), ctx);
  }

  @Override
  public BinaryExpressionOperator visitMathOperatorHighPrior(MathOperatorHighPriorContext ctx) {
    return handleComments(super.visitMathOperatorHighPrior(ctx), ctx);
  }

  @Override
  public ASTCssNode visitMathExprHighPriorNoWhitespaceList_helper(MathExprHighPriorNoWhitespaceList_helperContext ctx) {
    return handleComments(super.visitMathExprHighPriorNoWhitespaceList_helper(ctx), ctx);
  }

  @Override
  public Expression visitMathExprLowPrior(MathExprLowPriorContext ctx) {
    return handleComments(super.visitMathExprLowPrior(ctx), ctx);
  }

  @Override
  public ASTCssNode visitHexColor(HexColorContext ctx) {
    return handleComments(super.visitHexColor(ctx), ctx);
  }

  @Override
  public Expression visitUnsigned_value_term(Unsigned_value_termContext ctx) {
    return handleComments(super.visitUnsigned_value_term(ctx), ctx);
  }

  @Override
  public Expression visitTerm_no_preceeding_whitespace(Term_no_preceeding_whitespaceContext ctx) {
    return handleComments(super.visitTerm_no_preceeding_whitespace(ctx), ctx);
  }

  @Override
  public ASTCssNode visitIdentifierValueTermHelper(IdentifierValueTermHelperContext ctx) {
    return handleComments(super.visitIdentifierValueTermHelper(ctx), ctx);
  }

  @Override
  public FunctionExpression visitTerm_only_function(Term_only_functionContext ctx) {
    return handleComments(super.visitTerm_only_function(ctx), ctx);
  }

  @Override
  public ASTCssNode visitValue_term(Value_termContext ctx) {
    return handleComments(super.visitValue_term(ctx), ctx);
  }

  @Override
  public ASTCssNode visitFunctionName(FunctionNameContext ctx) {
    return handleComments(super.visitFunctionName(ctx), ctx);
  }

  @Override
  public FixedNamePart visitFixedIdOrClassNamePart(FixedIdOrClassNamePartContext ctx) {
    return handleComments(super.visitFixedIdOrClassNamePart(ctx), ctx);
  }

  @Override
  public IdSelector visitIdSelector(IdSelectorContext ctx) {
    return handleComments(super.visitIdSelector(ctx), ctx);
  }

  @Override
  public CssClass visitCssClass(CssClassContext ctx) {
    return handleComments(super.visitCssClass(ctx), ctx);
  }

  @Override
  public ElementSubsequent visitCssClassOrId(CssClassOrIdContext ctx) {
    return handleComments(super.visitCssClassOrId(ctx), ctx);
  }

  @Override
  public ElementSubsequent visitElementSubsequent(ElementSubsequentContext ctx) {
    return handleComments(super.visitElementSubsequent(ctx), ctx);
  }

  @Override
  public ElementSubsequent visitAttribOrPseudo(AttribOrPseudoContext ctx) {
    return handleComments(super.visitAttribOrPseudo(ctx), ctx);
  }

  @Override
  public ElementSubsequent visitAttrib(AttribContext ctx) {
    return handleComments(super.visitAttrib(ctx), ctx);
  }

  @Override
  public SelectorOperator visitSelectorAttributeOperator(SelectorAttributeOperatorContext ctx) {
    return handleComments(super.visitSelectorAttributeOperator(ctx), ctx);
  }

  @Override
  public Pseudo visitPseudo(PseudoContext ctx) {
    return handleComments(super.visitPseudo(ctx), ctx);
  }

  @Override
  public ASTCssNode visitRuleSet(RuleSetContext ctx) {
    return handleComments(super.visitRuleSet(ctx), ctx);
  }

  @Override
  public Selector visitSelector(SelectorContext ctx) {
    return handleComments(super.visitSelector(ctx), ctx);
  }

  @Override
  public SelectorCombinator visitCombinator(CombinatorContext ctx) {
    return handleComments(super.visitCombinator(ctx), ctx);
  }

  @Override
  public SelectorCombinator visitCombinator_ws(Combinator_wsContext ctx) {
    return handleComments(super.visitCombinator_ws(ctx), ctx);
  }

  @Override
  public Selector visitNon_combinator_selector_block(Non_combinator_selector_blockContext ctx) {
    return handleComments(super.visitNon_combinator_selector_block(ctx), ctx);
  }

  @Override
  public SimpleSelector visitSimpleSelector(SimpleSelectorContext ctx) {
    return handleComments(super.visitSimpleSelector(ctx), ctx);
  }

  @Override
  public InterpolableName visitElementName(ElementNameContext ctx) {
    return handleComments(super.visitElementName(ctx), ctx);
  }

  @Override
  public ASTCssNode visitElementNamePart(ElementNamePartContext ctx) {
    return handleComments(super.visitElementNamePart(ctx), ctx);
  }

  @Override
  public ASTCssNode visitAllNumberKinds(AllNumberKindsContext ctx) {
    return handleComments(super.visitAllNumberKinds(ctx), ctx);
  }

  @Override
  public ASTCssNode visitPseudoparameter_termValue(Pseudoparameter_termValueContext ctx) {
    return handleComments(super.visitPseudoparameter_termValue(ctx), ctx);
  }

  @Override
  public EscapedSelector visitEscapedSelectorOldSyntax(EscapedSelectorOldSyntaxContext ctx) {
    return handleComments(super.visitEscapedSelectorOldSyntax(ctx), ctx);
  }

  @Override
  public ASTCssNode visitNestedAppender(NestedAppenderContext ctx) {
    return handleComments(super.visitNestedAppender(ctx), ctx);
  }

  @Override
  public ASTCssNode visitNth(NthContext ctx) {
    return handleComments(super.visitNth(ctx), ctx);
  }

  @Override
  public Expression visitNth_expression(Nth_expressionContext ctx) {
    return handleComments(super.visitNth_expression(ctx), ctx);
  }

  @Override
  public Expression visitNth_base(Nth_baseContext ctx) {
    return handleComments(super.visitNth_base(ctx), ctx);
  }

  @Override
  public ASTCssNode visitVariabledeclarationWithSemicolon(VariabledeclarationWithSemicolonContext ctx) {
    return handleComments(super.visitVariabledeclarationWithSemicolon(ctx), ctx);
  }

  @Override
  public ASTCssNode visitVariabledeclaration(VariabledeclarationContext ctx) {
    return handleComments(super.visitVariabledeclaration(ctx), ctx);
  }

  @Override
  public ASTCssNode visitReusableStructure(ReusableStructureContext ctx) {
    return handleComments(super.visitReusableStructure(ctx), ctx);
  }

  @Override
  public Guard visitGuard(GuardContext ctx) {
    return handleComments(super.visitGuard(ctx), ctx);
  }

  @Override
  public GuardCondition visitGuardCondition(GuardConditionContext ctx) {
    return handleComments(super.visitGuardCondition(ctx), ctx);
  }

  @Override
  public ComparisonExpressionOperator visitCompareOperator(CompareOperatorContext ctx) {
    return handleComments(super.visitCompareOperator(ctx), ctx);
  }

  @Override
  public GeneralBody visitReusableStructureArguments(ReusableStructureArgumentsContext ctx) {
    return handleComments(super.visitReusableStructureArguments(ctx), ctx);
  }

  @Override
  public GeneralBody visitCommaSplitReusableStructureArguments(CommaSplitReusableStructureArgumentsContext ctx) {
    return handleComments(super.visitCommaSplitReusableStructureArguments(ctx), ctx);
  }

  @Override
  public GeneralBody visitSemiSplitReusableStructureArguments(SemiSplitReusableStructureArgumentsContext ctx) {
    return handleComments(super.visitSemiSplitReusableStructureArguments(ctx), ctx);
  }

  @Override
  public ASTCssNode visitRsParameter_no_comma(RsParameter_no_commaContext ctx) {
    return handleComments(super.visitRsParameter_no_comma(ctx), ctx);
  }

  @Override
  public ASTCssNode visitRsParameter_with_comma(RsParameter_with_commaContext ctx) {
    return handleComments(super.visitRsParameter_with_comma(ctx), ctx);
  }

  @Override
  public ASTCssNode visitRsParameterWithoutDefault(RsParameterWithoutDefaultContext ctx) {
    return handleComments(super.visitRsParameterWithoutDefault(ctx), ctx);
  }

  @Override
  public ASTCssNode visitRsParameterWithDefault_no_comma(RsParameterWithDefault_no_commaContext ctx) {
    return handleComments(super.visitRsParameterWithDefault_no_comma(ctx), ctx);
  }

  @Override
  public ASTCssNode visitRsParameterWithDefault_with_comma(RsParameterWithDefault_with_commaContext ctx) {
    return handleComments(super.visitRsParameterWithDefault_with_comma(ctx), ctx);
  }

  @Override
  public ASTCssNode visitCollector(CollectorContext ctx) {
    return handleComments(super.visitCollector(ctx), ctx);
  }

  @Override
  public ASTCssNode visitRsPattern(RsPatternContext ctx) {
    return handleComments(super.visitRsPattern(ctx), ctx);
  }

  @Override
  public ASTCssNode visitRsAtName(RsAtNameContext ctx) {
    return handleComments(super.visitRsAtName(ctx), ctx);
  }

  @Override
  public ReusableStructureName visitReusableStructureName(ReusableStructureNameContext ctx) {
    return handleComments(super.visitReusableStructureName(ctx), ctx);
  }

  @Override
  public MixinReference visitMixinReferenceWithSemi(MixinReferenceWithSemiContext ctx) {
    return handleComments(super.visitMixinReferenceWithSemi(ctx), ctx);
  }

  @Override
  public MixinReference visitMixinReference(MixinReferenceContext ctx) {
    return handleComments(super.visitMixinReference(ctx), ctx);
  }

  @Override
  public MixinReference visitNamespaceReference(NamespaceReferenceContext ctx) {
    return handleComments(super.visitNamespaceReference(ctx), ctx);
  }

  @Override
  public MixinReference visitNamespaceReferenceWithSemi(NamespaceReferenceWithSemiContext ctx) {
    return handleComments(super.visitNamespaceReferenceWithSemi(ctx), ctx);
  }

  @Override
  public ASTCssNode visitReferenceSeparator(ReferenceSeparatorContext ctx) {
    return handleComments(super.visitReferenceSeparator(ctx), ctx);
  }

  @Override
  public GeneralBody visitMixinReferenceArguments(MixinReferenceArgumentsContext ctx) {
    return handleComments(super.visitMixinReferenceArguments(ctx), ctx);
  }

  @Override
  public GeneralBody visitSemiSplitMixinReferenceArguments(SemiSplitMixinReferenceArgumentsContext ctx) {
    return handleComments(super.visitSemiSplitMixinReferenceArguments(ctx), ctx);
  }

  @Override
  public GeneralBody visitCommaSplitMixinReferenceArguments(CommaSplitMixinReferenceArgumentsContext ctx) {
    return handleComments(super.visitCommaSplitMixinReferenceArguments(ctx), ctx);
  }

  @Override
  public ASTCssNode visitMixinReferenceArgument_with_comma(MixinReferenceArgument_with_commaContext ctx) {
    return handleComments(super.visitMixinReferenceArgument_with_comma(ctx), ctx);
  }

  @Override
  public ASTCssNode visitMixinReferenceArgument_no_comma(MixinReferenceArgument_no_commaContext ctx) {
    return handleComments(super.visitMixinReferenceArgument_no_comma(ctx), ctx);
  }

  @Override
  public DetachedRuleset visitDetachedRuleset(DetachedRulesetContext ctx) {
    return handleComments(super.visitDetachedRuleset(ctx), ctx);
  }

  @Override
  public DetachedRulesetReference visitDetachedRulesetReference(DetachedRulesetReferenceContext ctx) {
    return handleComments(super.visitDetachedRulesetReference(ctx), ctx);
  }

  @Override
  public ASTCssNode visitExtendTargetSelectors(ExtendTargetSelectorsContext ctx) {
    return handleComments(super.visitExtendTargetSelectors(ctx), ctx);
  }

  @Override
  public Extend visitExtendInDeclarationWithSemi(ExtendInDeclarationWithSemiContext ctx) {
    return handleComments(super.visitExtendInDeclarationWithSemi(ctx), ctx);
  }

  @Override
  public GeneralBody visitTop_level_body_with_declaration(Top_level_body_with_declarationContext ctx) {
    return handleComments(super.visitTop_level_body_with_declaration(ctx), ctx);
  }

  @Override
  public GeneralBody visitTop_level_body(Top_level_bodyContext ctx) {
    return handleComments(super.visitTop_level_body(ctx), ctx);
  }

  @Override
  public Media visitMedia_top_level(Media_top_levelContext ctx) {
    return handleComments(super.visitMedia_top_level(ctx), ctx);
  }

  @Override
  public Media visitMedia_in_general_body(Media_in_general_bodyContext ctx) {
    return handleComments(super.visitMedia_in_general_body(ctx), ctx);
  }

  @Override
  public MediaQuery visitMediaQuery(MediaQueryContext ctx) {
    return handleComments(super.visitMediaQuery(ctx), ctx);
  }

  @Override
  public Medium visitMedium(MediumContext ctx) {
    return handleComments(super.visitMedium(ctx), ctx);
  }

  @Override
  public MediaExpression visitMediaExpression(MediaExpressionContext ctx) {
    return handleComments(super.visitMediaExpression(ctx), ctx);
  }

  @Override
  public MediaExpression visitInterpolatedMediaExpression(InterpolatedMediaExpressionContext ctx) {
    return handleComments(super.visitInterpolatedMediaExpression(ctx), ctx);
  }

  @Override
  public MediaExpression visitCssMediaExpression(CssMediaExpressionContext ctx) {
    return handleComments(super.visitCssMediaExpression(ctx), ctx);
  }

  @Override
  public MediaExpressionFeature visitMediaFeature(MediaFeatureContext ctx) {
    return handleComments(super.visitMediaFeature(ctx), ctx);
  }

  @Override
  public ASTCssNode visitTop_level_body_with_declaration_member(Top_level_body_with_declaration_memberContext ctx) {
    return handleComments(super.visitTop_level_body_with_declaration_member(ctx), ctx);
  }

  @Override
  public ASTCssNode visitImports(ImportsContext ctx) {
    return handleComments(super.visitImports(ctx), ctx);
  }

  @Override
  public ASTCssNode visitImportoptions(ImportoptionsContext ctx) {
    return handleComments(super.visitImportoptions(ctx), ctx);
  }

  @Override
  public ASTCssNode visitKeyframes(KeyframesContext ctx) {
    return handleComments(super.visitKeyframes(ctx), ctx);
  }

  @Override
  public KeyframesName visitKeyframesname(KeyframesnameContext ctx) {
    return handleComments(super.visitKeyframesname(ctx), ctx);
  }

  @Override
  public FontFace visitFontface(FontfaceContext ctx) {
    return handleComments(super.visitFontface(ctx), ctx);
  }

  @Override
  public Document visitDocument(DocumentContext ctx) {
    return handleComments(super.visitDocument(ctx), ctx);
  }

  @Override
  public Viewport visitViewport(ViewportContext ctx) {
    return handleComments(super.visitViewport(ctx), ctx);
  }

  @Override
  public Supports visitSupports(SupportsContext ctx) {
    return handleComments(super.visitSupports(ctx), ctx);
  }

  @Override
  public SupportsCondition visitSupportsCondition(SupportsConditionContext ctx) {
    return handleComments(super.visitSupportsCondition(ctx), ctx);
  }

  @Override
  public SupportsCondition visitSscDeclaration(SscDeclarationContext ctx) {
    return handleComments(super.visitSscDeclaration(ctx), ctx);
  }

  @Override
  public SupportsCondition visitSscNotCondition(SscNotConditionContext ctx) {
    return handleComments(super.visitSscNotCondition(ctx), ctx);
  }

  @Override
  public SupportsCondition visitSscNestedCondition(SscNestedConditionContext ctx) {
    return handleComments(super.visitSscNestedCondition(ctx), ctx);
  }

  @Override
  public SupportsCondition visitSupportsQuery(SupportsQueryContext ctx) {
    return handleComments(super.visitSupportsQuery(ctx), ctx);
  }

  @Override
  public ASTCssNode visitPage(PageContext ctx) {
    return handleComments(super.visitPage(ctx), ctx);
  }

  @Override
  public Name visitPseudoPage(PseudoPageContext ctx) {
    return handleComments(super.visitPseudoPage(ctx), ctx);
  }

  @Override
  public ASTCssNode visitCharSet(CharSetContext ctx) {
    return handleComments(super.visitCharSet(ctx), ctx);
  }

  @Override
  public ASTCssNode visitUnknownAtRule(UnknownAtRuleContext ctx) {
    return handleComments(super.visitUnknownAtRule(ctx), ctx);
  }

  @Override
  public ListExpression visitUnknownAtRuleNamesSet(UnknownAtRuleNamesSetContext ctx) {
    return handleComments(super.visitUnknownAtRuleNamesSet(ctx), ctx);
  }

  @Override
  public PageMarginBox visitPageMarginBox(PageMarginBoxContext ctx) {
    return handleComments(super.visitPageMarginBox(ctx), ctx);
  }

  @Override
  public ASTCssNode visitReusableStructureGuards(ReusableStructureGuardsContext ctx) {
    return handleComments(super.visitReusableStructureGuards(ctx), ctx);
  }

  protected <T extends ASTCssNode> T handleComments(T node, ParserRuleContext ctx) {
    inheritCommentsFromToken(node, ctx);

    List<ParseTree> allChildren = getChildren(ctx);
    Map<ParseTree, ASTCssNode> map = childsNodesMap(node);
    //order childs nodes map - assign numbers according to which child it is

    ASTCssNode previous = null;
    List<CommonToken> unsedComments = new ArrayList<CommonToken>();
    for (ParseTree parseTree : allChildren) {
      NodeCommentsHolder comments = treeComments.getOrCreate(parseTree);
      if (!comments.isEmpty()) {
        System.out.println("Not Emptyyyyyyyyyyyyyyyyyyyyy " + parseTree);
        if (previous != null) {
          previous.addTrailingComments(convertToComments(comments.getPreceding()));
        } else {
          unsedComments.addAll(comments.getPreceding());
        }
        unsedComments.addAll(comments.getOrphans());
        unsedComments.addAll(comments.getFollowing());
      } else {
        System.out.println("????" + parseTree);
        if (map.containsKey(parseTree)) {
          previous = map.get(parseTree);
          previous.addBeforeOpeningComments(convertToComments(unsedComments));
        }
      }
    }
    return node;
  }

  private <T extends ASTCssNode> Map<ParseTree, ASTCssNode> childsNodesMap(T node) {
    List<? extends ASTCssNode> childs = node.getChilds();
    Map<ParseTree, ASTCssNode> map = new HashMap<ParseTree, ASTCssNode>();
    for (ASTCssNode kid : childs) {
      if (kid == null)
        throw new BugHappened("No child should be null.", node);

      HiddenTokenAwareTree underlyingStructure = kid.getUnderlyingStructure();
      if (underlyingStructure instanceof HiddenTokenAwareTreeAdapter) {
        HiddenTokenAwareTreeAdapter adapter = (HiddenTokenAwareTreeAdapter) underlyingStructure;
        ParseTree underlyingNode = adapter.getUnderlyingNode();
        map.put(underlyingNode, kid);
      }
    }
    return map;
  }

  private List<ParseTree> getChildren(ParserRuleContext ctx) {
    List<ParseTree> treeChildren = new ArrayList<ParseTree>();
    int ctxCnt = ctx.getChildCount();
    for (int i = 0; i < ctxCnt; i++) {
      ParseTree child = ctx.getChild(i);
      treeChildren.add(child);
    }
    return treeChildren;
  }

  private void inheritCommentsFromToken(ASTCssNode node, ParserRuleContext ctx) {
    NodeCommentsHolder comments = treeComments.getOrCreate(ctx);
    List<Comment> preceding = convertToComments(comments.getPreceding());
    node.setOpeningComments(preceding);

    List<Comment> following = convertToComments(comments.getFollowing());
    node.setTrailingComments(following);

    List<Comment> orphans = convertToComments(comments.getOrphans());
    node.setOrphanComments(orphans);

    comments.removeAll();
  }

  private List<Comment> convertToComments(List<CommonToken> list) {
    List<Comment> result = new ArrayList<Comment>();

    Comment comment = null;
    for (CommonToken token : list) {
      if (token.getType() == LessG4Lexer.COMMENT) {
        comment = new Comment(new HiddenTokenAwareTreeAdapter(token));
        result.add(comment);
      }
      if (token.getType() == LessG4Lexer.NEW_LINE) {
        if (comment != null)
          comment.setHasNewLine(true);
      }
    }

    return result;
  }

}
