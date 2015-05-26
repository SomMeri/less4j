package com.github.sommeri.less4j.antlr4.core.parser;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ArgumentDeclaration;
import com.github.sommeri.less4j.core.ast.BinaryExpression;
import com.github.sommeri.less4j.core.ast.BinaryExpressionOperator;
import com.github.sommeri.less4j.core.ast.CharsetDeclaration;
import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.ComparisonExpression;
import com.github.sommeri.less4j.core.ast.ComparisonExpressionOperator;
import com.github.sommeri.less4j.core.ast.CssClass;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.Declaration;
import com.github.sommeri.less4j.core.ast.DetachedRuleset;
import com.github.sommeri.less4j.core.ast.DetachedRulesetReference;
import com.github.sommeri.less4j.core.ast.Document;
import com.github.sommeri.less4j.core.ast.ElementSubsequent;
import com.github.sommeri.less4j.core.ast.EmbeddedScript;
import com.github.sommeri.less4j.core.ast.EmptyExpression;
import com.github.sommeri.less4j.core.ast.EscapedSelector;
import com.github.sommeri.less4j.core.ast.EscapedValue;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.Extend;
import com.github.sommeri.less4j.core.ast.FaultyExpression;
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
import com.github.sommeri.less4j.core.ast.Import.ImportContent;
import com.github.sommeri.less4j.core.ast.InterpolableName;
import com.github.sommeri.less4j.core.ast.InterpolableNamePart;
import com.github.sommeri.less4j.core.ast.InterpolatedMediaExpression;
import com.github.sommeri.less4j.core.ast.Keyframes;
import com.github.sommeri.less4j.core.ast.KeyframesName;
import com.github.sommeri.less4j.core.ast.KeywordExpression;
import com.github.sommeri.less4j.core.ast.ListExpression;
import com.github.sommeri.less4j.core.ast.ListExpressionOperator;
import com.github.sommeri.less4j.core.ast.ListExpressionOperator.Operator;
import com.github.sommeri.less4j.core.ast.Media;
import com.github.sommeri.less4j.core.ast.MediaExpression;
import com.github.sommeri.less4j.core.ast.MediaExpressionFeature;
import com.github.sommeri.less4j.core.ast.MediaQuery;
import com.github.sommeri.less4j.core.ast.Medium;
import com.github.sommeri.less4j.core.ast.MediumModifier;
import com.github.sommeri.less4j.core.ast.MediumType;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.MultiTargetExtend;
import com.github.sommeri.less4j.core.ast.Name;
import com.github.sommeri.less4j.core.ast.NamedColorExpression;
import com.github.sommeri.less4j.core.ast.NamedExpression;
import com.github.sommeri.less4j.core.ast.NestedSelectorAppender;
import com.github.sommeri.less4j.core.ast.Nth;
import com.github.sommeri.less4j.core.ast.Nth.Form;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.Page;
import com.github.sommeri.less4j.core.ast.PageMarginBox;
import com.github.sommeri.less4j.core.ast.ParenthesesExpression;
import com.github.sommeri.less4j.core.ast.Pseudo;
import com.github.sommeri.less4j.core.ast.PseudoClass;
import com.github.sommeri.less4j.core.ast.PseudoElement;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.ReusableStructureName;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.ast.SelectorAttribute;
import com.github.sommeri.less4j.core.ast.SelectorCombinator;
import com.github.sommeri.less4j.core.ast.SelectorCombinator.CombinatorType;
import com.github.sommeri.less4j.core.ast.SelectorOperator;
import com.github.sommeri.less4j.core.ast.SelectorPart;
import com.github.sommeri.less4j.core.ast.SignedExpression;
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
import com.github.sommeri.less4j.core.ast.UnknownAtRule;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.ast.VariableDeclaration;
import com.github.sommeri.less4j.core.ast.VariableNamePart;
import com.github.sommeri.less4j.core.ast.Viewport;
import com.github.sommeri.less4j.core.compiler.stages.AstLogic;
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
import com.github.sommeri.less4j.core.parser.LessG4Parser.SimpleSupportsConditionContext;
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

  private static String EXTEND_PSEUDO = "extend";
  private final static String EXTEND_ALL_KEYWORD = "all";

  private final static String IMPORT_OPTION_REFERENCE = "reference";
  private final static String IMPORT_OPTION_INLINE = "inline";
  private final static String IMPORT_OPTION_LESS = "less";
  private final static String IMPORT_OPTION_CSS = "css";
  private final static String IMPORT_OPTION_ONCE = "once";
  private final static String IMPORT_OPTION_MULTIPLE = "multiple";
  private final static String IMPORT_OPTION_OPTIONAL = "optional";

  private final ProblemsHandler problemsHandler;
  private final Antlr4_TermBuilder termBuilder;
  private final TreeComments treeComments;

  protected Antlr4_ASTBuilderSwitch(ProblemsHandler problemsHandler, TreeComments treeComments) {
    this.problemsHandler = problemsHandler;
    this.treeComments = treeComments;
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
  public Declaration visitDeclaration(DeclarationContext ctx) {
    // FIXME (antlr4) (comments) push comments to used tokens
    TerminalNode colon = ctx.COLON();

    InterpolableName propertyName = visitProperty(ctx.property());
    ListExpressionOperator.Operator mergeOperator = toDeclarationMergeOperator(ctx);
    Expression_fullContext valueCtx = ctx.expression_full();
    Expression expression = valueCtx == null ? null : visitExpression_full(valueCtx);

    Declaration declaration = new Declaration(new HiddenTokenAwareTreeAdapter(ctx), propertyName, expression, mergeOperator);
    return declaration;
  }

  @Override
  public ASTCssNode visitDeclarationWithSemicolon(DeclarationWithSemicolonContext ctx) {
    DeclarationContext declarationCtx = ctx.declaration();
    ParseTree previous = declarationCtx.getChild(declarationCtx.getChildCount()-1);
    treeComments.moveHidden(previous, ctx.SEMI(), null);

    Declaration result = visitDeclaration(declarationCtx);
    return result;
  }

  private ListExpressionOperator.Operator toDeclarationMergeOperator(DeclarationContext ctx) {
    if (ctx.UNDERSCORE() != null)
      return ListExpressionOperator.Operator.EMPTY_OPERATOR;
    if (ctx.PLUS() != null)
      return ListExpressionOperator.Operator.COMMA;

    return null;
  }

  @Override
  public InterpolableName visitProperty(PropertyContext ctx) {
    HiddenTokenAwareTreeAdapter token = new HiddenTokenAwareTreeAdapter(ctx);

    InterpolableName result = new InterpolableName(token);
    TerminalNode starCtx = ctx.STAR();
    if (starCtx != null) {
      InterpolableNamePart part = toInterpolableNamePart(starCtx);
      result.add(part);
    }
    List<PropertyNamePartContext> propertyNamePart = ctx.propertyNamePart();
    for (PropertyNamePartContext pnpCtx : propertyNamePart) {
      InterpolableNamePart part = visitPropertyNamePart(pnpCtx);
      result.add(part);
    }
    return result;
  }

  @Override
  public InterpolableNamePart visitPropertyNamePart(PropertyNamePartContext ctx) {
    return toInterpolableNamePart(ctx);
  }

  private InterpolableName toInterpolableName(ParserRuleContext ctx) {
    return toInterpolableName(ctx, 0);
  }

  private InterpolableName toInterpolableName(ParserRuleContext ctx, int startIndex) {
    InterpolableName result = new InterpolableName(new HiddenTokenAwareTreeAdapter(ctx));
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
      HiddenTokenAwareTreeAdapter underlyingStructure = new HiddenTokenAwareTreeAdapter(terminalNode);
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
    //FIXME: (antlr4) print type by name
    throw new BugHappened(SHOULD_NOT_VISIT + ". Terminal " + node.getSymbol().getType() + " " + node.getText(), new HiddenTokenAwareTreeAdapter(node));
  }

  @Override
  public GeneralBody visitGeneral_body(General_bodyContext ctx) {
    // FIXME (antlr) (comments) heavy comments handling in original
    TerminalNode lbraceToken = ctx.LBRACE();
    SyntaxOnlyElement lbrace = new SyntaxOnlyElement(new HiddenTokenAwareTreeAdapter(lbraceToken), lbraceToken.getText().trim());
    TerminalNode rbraceToken = ctx.RBRACE();
    SyntaxOnlyElement rbrace = new SyntaxOnlyElement(new HiddenTokenAwareTreeAdapter(rbraceToken), rbraceToken.getText().trim());

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
  public Variable visitVariablereference(VariablereferenceContext ctx) {
    VariablenameContext variablename = ctx.variablename();
    if (null != variablename)
      return visitVariablename(variablename);

    TerminalNode indirect = ctx.INDIRECT_VARIABLE();
    if (null != indirect) {
      return termBuilder.buildFromIndirectVariable(ctx, indirect);
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
  public Expression visitMathExprHighPrior(MathExprHighPriorContext ctx) {
    //    public Expression visitMathExprHighPriorNoWhitespaceList(MathExprHighPriorNoWhitespaceListContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);

    Iterator<MathExprLowPriorContext> expressionCtx = ctx.mathExprLowPrior().iterator();
    Expression result = visitMathExprLowPrior(expressionCtx.next());

    Iterator<PlusOrMinusContext> operators = ctx.plusOrMinus().iterator();
    while (expressionCtx.hasNext() && operators.hasNext()) {
      BinaryExpressionOperator operator = visitPlusOrMinus(operators.next());
      Expression value = visitMathExprLowPrior(expressionCtx.next());
      result = new BinaryExpression(token, result, operator, value);
    }
    if (expressionCtx.hasNext() || operators.hasNext())
      throw new BugHappened(GRAMMAR_MISMATCH, token);

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

    BinaryExpressionOperator.Operator operator = toExpressionOperator((TerminalNode)node);
    return new BinaryExpressionOperator(new HiddenTokenAwareTreeAdapter(ctx), operator);
  }

  private BinaryExpressionOperator.Operator toExpressionOperator(TerminalNode node) {
    Token token = ((TerminalNode) node).getSymbol();
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

    throw new BugHappened(GRAMMAR_MISMATCH, new HiddenTokenAwareTreeAdapter(node));
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
  public FunctionExpression visitTerm_only_function(Term_only_functionContext ctx) {
    return (FunctionExpression) ctx.getChild(0).accept(this);
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
    SelectorOperator operator = operatorCntx != null ? visitSelectorAttributeOperator(operatorCntx) : new SelectorOperator(new HiddenTokenAwareTreeAdapter(ctx), SelectorOperator.Operator.NONE);

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
  public Pseudo visitPseudo(PseudoContext ctx) {
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
    if (isTokenOfType(parameter, LessG4Lexer.INTERPOLATED_VARIABLE))
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

    ReusableStructureGuardsContext guardsCtx = ctx.reusableStructureGuards();
    if (guardsCtx != null) {
      List<Guard> guards = doVisitReusableStructureGuards(guardsCtx);
      ruleSet.addGuards(guards);
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

    boolean first = true;
    if (ctx.leading == null && blocks.hasNext()) {
      Non_combinator_selector_blockContext blockCtx = blocks.next();
      Selector block = visitNon_combinator_selector_block(blockCtx);
      //FIXME: (antlr4) it should be possible to simplify this
      for (SelectorPart selectorPart : block.getParts()) {
        addPart(result, selectorPart);
      }
      first = false;
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
      if (!first || leadingCombinator.getCombinatorType() != CombinatorType.DESCENDANT)
        parts.get(0).setLeadingCombinator(leadingCombinator);

      //FIXME: (antlr4) it should be possible to simplify this
      for (SelectorPart selectorPart : parts) {
        addPart(result, selectorPart);
      }
      
      //result.addParts(parts);
      first = false;
    }

    if (combinators.hasNext() || blocks.hasNext()) {
      throw new BugHappened(GRAMMAR_MISMATCH, new HiddenTokenAwareTreeAdapter(ctx));
    }

    return result;
  }

  private void addPart(Selector selector, SelectorPart part) {
    ElementSubsequent lastSubsequent = part.getLastSubsequent();
    while (lastSubsequent!=null && isExtends(lastSubsequent)) {
      convertAndAddExtends(selector, (PseudoClass) lastSubsequent);
      part.removeSubsequent(lastSubsequent);
      lastSubsequent = part.getLastSubsequent();
    } 
    
    //if the part had only extend as members
    if (!part.isEmpty())
      selector.addPart(part);
  }
  
  private void convertAndAddExtends(Selector selector, PseudoClass extendPC) {
    ASTCssNode parameter = extendPC.getParameter();
    if (parameter.getType()==ASTCssNodeType.EXTEND) {
      selector.addExtend((Extend) parameter);
    } else if (parameter.getType()==ASTCssNodeType.MULTI_TARGET_EXTEND) {
      MultiTargetExtend extend = (MultiTargetExtend) parameter;
      for (Extend node : extend.getAllExtends()) {
        selector.addExtend((Extend) node);
      }
    } else {
      throw new BugHappened(GRAMMAR_MISMATCH, parameter.getUnderlyingStructure());
    }
  }

  private boolean isExtends(ElementSubsequent subsequent) {
    return (subsequent instanceof PseudoClass) && EXTEND_PSEUDO.equals(subsequent.getName());
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
    HiddenTokenAwareTree valueToken = new HiddenTokenAwareTreeAdapter(ctx.VALUE_ESCAPE());
    String quotedText = ctx.VALUE_ESCAPE().getText();
    return new EscapedSelector(valueToken, quotedText.substring(2, quotedText.length() - 1), "" + quotedText.charAt(1), null);
  }

  @Override
  public ASTCssNode visitNestedAppender(NestedAppenderContext ctx) {
    //FIXME: (antlr4) (deal with spaces before and after)
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);

    //FIXME: (antlr4) (deal with spaces before and after and remove these two options - only combiner should influence it)
    boolean directlyBefore = true;
    boolean directlyAfter = true;

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
    Expression_fullContext valueCtx = ctx.expression_full();
    Expression value = valueCtx == null ? new EmptyExpression(token) : visitExpression_full(valueCtx);
    return new VariableDeclaration(token, variable, value);
  }

  @Override
  public ASTCssNode visitReusableStructure(ReusableStructureContext ctx) {
    HiddenTokenAwareTreeAdapter token = new HiddenTokenAwareTreeAdapter(ctx);
    ReusableStructure result = new ReusableStructure(token);

    result.addName(visitReusableStructureName(ctx.reusableStructureName()));
    ReusableStructureArgumentsContext argumentsCtx = ctx.reusableStructureArguments();
    if (argumentsCtx != null) {
      GeneralBody arguments = visitReusableStructureArguments(argumentsCtx);
      for (ASTCssNode member : arguments.getMembers()) {
        result.addParameter(member);
      }
    }

    ReusableStructureGuardsContext guardsCtx = ctx.reusableStructureGuards();
    if (guardsCtx != null) {
      List<Guard> guards = doVisitReusableStructureGuards(guardsCtx);
      result.addGuards(guards);
    }

    result.setBody(visitGeneral_body(ctx.general_body()));

    return result;
  }

  @Override
  public Guard visitGuard(GuardContext ctx) {
    HiddenTokenAwareTreeAdapter token = new HiddenTokenAwareTreeAdapter(ctx);
    Guard result = new Guard(token);
    List<GuardConditionContext> conditions = ctx.guardCondition();
    if (conditions != null) {
      for (GuardConditionContext condition : conditions) {
        result.addCondition(visitGuardCondition(condition));
      }
    }

    return result;
  }

  @Override
  public GuardCondition visitGuardCondition(GuardConditionContext ctx) {
    // (ident ws)? LPAREN ws mathExprHighPrior (ws compareOperator ws mathExprHighPrior)? ws RPAREN;
    HiddenTokenAwareTreeAdapter token = new HiddenTokenAwareTreeAdapter(ctx);

    boolean isNegated = false;
    IdentContext negationCtx = ctx.ident();
    if (negationCtx != null) {
      validateGuardNegation(negationCtx);
      isNegated = true;
    }

    Expression condition = visitMathExprHighPrior(ctx.leftE);
    if (ctx.rightE != null) {
      Expression rightE = visitMathExprHighPrior(ctx.rightE);
      condition = new ComparisonExpression(token, condition, visitCompareOperator(ctx.compareOperator()), rightE);
    }

    return new GuardCondition(token, isNegated, condition);
  }

  @Override
  public ComparisonExpressionOperator visitCompareOperator(CompareOperatorContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    ParseTree child = ctx.getChild(0);
    if (!(child instanceof TerminalNode))
      throw new BugHappened(GRAMMAR_MISMATCH, token);

    TerminalNode terminal = (TerminalNode) child;
    switch (terminal.getSymbol().getType()) {
    case LessG4Lexer.GREATER:
      return new ComparisonExpressionOperator(token, ComparisonExpressionOperator.Operator.GREATER);

    case LessG4Lexer.GREATER_OR_EQUAL:
      return new ComparisonExpressionOperator(token, ComparisonExpressionOperator.Operator.GREATER_OR_EQUAL);

    case LessG4Lexer.OPEQ:
      return new ComparisonExpressionOperator(token, ComparisonExpressionOperator.Operator.OPEQ);

    case LessG4Lexer.LOWER_OR_EQUAL:
      return new ComparisonExpressionOperator(token, ComparisonExpressionOperator.Operator.LOWER_OR_EQUAL);

    case LessG4Lexer.LOWER:
      return new ComparisonExpressionOperator(token, ComparisonExpressionOperator.Operator.LOWER);

    default:
      break;
    }

    throw new BugHappened(GRAMMAR_MISMATCH, token);
  }

  public void validateGuardNegation(IdentContext ctx) {
    String operator = ctx.getText().trim();
    if (!"not".equals(operator))
      throw new BugHappened(GRAMMAR_MISMATCH, new HiddenTokenAwareTreeAdapter(ctx));
  }

  @Override
  public GeneralBody visitReusableStructureArguments(ReusableStructureArgumentsContext ctx) {
    GeneralBody arguments = null;
    SemiSplitReusableStructureArgumentsContext semiSplit = ctx.semiSplitReusableStructureArguments();
    if (semiSplit != null) {
      return visitSemiSplitReusableStructureArguments(semiSplit);
    }
    CommaSplitReusableStructureArgumentsContext commaSplit = ctx.commaSplitReusableStructureArguments();
    return visitCommaSplitReusableStructureArguments(commaSplit);
  }

  @Override
  public GeneralBody visitCommaSplitReusableStructureArguments(CommaSplitReusableStructureArgumentsContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    GeneralBody result = new GeneralBody(token);
    List<RsParameter_no_commaContext> parameters = ctx.rsParameter_no_comma();
    for (RsParameter_no_commaContext parameter : parameters) {
      result.addMember(parameter.accept(this));
    }
    return result;
  }

  @Override
  public GeneralBody visitSemiSplitReusableStructureArguments(SemiSplitReusableStructureArgumentsContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    GeneralBody result = new GeneralBody(token);
    List<RsParameter_with_commaContext> parameters = ctx.rsParameter_with_comma();
    for (RsParameter_with_commaContext parameter : parameters) {
      result.addMember(parameter.accept(this));
    }
    return result;
  }

  @Override
  public ASTCssNode visitRsParameter_no_comma(RsParameter_no_commaContext ctx) {
    return ctx.getChild(0).accept(this);
  }

  @Override
  public ASTCssNode visitRsParameter_with_comma(RsParameter_with_commaContext ctx) {
    return ctx.getChild(0).accept(this);
  }

  @Override
  public ASTCssNode visitRsParameterWithoutDefault(RsParameterWithoutDefaultContext ctx) {
    //atName | collector | rsPattern;
    return ctx.getChild(0).accept(this);
  }

  @Override
  public ASTCssNode visitRsParameterWithDefault_no_comma(RsParameterWithDefault_no_commaContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);

    TerminalNode nameCtx = ctx.AT_NAME();
    Variable name = new Variable(new HiddenTokenAwareTreeAdapter(nameCtx), nameCtx.getText());
    Expression value = visitExpression_space_separated_list(ctx.value);

    return new ArgumentDeclaration(token, name, value);
  }

  @Override
  public ASTCssNode visitRsParameterWithDefault_with_comma(RsParameterWithDefault_with_commaContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);

    TerminalNode nameCtx = ctx.AT_NAME();
    Variable name = new Variable(new HiddenTokenAwareTreeAdapter(nameCtx), nameCtx.getText());
    Expression value = visitExpression_full(ctx.value);

    return new ArgumentDeclaration(token, name, value);
  }

  @Override
  public ASTCssNode visitCollector(CollectorContext ctx) {
    HiddenTokenAwareTreeAdapter token = new HiddenTokenAwareTreeAdapter(ctx);
    return new ArgumentDeclaration(token, new Variable(token, "@"), null, true);
  }

  @Override
  public ASTCssNode visitRsPattern(RsPatternContext ctx) {
    //    rsPattern: (( (plusOrMinus ws)? (value_term)
    //        ) | (
    //           unsigned_value_term
    //           | hexColor
    //        ))
    //        ;
    PlusOrMinusContext sign = ctx.sign;
    //last child is value
    int valueIndx = ctx.getChildCount() - 1;
    ParseTree value = ctx.getChild(valueIndx);
    Expression expression = termBuilder.buildFromSignedTerm(sign, value);
    return expression;
  }

  @Override
  public ASTCssNode visitRsAtName(RsAtNameContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);

    TerminalNode nameCtx = ctx.AT_NAME();
    Variable name = new Variable(new HiddenTokenAwareTreeAdapter(nameCtx), nameCtx.getText());

    return new ArgumentDeclaration(token, name, null, ctx.DOT3() != null);
  }

  @Override
  public ReusableStructureName visitReusableStructureName(ReusableStructureNameContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    ElementSubsequent name = visitCssClassOrId(ctx.cssClassOrId());
    return new ReusableStructureName(token, name);
  }

  @Override
  public MixinReference visitMixinReferenceWithSemi(MixinReferenceWithSemiContext ctx) {
    return visitMixinReference(ctx.mixinReference());
  }

  @Override
  public MixinReference visitMixinReference(MixinReferenceContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    //reusableStructureName (ws LPAREN (ws mixinReferenceArguments)? ws RPAREN ws)? (ws IMPORTANT_SYM)?;
    MixinReference result = new MixinReference(token);

    ReusableStructureName name = visitReusableStructureName(ctx.reusableStructureName());
    result.setFinalName(name);

    MixinReferenceArgumentsContext argumentsCtx = ctx.mixinReferenceArguments();
    if (argumentsCtx != null) {
      GeneralBody arguments = visitMixinReferenceArguments(argumentsCtx);
      for (ASTCssNode child : arguments.getMembers()) {
        if (child instanceof Expression) {
          Expression expression = (Expression) child;
          result.addPositionalParameter(expression);
        } else if (child instanceof VariableDeclaration) {
          VariableDeclaration namedValue = (VariableDeclaration) child;
          result.addNamedParameter(namedValue);
        }
      }
    }

    if (null != ctx.IMPORTANT_SYM())
      result.setImportant(true);

    return result;
  }

  @Override
  public MixinReference visitNamespaceReference(NamespaceReferenceContext ctx) {
    MixinReference reference = visitMixinReference(ctx.mixinReference());
    List<ReusableStructureNameContext> reusableStructureName = ctx.reusableStructureName();
    for (ReusableStructureNameContext name : reusableStructureName) {
      reference.addName(visitReusableStructureName(name));
    }

    return reference;
  }

  @Override
  public MixinReference visitNamespaceReferenceWithSemi(NamespaceReferenceWithSemiContext ctx) {
    return visitNamespaceReference(ctx.namespaceReference());
  }

  @Override
  public ASTCssNode visitReferenceSeparator(ReferenceSeparatorContext ctx) {
    throw new BugHappened(SHOULD_NOT_VISIT, new HiddenTokenAwareTreeAdapter(ctx));
  }

  @Override
  public GeneralBody visitMixinReferenceArguments(MixinReferenceArgumentsContext ctx) {
    SemiSplitMixinReferenceArgumentsContext semiSplit = ctx.semiSplitMixinReferenceArguments();
    if (semiSplit != null)
      return visitSemiSplitMixinReferenceArguments(semiSplit);

    return visitCommaSplitMixinReferenceArguments(ctx.commaSplitMixinReferenceArguments());
  }

  @Override
  public GeneralBody visitSemiSplitMixinReferenceArguments(SemiSplitMixinReferenceArgumentsContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    GeneralBody result = new GeneralBody(token);
    List<MixinReferenceArgument_with_commaContext> parameters = ctx.mixinReferenceArgument_with_comma();
    for (MixinReferenceArgument_with_commaContext parameter : parameters) {
      result.addMember(parameter.accept(this));
    }
    return result;
  }

  @Override
  public GeneralBody visitCommaSplitMixinReferenceArguments(CommaSplitMixinReferenceArgumentsContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    GeneralBody result = new GeneralBody(token);
    List<MixinReferenceArgument_no_commaContext> parameters = ctx.mixinReferenceArgument_no_comma();
    for (MixinReferenceArgument_no_commaContext parameter : parameters) {
      result.addMember(parameter.accept(this));
    }
    return result;
  }

  @Override
  public ASTCssNode visitMixinReferenceArgument_with_comma(MixinReferenceArgument_with_commaContext ctx) {
    Expression_fullContext posValue = ctx.posValue;
    if (posValue != null)
      return posValue.accept(this);

    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    String name = ctx.variablename().getText();
    Expression value = visitExpression_full(ctx.namedValue);
    return new VariableDeclaration(token, new Variable(token, name), value);
  }

  @Override
  public ASTCssNode visitMixinReferenceArgument_no_comma(MixinReferenceArgument_no_commaContext ctx) {
    Expression_space_separated_listContext posValue = ctx.posValue;
    if (posValue != null)
      return posValue.accept(this);

    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    String name = ctx.variablename().getText();
    Expression value = visitExpression_space_separated_list(ctx.namedValue);
    return new VariableDeclaration(token, new Variable(token, name), value);
  }

  @Override
  public DetachedRuleset visitDetachedRuleset(DetachedRulesetContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    General_bodyContext bodyCtx = ctx.general_body();
    GeneralBody body = visitGeneral_body(bodyCtx);
    return new DetachedRuleset(token, body);
  }

  @Override
  public DetachedRulesetReference visitDetachedRulesetReference(DetachedRulesetReferenceContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    TerminalNode nameCtx = ctx.AT_NAME();
    HiddenTokenAwareTree vToken = new HiddenTokenAwareTreeAdapter(nameCtx);

    DetachedRulesetReference result = new DetachedRulesetReference(token, new Variable(vToken, nameCtx.getText()));
    if (ctx.LPAREN() == null || ctx.RPAREN() == null) {
      problemsHandler.detachedRulesetCallWithoutParentheses(result);
    }

    return result;
  }

  /*
   * ***************************UNFINISHED HERE*******************************************
   */
  @Override
  public ASTCssNode visitExtendTargetSelectors(ExtendTargetSelectorsContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    //selector (ws COMMA ws selector)*;
    List<SelectorContext> selectors = ctx.selector();
    if (selectors.size() == 1) {
      return convertSelectorToExtend(token, visitSelector(selectors.get(0)));
    }

    MultiTargetExtend extend = new MultiTargetExtend(token);
    for (SelectorContext kid : selectors) {
      Selector selector = visitSelector(kid);
      extend.addExtend(convertSelectorToExtend(token, selector));
    }

    return extend;
  }

  private Extend convertSelectorToExtend(HiddenTokenAwareTree token, Selector selector) {
    if (selector.isExtending()) {
      problemsHandler.warnExtendInsideExtend(selector);
    }

    SelectorPart lastPart = selector.getLastPart();
    if (lastPart == null || !(lastPart instanceof SimpleSelector))
      return new Extend(token, selector);

    SimpleSelector possibleAll = (SimpleSelector) lastPart;
    if (possibleAll.hasSubsequent() || !possibleAll.hasElement())
      return new Extend(token, selector);

    if (!EXTEND_ALL_KEYWORD.equals(possibleAll.getElementName().getName()))
      return new Extend(token, selector);

    if (AstLogic.hasNonSpaceCombinator(possibleAll)) {
      possibleAll.setElementName(null);
    } else {
      selector.getParts().remove(possibleAll);
    }
    return new Extend(token, selector, true);
  }

  @Override
  public Extend visitExtendInDeclarationWithSemi(ExtendInDeclarationWithSemiContext ctx) {
    Pseudo extendAsPseudo = visitPseudo(ctx.pseudo());

    if (!(extendAsPseudo instanceof PseudoClass))
      throw new BugHappened(GRAMMAR_MISMATCH, extendAsPseudo);

    PseudoClass asPseudoclass = (PseudoClass) extendAsPseudo;
    ASTCssNode parameter = asPseudoclass.getParameter();
    if (parameter.getType()!=ASTCssNodeType.EXTEND && parameter.getType()!=ASTCssNodeType.MULTI_TARGET_EXTEND)
      throw new BugHappened(GRAMMAR_MISMATCH, extendAsPseudo);
    
    return (Extend)parameter;
  }

  /*
   * ***************************part 2*******************************************
   */
  @Override
  public GeneralBody visitTop_level_body_with_declaration(Top_level_body_with_declarationContext ctx) {
    TerminalNode lbraceToken = ctx.LBRACE();
    SyntaxOnlyElement lbrace = new SyntaxOnlyElement(new HiddenTokenAwareTreeAdapter(lbraceToken), lbraceToken.getText().trim());
    TerminalNode rbraceToken = ctx.RBRACE();
    SyntaxOnlyElement rbrace = new SyntaxOnlyElement(new HiddenTokenAwareTreeAdapter(rbraceToken), rbraceToken.getText().trim());

    List<ASTCssNode> members = new ArrayList<ASTCssNode>();
    List<Top_level_body_with_declaration_memberContext> childsCtxs = ctx.top_level_body_with_declaration_member();
    for (Top_level_body_with_declaration_memberContext childCtx : childsCtxs) {
      members.add(childCtx.accept(this));
    }
    GeneralBody result = new GeneralBody(new HiddenTokenAwareTreeAdapter(ctx), lbrace, rbrace, members);
    return result;
  }

  @Override
  public GeneralBody visitTop_level_body(Top_level_bodyContext ctx) {
    TerminalNode lbraceToken = ctx.LBRACE();
    SyntaxOnlyElement lbrace = new SyntaxOnlyElement(new HiddenTokenAwareTreeAdapter(lbraceToken), lbraceToken.getText().trim());
    TerminalNode rbraceToken = ctx.RBRACE();
    SyntaxOnlyElement rbrace = new SyntaxOnlyElement(new HiddenTokenAwareTreeAdapter(rbraceToken), rbraceToken.getText().trim());

    List<ASTCssNode> members = new ArrayList<ASTCssNode>();
    List<Top_level_elementContext> childsCtxs = ctx.top_level_element();
    for (Top_level_elementContext childCtx : childsCtxs) {
      members.add(childCtx.accept(this));
    }
    GeneralBody result = new GeneralBody(new HiddenTokenAwareTreeAdapter(ctx), lbrace, rbrace, members);
    return result;
  }

  @Override
  public Media visitMedia_top_level(Media_top_levelContext ctx) {
    MediaQueryContext firstQuery = ctx.firstQuery;
    Iterator<TerminalNode> comma = ctx.COMMA().iterator();
    List<MediaQueryContext> tail = ctx.tail;
    Top_level_body_with_declarationContext body = ctx.body;

    return doVisitMedia(ctx, firstQuery, comma, tail, body);
  }

  @Override
  public Media visitMedia_in_general_body(Media_in_general_bodyContext ctx) {
    MediaQueryContext firstQuery = ctx.firstQuery;
    Iterator<TerminalNode> comma = ctx.COMMA().iterator();
    List<MediaQueryContext> tail = ctx.tail;
    General_bodyContext body = ctx.body;

    return doVisitMedia(ctx, firstQuery, comma, tail, body);
  }

  private Media doVisitMedia(ParserRuleContext ctx, MediaQueryContext firstQuery, Iterator<TerminalNode> comma, List<MediaQueryContext> tail, ParserRuleContext body) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);

    Media result = new Media(token);
    result.addMediaQuery(visitMediaQuery(firstQuery));

    Iterator<MediaQueryContext> queriesCtx = tail != null ? tail.iterator() : new ArrayList<MediaQueryContext>().iterator();

    while (comma.hasNext() && queriesCtx.hasNext()) {
      result.addMediaQuery(visitMediaQuery(queriesCtx.next()));
      comma.next();
    }

    if (comma.hasNext() || queriesCtx.hasNext())
      throw new BugHappened(GRAMMAR_MISMATCH, token);

    result.setUnderlyingStructure(token);
    result.setBody((GeneralBody) body.accept(this));
    return result;
  }

  @Override
  public MediaQuery visitMediaQuery(MediaQueryContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);

    MediumContext mediumCtx = ctx.medium();
    Medium medium = mediumCtx != null ? visitMedium(mediumCtx) : null;

    List<MediaExpression> expressions = new ArrayList<MediaExpression>();
    List<MediaExpressionContext> mediaExpressionsCtx = ctx.mediaExpression();
    for (MediaExpressionContext context : mediaExpressionsCtx) {
      expressions.add(visitMediaExpression(context));
    }
    return new MediaQuery(token, medium, expressions);
  }

  @Override
  public Medium visitMedium(MediumContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    int childsCnt = ctx.getChildCount();
    if (childsCnt == 1) {
      ParseTree type = ctx.getChild(0);
      HiddenTokenAwareTree typeToken = new HiddenTokenAwareTreeAdapter(type);
      return new Medium(token, new MediumModifier(typeToken), new MediumType(typeToken, type.getText()));
    }

    ParseTree type = ctx.getChild(childsCnt - 1);
    HiddenTokenAwareTree typeToken = new HiddenTokenAwareTreeAdapter(type);
    return new Medium(token, toMediumModifier(ctx.getChild(0)), new MediumType(typeToken, type.getText()));
  }

  private MediumModifier toMediumModifier(ParseTree ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    String modifier = ctx.getText().toLowerCase();

    if ("not".equals(modifier))
      return new MediumModifier(token, MediumModifier.Modifier.NOT);

    if ("only".equals(modifier))
      return new MediumModifier(token, MediumModifier.Modifier.ONLY);

    throw new IllegalStateException("Unexpected medium modifier: " + modifier);
  }

  @Override
  public MediaExpression visitMediaExpression(MediaExpressionContext ctx) {
    CssMediaExpressionContext css = ctx.cssMediaExpression();
    if (css != null)
      return visitCssMediaExpression(css);

    InterpolatedMediaExpressionContext interpolated = ctx.interpolatedMediaExpression();
    if (interpolated != null)
      return visitInterpolatedMediaExpression(interpolated);

    throw new BugHappened(GRAMMAR_MISMATCH, new HiddenTokenAwareTreeAdapter(ctx));
  }

  @Override
  public MediaExpression visitInterpolatedMediaExpression(InterpolatedMediaExpressionContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);

    List<VariablereferenceContext> children = ctx.variablereference();
    List<Expression> expressions = new ArrayList<Expression>();
    for (VariablereferenceContext child : children) {
      Variable expression = visitVariablereference(child);
      expressions.add(expression);
    }

    ListExpression list = new ListExpression(token, expressions, new ListExpressionOperator(token, ListExpressionOperator.Operator.EMPTY_OPERATOR));
    return new InterpolatedMediaExpression(token, list);
  }

  @Override
  public MediaExpression visitCssMediaExpression(CssMediaExpressionContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);

    MediaFeatureContext mediaFeatureCtx = ctx.mediaFeature();
    Expression_fullContext expression_full = ctx.expression_full();

    MediaExpressionFeature mediaFeature = visitMediaFeature(mediaFeatureCtx);
    if (expression_full == null)
      return new FixedMediaExpression(token, mediaFeature, null);

    // FIXME (antlr4) (comments) colon 
    Expression expression = visitExpression_full(expression_full);
    return new FixedMediaExpression(token, mediaFeature, expression);
  }

  @Override
  public MediaExpressionFeature visitMediaFeature(MediaFeatureContext ctx) {
    HiddenTokenAwareTree featureNode = new HiddenTokenAwareTreeAdapter(ctx);
    return new MediaExpressionFeature(featureNode, ctx.getText());
  }

  @Override
  public ASTCssNode visitTop_level_body_with_declaration_member(Top_level_body_with_declaration_memberContext ctx) {
    return ctx.getChild(0).accept(this);
  }

  @Override
  public ASTCssNode visitImports(ImportsContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    Import result = new Import(token);
    switch (ctx.kind.getType()) {
    case LessG4Lexer.IMPORT_SYM:
      result.setMultiplicity(Import.ImportMultiplicity.IMPORT);
      break;
    case LessG4Lexer.IMPORT_ONCE_SYM:
      result.setMultiplicity(Import.ImportMultiplicity.IMPORT_ONCE);
      problemsHandler.deprecatedImportOnce(result);
      break;
    case LessG4Lexer.IMPORT_MULTIPLE_SYM:
      result.setMultiplicity(Import.ImportMultiplicity.IMPORT_MULTIPLE);
      problemsHandler.deprecatedImportMultiple(result);
      break;
    default:
      throw new BugHappened(GRAMMAR_MISMATCH, token);
    }

    ImportoptionsContext importoptions = ctx.importoptions();
    configureImportOptions(result, importoptions);

    result.setUrlExpression(visitTerm(ctx.url));
    List<MediaQueryContext> mediaQuery = ctx.mediaQuery();
    for (MediaQueryContext mediaQueryContext : mediaQuery) {
      result.add(visitMediaQuery(mediaQueryContext));
    }

    return result;
  }

  private void configureImportOptions(Import node, ImportoptionsContext importoptions) {
    if (importoptions == null)
      return;

    List<IdentContext> options = importoptions.option;
    for (IdentContext optionCtx : options) {
      String text = optionCtx.getText();
      if (IMPORT_OPTION_INLINE.equals(text)) {
        node.setInline(true);
      } else if (IMPORT_OPTION_ONCE.equals(text)) {
        node.setMultiplicity(Import.ImportMultiplicity.IMPORT_ONCE);
      } else if (IMPORT_OPTION_MULTIPLE.equals(text)) {
        node.setMultiplicity(Import.ImportMultiplicity.IMPORT_MULTIPLE);
      } else if (IMPORT_OPTION_LESS.equals(text)) {
        node.setContentKind(ImportContent.LESS);
      } else if (IMPORT_OPTION_CSS.equals(text)) {
        node.setContentKind(ImportContent.CSS);
      } else if (IMPORT_OPTION_REFERENCE.equals(text)) {
        node.setReferenceOnly(true);
      } else if (IMPORT_OPTION_OPTIONAL.equals(text)) {
        node.setOptional(true);
      } else {
        problemsHandler.unknownImportOption(node, text);
      }
    }

  }

  @Override
  public ASTCssNode visitImportoptions(ImportoptionsContext ctx) {
    throw new BugHappened(SHOULD_NOT_VISIT, new HiddenTokenAwareTreeAdapter(ctx));
  }

  @Override
  public ASTCssNode visitKeyframes(KeyframesContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);

    Keyframes result = new Keyframes(token, ctx.AT_KEYFRAMES().getText());
    List<KeyframesnameContext> namesCtx = ctx.names;
    for (KeyframesnameContext name : namesCtx) {
      result.addName(visitKeyframesname(name));
    }
    result.setBody(visitGeneral_body(ctx.general_body()));

    return result;
  }

  @Override
  public KeyframesName visitKeyframesname(KeyframesnameContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    Expression name = termBuilder.buildFromTerm(ctx.getChild(0));
    KeyframesName result = new KeyframesName(token, name);
    return result;
  }

  @Override
  public FontFace visitFontface(FontfaceContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    FontFace result = new FontFace(token);
    result.setBody(visitGeneral_body(ctx.body));
    return result;
  }

  @Override
  public Document visitDocument(DocumentContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);

    Document result = new Document(token, ctx.AT_DOCUMENT().getText());
    for (Term_only_functionContext fncCtx : ctx.urlFnc) {
      result.addUrlMatchFunction(visitTerm_only_function(fncCtx));
    }
    result.setBody(visitTop_level_body(ctx.body));
    return result;
  }

  @Override
  public Viewport visitViewport(ViewportContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);

    Viewport result = new Viewport(token, ctx.AT_VIEWPORT().getText());
    result.setBody(visitGeneral_body(ctx.body));
    return result;
  }

  @Override
  public Supports visitSupports(SupportsContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    Supports result = new Supports(token, ctx.AT_SUPPORTS().getText());
    result.setCondition(visitSupportsCondition(ctx.condition));
    result.setBody(visitGeneral_body(ctx.body));

    return result;
  }

  @Override
  public SupportsCondition visitSupportsCondition(SupportsConditionContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);

    SupportsCondition firstCondition = (SupportsCondition) ctx.first.accept(this);
    if (ctx.second == null || ctx.second.isEmpty())
      return firstCondition;

    SupportsLogicalCondition result = new SupportsLogicalCondition(token, firstCondition);
    Iterator<SimpleSupportsConditionContext> second = ctx.second.iterator();
    Iterator<Ident_except_notContext> operator = ctx.oper.iterator();

    while (operator.hasNext() && second.hasNext()) {
      Ident_except_notContext operCtx = operator.next();
      SupportsLogicalOperator logicalOperator = toSupportsLogicalOperator(operCtx);

      SimpleSupportsConditionContext condCtx = second.next();
      SupportsCondition condition = (SupportsCondition) condCtx.accept(this);

      result.addCondition(logicalOperator, condition);
    }
    if (operator.hasNext() || second.hasNext())
      throw new BugHappened(GRAMMAR_MISMATCH, new HiddenTokenAwareTreeAdapter(ctx));

    return result;
  }

  private SupportsLogicalOperator toSupportsLogicalOperator(Ident_except_notContext operCtx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(operCtx);
    String text = operCtx.getText();
    Map<String, SupportsLogicalOperator.Operator> operatorsBySymbol = SupportsLogicalOperator.Operator.getSymbolsMap();
    if (text == null || !operatorsBySymbol.containsKey(text.toLowerCase())) {
      SupportsLogicalOperator result = new SupportsLogicalOperator(token, null);
      problemsHandler.errWrongSupportsLogicalOperator(result, operCtx.getText());
      return result;
    }

    SupportsLogicalOperator result = new SupportsLogicalOperator(token, operatorsBySymbol.get(text.toLowerCase()));
    return result;
  }

  @Override
  public SupportsCondition visitSscDeclaration(SscDeclarationContext ctx) {
    return visitSupportsQuery(ctx.supportsQuery());
  }

  @Override
  public SupportsCondition visitSscNotCondition(SscNotConditionContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    //TODO: warning on wrong operator (anything that is not 'not')
    SyntaxOnlyElement negation = toSyntaxOnlyElement(ctx.IDENT_NOT());
    SupportsCondition condition = visitSupportsCondition(ctx.supportsCondition());

    SupportsConditionNegation result = new SupportsConditionNegation(token, negation, condition);
    return result;
  }

  @Override
  public SupportsCondition visitSscNestedCondition(SscNestedConditionContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    SyntaxOnlyElement openingParentheses = toSyntaxOnlyElement(ctx.LPAREN());
    SupportsCondition condition = visitSupportsCondition(ctx.supportsCondition());
    SyntaxOnlyElement closingParentheses = toSyntaxOnlyElement(ctx.RPAREN());
    //
    SupportsConditionInParentheses result = new SupportsConditionInParentheses(token, openingParentheses, condition, closingParentheses);
    return result;
  }

  @Override
  public SupportsCondition visitSupportsQuery(SupportsQueryContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);

    SyntaxOnlyElement openingParentheses = toSyntaxOnlyElement(ctx.LPAREN());
    Declaration declaration = visitDeclaration(ctx.declaration());
    SyntaxOnlyElement closingParentheses = toSyntaxOnlyElement(ctx.RPAREN());
    SupportsQuery result = new SupportsQuery(token, openingParentheses, closingParentheses, declaration);
    return result;
  }

  private SyntaxOnlyElement toSyntaxOnlyElement(ParseTree ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    return new SyntaxOnlyElement(token, ctx.getText());
  }

  @Override
  public ASTCssNode visitPage(PageContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    Page result = new Page(token);

    IdentContext nameCtx = ctx.ident();
    if (nameCtx != null)
      result.setName(new Name(new HiddenTokenAwareTreeAdapter(nameCtx), nameCtx.getText()));

    //mandatory_ws? COLON ws ident;
    PseudoPageContext pseudoPageCtx = ctx.pseudoPage();
    if (pseudoPageCtx != null) {
      result.setDockedPseudopage(ctx.ppDock == null);
      result.setPseudopage(visitPseudoPage(pseudoPageCtx));
    }

    result.setBody(visitGeneral_body(ctx.general_body()));
    return result;
  }

  @Override
  public Name visitPseudoPage(PseudoPageContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    return new Name(token, ":" + ctx.ident().getText());
  }

  @Override
  public ASTCssNode visitCharSet(CharSetContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);
    TerminalNode charset = ctx.STRING();
    return new CharsetDeclaration(token, termBuilder.createCssString(charset, charset.getText()));
  }

  @Override
  public ASTCssNode visitUnknownAtRule(UnknownAtRuleContext ctx) {
    //    unknownAtRule: AT_NAME (ws names+=unknownAtRuleNamesSet)? ws ( body+=general_body | semi+=SEMI );
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);

    UnknownAtRule result = new UnknownAtRule(token, ctx.AT_NAME().getText());
    UnknownAtRuleNamesSetContext names = ctx.unknownAtRuleNamesSet();
    if (names != null)
      result.addNames(visitUnknownAtRuleNamesSet(names).getExpressions());

    General_bodyContext general_body = ctx.general_body();
    if (general_body != null)
      result.setBody(visitGeneral_body(general_body));

    TerminalNode semi = ctx.SEMI();
    if (semi != null)
      result.setSemicolon(toSyntaxOnlyElement(semi));

    problemsHandler.warnUnknowAtRule(result);
    return result;
  }

  @Override
  public ListExpression visitUnknownAtRuleNamesSet(UnknownAtRuleNamesSetContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);

    ListExpression result = new ListExpression(token, new ArrayList<Expression>(), null);
    List<MathExprHighPriorContext> mathExprHighPrior = ctx.mathExprHighPrior();
    for (MathExprHighPriorContext childCtx : mathExprHighPrior) {
      result.addExpression(visitMathExprHighPrior(childCtx));
    }

    return result;
  }

  @Override
  public PageMarginBox visitPageMarginBox(PageMarginBoxContext ctx) {
    HiddenTokenAwareTree token = new HiddenTokenAwareTreeAdapter(ctx);

    PageMarginBox result = new PageMarginBox(token);
    TerminalNode nameCtx = ctx.AT_PAGE_MARGIN_BOX();
    result.setName(new Name(new HiddenTokenAwareTreeAdapter(nameCtx), nameCtx.getText()));
    result.setBody(visitGeneral_body(ctx.general_body()));

    return result;
  }

  @Override
  public ASTCssNode visitReusableStructureGuards(ReusableStructureGuardsContext ctx) {
    throw new BugHappened(SHOULD_NOT_VISIT, new HiddenTokenAwareTreeAdapter(ctx));
  }

  public List<Guard> doVisitReusableStructureGuards(ReusableStructureGuardsContext ctx) {
    List<Guard> result = new ArrayList<Guard>();
    List<GuardContext> guards = ctx.guard();
    if (guards != null) {
      for (GuardContext guard : guards) {
        result.add(visitGuard(guard));
      }
    }
    return result;
  }
}

class HiddenTokenAwareTreeAdapter extends HiddenTokenAwareTree {

  private Token start;
  private ParseTree ctx;

  public HiddenTokenAwareTreeAdapter(ParseTree ctx) {
    super(null);
    this.start = toToken(ctx);
    this.ctx = ctx;
  }

  public HiddenTokenAwareTreeAdapter(CommonToken token) {
    super(null);
    this.start = token;
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

  public ParseTree getUnderlyingNode() {
    return ctx;
  }

}