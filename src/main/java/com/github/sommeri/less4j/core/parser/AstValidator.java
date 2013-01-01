package com.github.sommeri.less4j.core.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.EscapedSelector;
import com.github.sommeri.less4j.core.ast.KeyframesBody;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.NamespaceReference;
import com.github.sommeri.less4j.core.ast.PseudoClass;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.compiler.stages.ASTManipulator;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class AstValidator {

  private final ProblemsHandler problemsHandler;
  private final ASTManipulator manipulator = new ASTManipulator();

  public AstValidator(ProblemsHandler problemsHandler) {
    this.problemsHandler = problemsHandler;
  }

  public void validate(ASTCssNode node) {
    switch (node.getType()) {
    case RULE_SET: {
      checkEmptySelector((RuleSet) node);
      break;
    }
    case MIXIN_REFERENCE: {
      checkInterpolatedMixinName((MixinReference)node);
      break;
    }
    case NAMESPACE_REFERENCE: {
      checkInterpolatedNamespaceName((NamespaceReference)node);
      break;
    }
    case PSEUDO_CLASS: {
      checkDeprecatedParameterType((PseudoClass) node);
      break;
    }
    case ESCAPED_SELECTOR: {
      problemsHandler.deprecatedSyntaxEscapedSelector((EscapedSelector)node);
      break;
    }
    case KEYFRAMES_BODY: {
      checkForDisallowedMembers((KeyframesBody) node);
      break;
    }
    }

    List<ASTCssNode> childs = new ArrayList<ASTCssNode>(node.getChilds());
    for (ASTCssNode kid : childs) {
      validate(kid);
    }

  }

  private void checkForDisallowedMembers(KeyframesBody keyframes) {
    Set<ASTCssNodeType> supportedMembers = keyframes.getSupportedMembers();
    for (ASTCssNode member: keyframes.getBody()) {
      ASTCssNodeType type = member.getType();
      if (!supportedMembers.contains(type))
        problemsHandler.unsupportedKeyframesMember(member);
    }
  }

  private void checkInterpolatedMixinName(MixinReference reference) {
    if (reference.hasInterpolatedName()) {
      problemsHandler.interpolatedMixinReferenceSelector(reference);
      manipulator.removeFromClosestBody(reference);
    }
  }

  private void checkInterpolatedNamespaceName(NamespaceReference reference) {
    if (reference.hasInterpolatedName()) {
      problemsHandler.interpolatedNamespaceReferenceSelector(reference);
      manipulator.removeFromClosestBody(reference);
    }
  }

  private void checkDeprecatedParameterType(PseudoClass pseudo) {
    ASTCssNode parameter = pseudo.getParameter();
    if (parameter!=null && parameter.getType()==ASTCssNodeType.VARIABLE) {
      problemsHandler.variableAsPseudoclassParameter(pseudo);
    }
  }

  private void checkEmptySelector(RuleSet ruleSet) {
    if (ruleSet.getSelectors() == null || ruleSet.getSelectors().isEmpty())
      problemsHandler.rulesetWithoutSelector(ruleSet);
  }

}
