package com.github.sommeri.less4j.core.compiler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ArgumentDeclaration;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.Declaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.IndirectVariable;
import com.github.sommeri.less4j.core.ast.Media;
import com.github.sommeri.less4j.core.ast.MediaExpression;
import com.github.sommeri.less4j.core.ast.MediaExpressionFeature;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.NestedRuleSet;
import com.github.sommeri.less4j.core.ast.PureMixin;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.RuleSetsBody;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.ast.VariableDeclaration;

public class LessToCssCompiler {

  private ASTManipulator manipulator = new ASTManipulator();
  private ActiveScope activeScope;
  private ExpressionEvaluator expressionEvaluator;
  private NestedRulesCollector nestedRulesCollector;

  public ASTCssNode compileToCss(StyleSheet less) {
    activeScope = new ActiveScope();
    expressionEvaluator = new ExpressionEvaluator(activeScope);
    nestedRulesCollector = new NestedRulesCollector();

    solveVariablesAndMixins(less);
    evaluateExpressions(less);
    freeNestedRuleSets(less);

    return less;
  }

  private void freeNestedRuleSets(Body<ASTCssNode> body) {
    List<? extends ASTCssNode> childs = new ArrayList<ASTCssNode>(body.getChilds());
    for (ASTCssNode kid : childs) {
      if (kid.getType() == ASTCssNodeType.RULE_SET) {
        List<NestedRuleSet> nestedRulesets = nestedRulesCollector.collectNestedRuleSets((RuleSet) kid);
        body.addMembersAfter(convertToRulesSets(nestedRulesets), kid);
        for (RuleSet ruleSet : nestedRulesets) {
          ruleSet.setParent(body);
        }
      }
      if (kid.getType() == ASTCssNodeType.MEDIA) {
        freeNestedRuleSets((Media) kid);
      }
    }
  }

  private List<RuleSet> convertToRulesSets(List<NestedRuleSet> nestedRulesets) {
    List<RuleSet> result = new ArrayList<RuleSet>();
    for (NestedRuleSet nested : nestedRulesets) {
      result.add(nested.convertToRuleSet());
    }
    return result;
  }

  private void evaluateExpressions(ASTCssNode node) {
    if (node instanceof Expression) {
      Expression value = expressionEvaluator.evaluate((Expression) node);
      manipulator.replace(node, value);
    } else {
      List<? extends ASTCssNode> childs = node.getChilds();
      for (ASTCssNode kid : childs) {
        switch (kid.getType()) {
        case MEDIA_EXPRESSION:
          evaluateInMediaExpressions((MediaExpression) kid);
          break;

        case DECLARATION:
          evaluateInDeclaration((Declaration) kid);
          break;

        default:
          evaluateExpressions(kid);
          break;
        }

      }
    }
  }

  private void evaluateInDeclaration(Declaration node) {
    if (!node.isFontDeclaration()) {
      evaluateExpressions(node);
      return;
    }
  }

  private void evaluateInMediaExpressions(MediaExpression node) {
    MediaExpressionFeature feature = node.getFeature();
    if (!feature.isRatioFeature()) {
      evaluateExpressions(node);
      return;
    }
  }

  private void solveVariablesAndMixins(ASTCssNode node) {
    boolean hasOwnScope = hasOwnScope(node);
    if (hasOwnScope)
      activeScope.increaseScope();

    switch (node.getType()) {
    case VARIABLE_DECLARATION: {
      manipulator.removeFromBody(node);
      break;
    }
    case VARIABLE: {
      Expression replacement = expressionEvaluator.evaluate((Variable) node);
      manipulator.replace(node, replacement);
      break;
    }
    case INDIRECT_VARIABLE: {
      Expression replacement = expressionEvaluator.evaluate((IndirectVariable) node);
      manipulator.replace(node, replacement);
      break;
    }
    case MIXIN_REFERENCE: {
      MixinReference mixinReference = (MixinReference) node;
      RuleSetsBody replacement = resolveMixinReference(mixinReference);

      List<ASTCssNode> childs = replacement.getChilds();
      if (!childs.isEmpty()) {
        childs.get(0).addOpeningComments(mixinReference.getOpeningComments());
        childs.get(childs.size() - 1).addTrailingComments(mixinReference.getTrailingComments());
      }
      manipulator.replaceInBody(mixinReference, childs);
      break;
    }
    case PURE_MIXIN: {
      activeScope.enteringPureMixin((PureMixin) node);
      expressionEvaluator.turnOffEvaluation();
      break;
    }
    }

    if (node.getType() != ASTCssNodeType.PURE_MIXIN && node.getType() != ASTCssNodeType.VARIABLE_DECLARATION && node.getType() != ASTCssNodeType.ARGUMENT_DECLARATION) {
      List<? extends ASTCssNode> childs = new ArrayList<ASTCssNode>(node.getChilds());
      //FIXME: make extensive test case on nested mixins and nested rulesets - pure mixins and simple classes behave diferently
      //Register all variables and  mixins. We have to do that because every variable and every mixin is valid within 
      //the whole scope, even before it was defined. 
      registerAllVariables(childs);
      registerAllMixins(childs);

      for (ASTCssNode kid : childs) {
        solveVariablesAndMixins(kid);
      }
    }

    switch (node.getType()) {
    case PURE_MIXIN: {
      activeScope.leavingPureMixin((PureMixin) node);
      if (!activeScope.isInPureMixin())
        expressionEvaluator.turnOnEvaluation();
      manipulator.removeFromBody(node);
      break;
    }
    }
    if (hasOwnScope)
      activeScope.decreaseScope();
  }

