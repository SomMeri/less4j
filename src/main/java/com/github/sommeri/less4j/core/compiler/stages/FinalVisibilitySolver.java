package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNode.Visibility;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.BodyOwner;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.Selector;

public class FinalVisibilitySolver {

  /*
   * A node can be visible either because it is visible in its own right or something inside it 
   * wants be visible.
   */
  private final ASTManipulator manipulator = new ASTManipulator();

  public void resolveVisibility(ASTCssNode node) {
    if (node.getVisibility() == Visibility.DEFAULT) {
      return;
    }

    if (node.getType() == ASTCssNodeType.RULE_SET) {
      handleRuleset((RuleSet) node);
    } else if (node instanceof Body) {
      handleBodyKids((Body) node);
    } else if (node instanceof BodyOwner) {
      Body body = ((BodyOwner<?>) node).getBody();
      handleBodyKids(body);
    }
  }

  private void handleRuleset(RuleSet node) {
    List<Selector> removeSelectors = null;
    for (Selector selector : node.getSelectors()) {
      switch (selector.getVisibility()) {
      case DEFAULT:
        if (removeSelectors == null) {
          removeSelectors = new ArrayList<Selector>();
        }
        removeSelectors.add(selector);
        break;
      case VISIBLE:
        break;
      }
    }
    if (removeSelectors != null) {
      node.getSelectors().removeAll(removeSelectors);
    }

    handleBodyKids(node.getBody());
  }

  private void handleBodyKids(Body body) {
    if (body == null)
      return ;
    
    List<ASTCssNode> removeKids = null;
    for (ASTCssNode kid : body.getMembers()) {
      switch (kid.getVisibility()) {
      case DEFAULT:
        if (removeKids == null) {
          removeKids = new ArrayList<ASTCssNode>();
        }
        removeKids.add(kid);
        break;
      case VISIBLE:
        resolveVisibility(kid);
        break;
      }
    }
    if (removeKids != null) {
      manipulator.removeFromBody(body, removeKids);
    }
  }

}
