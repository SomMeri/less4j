package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.VariableDeclaration;
import com.github.sommeri.less4j.core.compiler.scopes.IScope;
import com.github.sommeri.less4j.core.compiler.scopes.refactoring.ScopeFactory;

/**
 * Splits the input tree into separate scope tree and the trees. Removes
 * variables, pure mixin and pure namespace declarations from the original
 * abstract syntax tree.
 * 
 */
//Variables, mixins and namespaces are valid within the whole scope, even before they have been defined. 
public class InitialScopeExtractor {
  private ASTManipulator manipulator = new ASTManipulator();

  private IScope currentScope;
  //FIXME: (!!!)remove
  public static int counter=0;

  public InitialScopeExtractor() {
  }

  public IScope extractScope(ASTCssNode node) {
    currentScope = null;

    IScope result = buildScope(node);
    return result;
  }

  private IScope buildScope(ASTCssNode node) {
    boolean hasOwnScope = AstLogic.hasOwnScope(node);
    if (hasOwnScope)
      increaseScope(node);

    fillScopeNames(node);

    List<? extends ASTCssNode> childs = new ArrayList<ASTCssNode>(node.getChilds());
    for (ASTCssNode kid : childs) {
      buildScope(kid);

      if (kid.getType() == ASTCssNodeType.VARIABLE_DECLARATION) {
        currentScope.registerVariable((VariableDeclaration) kid);
        manipulator.removeFromBody(kid);
      } else if (kid.getType() == ASTCssNodeType.REUSABLE_STRUCTURE) {
        ReusableStructure mixin = (ReusableStructure) kid;
        IScope bodyScope = currentScope.childByOwners(mixin, mixin.getBody());
        currentScope.registerMixin(mixin, bodyScope);
        bodyScope.removedFromAst();
        if (bodyScope.hasParent())
          bodyScope.getParent().removedFromAst(); // remove also arguments scope from tree
        manipulator.removeFromBody(kid);
      } else if (kid.getType() == ASTCssNodeType.RULE_SET) {
        RuleSet ruleSet = (RuleSet) kid;
        if (ruleSet.isUsableAsReusableStructure()) {
          IScope bodyScope = currentScope.childByOwners(ruleSet, ruleSet.getBody());
          currentScope.registerMixin(ruleSet.convertToReusableStructure(), bodyScope);
        }
      } else if (kid.getType() == ASTCssNodeType.MIXIN_REFERENCE) {
        currentScope.createPlaceholder();
      }
    }

    IScope result = currentScope;
    if (hasOwnScope)
      decreaseScope();

    return result;
  }

  private void fillScopeNames(ASTCssNode node) {
    switch (node.getType()) {
    case REUSABLE_STRUCTURE:
      currentScope.addNames(((ReusableStructure) node).getNamesAsStrings());
      break;

    case RULE_SET: {
      RuleSet ruleSet = (RuleSet) node;
      if (ruleSet.isUsableAsReusableStructure()) {
        currentScope.addNames(ruleSet.extractReusableStructureNames());
      }
      break;
    }

    default:
    }
  }

  private void decreaseScope() {
    currentScope = currentScope.getParent();
  }

  private void increaseScope(ASTCssNode owner) {
    counter++;
    if (currentScope == null) {
      currentScope = ScopeFactory.createDefaultScope(owner);
    } else if(AstLogic.isBodyOwner(owner)) {
      currentScope = ScopeFactory.createBodyOwnerScope(owner, currentScope);
    } else {
      currentScope = ScopeFactory.createScope(owner, currentScope);
    }

    return;

  }

}