  public void registerAllVariables(List<? extends ASTCssNode> childs) {
    for (ASTCssNode kid : childs) {
      if (kid.getType() == ASTCssNodeType.VARIABLE_DECLARATION) {
        activeScope.addDeclaration((VariableDeclaration) kid); //no reason to go further
      }
    }
  }

  public void registerAllMixins(List<? extends ASTCssNode> childs) {
    for (ASTCssNode kid : childs) {
      if (kid.getType() == ASTCssNodeType.PURE_MIXIN) {
        activeScope.registerMixin((PureMixin) kid);
      }
      if (kid.getType() == ASTCssNodeType.RULE_SET) {
        RuleSet ruleSet = (RuleSet) kid;
        if (ruleSet.isMixin()) {
          activeScope.registerMixin(ruleSet.convertToMixin());
        }
      }
      if (kid.getType() == ASTCssNodeType.NESTED_RULESET) {
        NestedRuleSet nested = (NestedRuleSet) kid;
        if (nested.isMixin()) {
          activeScope.registerMixin(nested.convertToMixin());
        }
      }
    }
  }

  private boolean hasOwnScope(ASTCssNode node) {
    return (node instanceof Body);
  }

  private RuleSetsBody resolveMixinReference(MixinReference reference) {
    List<MixinWithScope> matchingMixins = activeScope.getAllMatchingMixins(reference);
    RuleSetsBody result = new RuleSetsBody(reference.getUnderlyingStructure());
    for (MixinWithScope mixin : matchingMixins) {
      boolean evaluatorOn = expressionEvaluator.turnOnEvaluation();
      
      initializeMixinVariableScope(reference, mixin);
      
      RuleSetsBody body = solveVariablesAndMixinsInMixin(mixin.getMixin());
      result.addMembers(body.getChilds());

      activeScope.leaveMixinVariableScope();
      if (!evaluatorOn)
        expressionEvaluator.turnOffEvaluation();
    }

    if (reference.isImportant()) {
      declarationsAreImportant(result);
    }

    return result;
  }

  public void declarationsAreImportant(RuleSetsBody result) {
    for (ASTCssNode kid : result.getChilds()) {
      if (kid instanceof Declaration) {
        Declaration declaration = (Declaration) kid;
        declaration.setImportant(true);
      }
    }
  }

  private RuleSetsBody solveVariablesAndMixinsInMixin(PureMixin mixin) {
    if (!expressionEvaluator.evaluate(mixin.getGuards())) {
      List<ASTCssNode> emptyList = Collections.emptyList();
      return new RuleSetsBody(mixin.getUnderlyingStructure(), emptyList);
    }

    RuleSetsBody body = mixin.getBody().clone();
    solveVariablesAndMixins(body);
    return body;
  }

  private void initializeMixinVariableScope(MixinReference reference, MixinWithScope mixin) {
    Map<String, Expression> variableState = mixin.getVariablesUponDefinition();
    
   // this is the root of the problem
    
    int length = mixin.getMixin().getParameters().size();
    for (int i = 0; i < length; i++) {
      ASTCssNode parameter = mixin.getMixin().getParameters().get(i);
      if (parameter.getType() == ASTCssNodeType.ARGUMENT_DECLARATION) {
        ArgumentDeclaration declaration = (ArgumentDeclaration) parameter;
        if (reference.hasParameter(i)) {
          Expression value = expressionEvaluator.evaluate(reference.getParameter(i));
          activeScope.addDeclaration(variableState, declaration, value);
        } else {
          if (declaration.getValue() == null)
            CompileException.throwUndefinedMixinParameterValue(mixin.getMixin(), declaration, reference);

          activeScope.addDeclaration(variableState, declaration);
        }
      }
    }
    
    activeScope.enterMixinVariableScope(variableState);
  }

}
