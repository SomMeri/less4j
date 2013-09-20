package com.github.sommeri.less4j.core.validators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.EscapedSelector;
import com.github.sommeri.less4j.core.ast.MediaQuery;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.NestedSelectorAppender;
import com.github.sommeri.less4j.core.ast.PseudoClass;
import com.github.sommeri.less4j.core.ast.ReusableStructureName;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.ast.SupportsLogicalCondition;
import com.github.sommeri.less4j.core.ast.SupportsLogicalOperator;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.compiler.stages.ASTManipulator;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class LessAstValidator {

  private final ProblemsHandler problemsHandler;
  private final ASTManipulator manipulator = new ASTManipulator();
  private final SupportedLessBodyMembers allowedBodyMembers = new SupportedLessBodyMembers();

  public LessAstValidator(ProblemsHandler problemsHandler) {
    this.problemsHandler = problemsHandler;
  }

  public void validate(ASTCssNode node) {
    switch (node.getType()) {
    case RULE_SET: {
      checkEmptySelector((RuleSet) node);
      checkTopLevelNested((RuleSet) node);
      break;
    }
    case MIXIN_REFERENCE: {
      checkInterpolatedNamespaceName((MixinReference) node);
      checkExtendedNamespaceName((MixinReference) node);
      checkInterpolatedMixinName((MixinReference) node);
      break;
    }
    case PSEUDO_CLASS: {
      checkDeprecatedParameterType((PseudoClass) node);
      break;
    }
    case ESCAPED_SELECTOR: {
      problemsHandler.deprecatedSyntaxEscapedSelector((EscapedSelector) node);
      break;
    }
    case GENERAL_BODY: {
      checkForDisallowedMembers((Body) node);
      break;
    }
    case MEDIA_QUERY: {
      checkForPartialInterpolatedMediaQuery((MediaQuery) node);
      break;
    }
    case SUPPORTS_CONDITION_LOGICAL: {
      checkForLogicalConditionConsistency((SupportsLogicalCondition) node);
      break;
    }
    default:
      //nothing is needed
    }

    List<ASTCssNode> childs = new ArrayList<ASTCssNode>(node.getChilds());
    for (ASTCssNode kid : childs) {
      validate(kid);
    }

  }

  private void checkForLogicalConditionConsistency(SupportsLogicalCondition condition) {
    Iterator<SupportsLogicalOperator> logicalOperators = condition.getLogicalOperators().iterator();
    if (!logicalOperators.hasNext())
      return;

    SupportsLogicalOperator masterOperator = logicalOperators.next();
    while (logicalOperators.hasNext()) {
      SupportsLogicalOperator operator = logicalOperators.next();
      if (!masterOperator.getOperator().equals(operator.getOperator()))
        problemsHandler.warnInconsistentSupportsLogicalConditionOperators(operator, masterOperator);
    }
  }

  private void checkForPartialInterpolatedMediaQuery(MediaQuery node) {
    if (!node.hasInterpolatedExpression())
      return;

    if (node.getMedium() != null || node.getExpressions().size() > 1)
      problemsHandler.lessjsDoesNotSupportPartiallyInterpolatedMediaQueries(node);

  }

  private void checkTopLevelNested(RuleSet node) {
    if (node.getParent().getType() != ASTCssNodeType.STYLE_SHEET)
      return;

    for (Selector selector : node.getSelectors()) {
      NestedSelectorAppender appender = selector.findFirstAppender();
      if (appender != null) {
        problemsHandler.nestedAppenderOnTopLevel(appender);
        manipulator.removeFromClosestBody(node);
        return;
      }
    }

  }

  private void checkForDisallowedMembers(Body body) {
    Set<ASTCssNodeType> supportedMembers = allowedBodyMembers.getSupportedMembers(body);
    for (ASTCssNode member : body.getMembers()) {
      ASTCssNodeType type = member.getType();
      if (!supportedMembers.contains(type))
        problemsHandler.wrongMemberInLessBody(member, body);
    }
  }

  private void checkInterpolatedMixinName(MixinReference reference) {
    if (reference.hasInterpolatedFinalName()) {
      problemsHandler.interpolatedMixinReferenceSelector(reference);
      manipulator.removeFromClosestBody(reference);
    }
  }

  private void checkInterpolatedNamespaceName(MixinReference reference) {
    if (reference.hasInterpolatedNameChain()) {
      problemsHandler.interpolatedNamespaceReferenceSelector(reference);
      manipulator.removeFromClosestBody(reference);
    }
  }

  private void checkExtendedNamespaceName(MixinReference reference) {
    for (ReusableStructureName name : reference.getNameChain()) {
      if (name.hasMultipleParts()) {
        problemsHandler.extendedNamespaceReferenceSelector(reference);
        manipulator.removeFromClosestBody(reference);
        return;
      }
    }
  }

  private void checkDeprecatedParameterType(PseudoClass pseudo) {
    ASTCssNode parameter = pseudo.getParameter();
    if (parameter == null || parameter.getType() != ASTCssNodeType.VARIABLE)
      return;
    
    Variable variable = (Variable) parameter;
    if (!variable.hasInterpolatedForm())
      problemsHandler.variableAsPseudoclassParameter(pseudo);
  }

  private void checkEmptySelector(RuleSet ruleSet) {
    if (ruleSet.getSelectors() == null || ruleSet.getSelectors().isEmpty())
      problemsHandler.rulesetWithoutSelector(ruleSet);
  }

}
