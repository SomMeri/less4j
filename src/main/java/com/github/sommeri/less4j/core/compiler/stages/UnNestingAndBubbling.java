package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.BodyOwner;
import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.Media;
import com.github.sommeri.less4j.core.ast.Page;
import com.github.sommeri.less4j.core.ast.PageMarginBox;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class UnNestingAndBubbling {

  private final ASTManipulator astManipulator = new ASTManipulator();
  private final ASTManipulator manipulator = new ASTManipulator();

  public void unnestRulesetAndMedia(Body generalBody) {
    List<? extends ASTCssNode> childs = new ArrayList<ASTCssNode>(generalBody.getChilds());
    for (ASTCssNode kid : childs) {
      switch (kid.getType()) {
      case RULE_SET: {
        List<ASTCssNode> nestedRulesets = collectNestedRuleSets((RuleSet) kid);
        astManipulator.addIntoBody(nestedRulesets, kid);
        break;
      }
      case MEDIA: {
        Media media = (Media) kid;
        unnestRulesetAndMedia(media.getBody());
        break;
      }
      case PAGE: {
        Page page = (Page) kid;
        unnestRulesetAndMedia(page.getBody());
        break;
      }
      case PAGE_MARGIN_BOX: {
        PageMarginBox marginBox = (PageMarginBox) kid;
        unnestRulesetAndMedia(marginBox.getBody());
        break;
      }
      default:
        //nothing is needed
        if (kid instanceof BodyOwner<?>) {
          BodyOwner<?> bodyOwner = (BodyOwner<?>) kid;
          if (bodyOwner.getBody() != null)
            unnestRulesetAndMedia(bodyOwner.getBody());
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
      }
        break;
      case MEDIA: {
        List<Selector> outerSelectors = ArraysUtils.deeplyClonedList(nestedNodes.currentSelectors());

        Media media = (Media) kid;
        manipulator.removeFromBody(media);
        nestedNodes.collect(media);

        putMediaBodyIntoRuleset(media, outerSelectors);

        unnestRulesetAndMedia(media.getBody());

      }
        break;

      default:
        //unless explicitly set, do not pass take rulessets out of body owners
        if (!(kid instanceof BodyOwner<?>)) {
          collectChildRuleSets(kid, nestedNodes);
        } else {
          BodyOwner<?> bodyOwner = (BodyOwner<?>) kid;
          if (bodyOwner.getBody() != null)
            unnestRulesetAndMedia(bodyOwner.getBody());
        }

        break;
      }

    }
  }

  private void putMediaBodyIntoRuleset(Media media, List<Selector> selectors) {
    RuleSet newRuleset = new RuleSet(media.getUnderlyingStructure(), media.getBody(), selectors);
    GeneralBody newMediaBody = new GeneralBody(media.getUnderlyingStructure());
    newMediaBody.addMember(newRuleset);
    media.setBody(newMediaBody);

    newMediaBody.configureParentToAllChilds();
    media.configureParentToAllChilds();
    newRuleset.configureParentToAllChilds();
  }
}
