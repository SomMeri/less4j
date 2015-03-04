package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.BodyOwner;
import com.github.sommeri.less4j.core.ast.Directive;
import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.compiler.selectors.UselessLessElementsRemover;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class UnNestingAndBubbling {

  private final ASTManipulator manipulator = new ASTManipulator();
  private final UselessLessElementsRemover uselessLessElementsRemover = new UselessLessElementsRemover();

  public void unnestRulesetsAndDirectives(Body generalBody) {
    List<? extends ASTCssNode> childs = new ArrayList<ASTCssNode>(generalBody.getChilds());
    for (ASTCssNode kid : childs) {
      switch (kid.getType()) {
      case RULE_SET: {
        List<ASTCssNode> nestedRulesets = collectNestedRuleSets((RuleSet) kid);
        manipulator.addIntoBody(nestedRulesets, kid);
        uselessLessElementsRemover.removeFrom((RuleSet) kid);
        break;
      }
      default:
        if (AstLogic.isBubleableDirective(kid)) {
          Directive directive = (Directive) kid;
          unnestRulesetsAndDirectives(directive.getBody());
        } else {
          if (kid instanceof BodyOwner<?>) {
            BodyOwner<?> bodyOwner = (BodyOwner<?>) kid;
            if (bodyOwner.getBody() != null)
              unnestRulesetsAndDirectives(bodyOwner.getBody());
          }
        }
      }
    }

  }

  private List<ASTCssNode> collectNestedRuleSets(RuleSet topLevelNode) {
    NestedInRulesetStack nestedNodesStack = new NestedInRulesetStack(topLevelNode);
    collectChildRuleSets(topLevelNode, nestedNodesStack);

    return nestedNodesStack.getRulesets();
  }

  private void collectChildRuleSets(ASTCssNode ownerNode, NestedInRulesetStack nestedNodes) {
    List<? extends ASTCssNode> childs = new ArrayList<ASTCssNode>(ownerNode.getChilds());
    for (ASTCssNode kid : childs) {
      switch (kid.getType()) {
      case RULE_SET: {
        RuleSet nestedSet = (RuleSet) kid;
        manipulator.removeFromBody(nestedSet);
        nestedNodes.collect(nestedSet);
        nestedNodes.pushSelectors(nestedSet);

        collectChildRuleSets(kid, nestedNodes);

        nestedNodes.popSelectors();
        break;
      }
      default:
        if (AstLogic.isBubleableDirective(kid)) {
          List<Selector> outerSelectors = ArraysUtils.deeplyClonedList(nestedNodes.currentSelectors());
          Directive directive = (Directive) kid;
          manipulator.removeFromBody(directive);
          nestedNodes.collect(directive);

          putBodyIntoRuleset(directive, outerSelectors);
          unnestRulesetsAndDirectives(directive.getBody());

        } else {
          // unless explicitly set, do not pass take rulessets out of body
          // owners
          if (!(kid instanceof BodyOwner<?>)) {
            collectChildRuleSets(kid, nestedNodes);
          } else {
            BodyOwner<?> bodyOwner = (BodyOwner<?>) kid;
            if (bodyOwner.getBody() != null)
              unnestRulesetsAndDirectives(bodyOwner.getBody());
          }
        }

        break;
      }

    }
  }

  private void putBodyIntoRuleset(Directive bodyOwner, List<Selector> selectors) {
    RuleSet newRuleset = new RuleSet(bodyOwner.getUnderlyingStructure(), bodyOwner.getBody(), selectors);
    GeneralBody newBodyForOwner = new GeneralBody(bodyOwner.getUnderlyingStructure());
    newBodyForOwner.addMember(newRuleset);
    bodyOwner.setBody(newBodyForOwner);

    newBodyForOwner.configureParentToAllChilds();
    bodyOwner.configureParentToAllChilds();
    newRuleset.configureParentToAllChilds();
  }
}
