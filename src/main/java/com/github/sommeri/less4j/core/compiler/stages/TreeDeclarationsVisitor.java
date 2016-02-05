package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.Declaration;
import com.github.sommeri.less4j.core.ast.RuleSet;

public abstract class TreeDeclarationsVisitor {

  public void apply(ASTCssNode node) {
    switch (node.getType()) {
    case RULE_SET:
      RuleSet ruleset = (RuleSet) node;
      rulesetsBodyPropertiesMerger(ruleset.getBody());
      break;

    case CHARSET_DECLARATION:
    case IMPORT:
      break;

    default:
      applyToKidsProperties(node);
    }

  }

  private void applyToKidsProperties(ASTCssNode node) {
    List<ASTCssNode> childs = new ArrayList<ASTCssNode>(node.getChilds());
    for (ASTCssNode kid : childs) {
      apply(kid);
    }
  }
  
  private void rulesetsBodyPropertiesMerger(Body node) {
    enteringBody(node);

    List<? extends ASTCssNode> childs = node.getChilds();
    for (ASTCssNode kid : childs) {
      switch (kid.getType()) {
      case DECLARATION:
        applyToDeclaration((Declaration) kid);
        
        break;

      default:
        applyToKidsProperties(kid);
        break;
      }
    }

    leavingBody(node);
  }
  
  protected abstract void applyToDeclaration(Declaration declaration);

  protected abstract void enteringBody(Body node);

  protected abstract void leavingBody(Body node);

}
