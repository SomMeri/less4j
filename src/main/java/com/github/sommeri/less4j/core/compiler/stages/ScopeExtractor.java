package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.PureMixin;
import com.github.sommeri.less4j.core.ast.PureNamespace;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.VariableDeclaration;
import com.github.sommeri.less4j.core.compiler.scopes.Scope;

/**
 * Splits the input tree into separate scope tree and the trees. Removes variables, pure mixin and 
 * pure namespace declarations from the original abstract syntax tree. 
 *
 */
//Variables, mixins and namespaces are valid within the whole scope, even before they have been defined. 
public class ScopeExtractor {
  private ASTManipulator manipulator = new ASTManipulator();

  private Scope currentScope;
  private String nextChildScopeName = null;

  public ScopeExtractor() {
    //FIXME: this is one use only class,fix it once it works
  }

  public Scope extractScope(ASTCssNode node) {
    Scope result = buildScope(node);
    return result;
  }

  private Scope buildScope(ASTCssNode node) {
    boolean hasOwnScope = AstLogic.hasOwnScope(node);
    if (hasOwnScope)
      increaseScope(node);

    String representedNamespace = representedNamedScope(node);
    if (representedNamespace != null) {
      nextChildScopeName = representedNamespace;
    }

    List<? extends ASTCssNode> childs = new ArrayList<ASTCssNode>(node.getChilds());
    for (ASTCssNode kid : childs) {
      buildScope(kid);
      
      if (kid.getType() == ASTCssNodeType.VARIABLE_DECLARATION) {
        currentScope.registerVariable((VariableDeclaration) kid);
        manipulator.removeFromBody(kid);
      } else if (kid.getType() == ASTCssNodeType.PURE_MIXIN) {
        PureMixin mixin = (PureMixin) kid;
        Scope bodyScope = currentScope.getChildOwnerOf(mixin.getBody());
        currentScope.registerMixin(mixin, bodyScope);
        
        bodyScope.removedFromTree();
        manipulator.removeFromBody(kid);
      } else if (kid.getType() == ASTCssNodeType.RULE_SET) {
        RuleSet ruleSet = (RuleSet) kid;
        if (ruleSet.isMixin()) { 
          Scope bodyScope = currentScope.getChildOwnerOf(ruleSet.getBody());
          currentScope.registerMixin(ruleSet.convertToMixin(), bodyScope);
        }
      } else if (kid.getType() == ASTCssNodeType.PURE_NAMESPACE) {
        PureNamespace namespace = (PureNamespace) kid;
        Scope bodyScope = currentScope.getChildOwnerOf(namespace.getBody());
        bodyScope.removedFromTree();
        manipulator.removeFromBody(kid);
      }
    }

    Scope result = currentScope;
    if (hasOwnScope)
      decreaseScope();

    return result;
  }

  private String representedNamedScope(ASTCssNode node) {
    switch (node.getType()) {
    case PURE_NAMESPACE:
      return ((PureNamespace) node).getName();

    case RULE_SET: {
      RuleSet ruleSet = (RuleSet) node;
      if (ruleSet.isNamespace())
        return ruleSet.extractNamespaceName();

      return null;
    }

    default:
      return null;
    }
  }

  private void decreaseScope() {
    currentScope = currentScope.getParent();
  }

  private void increaseScope(ASTCssNode owner) {
    if (nextChildScopeName == null) {
      if (currentScope==null)
        currentScope = Scope.createDefaultScope(owner);
      else 
        currentScope = Scope.createUnnamedScope(owner, currentScope);
      
      return;
    }

    currentScope = Scope.createScope(owner, nextChildScopeName, currentScope);
    nextChildScopeName = null;
  }

}
