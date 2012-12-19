package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.RuleSet;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
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
  private List<String> nextChildScopeNames;

  public ScopeExtractor() {
  }

  public Scope extractScope(ASTCssNode node) {
    currentScope = null;
    nextChildScopeNames = null;
    
    Scope result = buildScope(node);
    return result;
  }

  private Scope buildScope(ASTCssNode node) {
    boolean hasOwnScope = AstLogic.hasOwnScope(node);
    if (hasOwnScope)
      increaseScope(node);

    List<String> representedNamespaces = representedNamedScope(node);
    if (representedNamespaces != null) {
      nextChildScopeNames = representedNamespaces;
    }

    List<? extends ASTCssNode> childs = new ArrayList<ASTCssNode>(node.getChilds());
    for (ASTCssNode kid : childs) {
      buildScope(kid);
      
      if (kid.getType() == ASTCssNodeType.VARIABLE_DECLARATION) {
        currentScope.registerVariable((VariableDeclaration) kid);
        manipulator.removeFromBody(kid);
      } else if (kid.getType() == ASTCssNodeType.REUSABLE_STRUCTURE) {
        ReusableStructure mixin = (ReusableStructure) kid;
        Scope bodyScope = currentScope.getChildOwnerOf(mixin.getBody());
        currentScope.registerMixin(mixin, bodyScope);
        
        bodyScope.removedFromTree();
        manipulator.removeFromBody(kid);
      } else if (kid.getType() == ASTCssNodeType.RULE_SET) {
        RuleSet ruleSet = (RuleSet) kid;
        if (ruleSet.usableAsReusableStructure()) { 
          Scope bodyScope = currentScope.getChildOwnerOf(ruleSet.getBody());
          currentScope.registerMixin(ruleSet.convertToReusableStructure(), bodyScope);
        }
      } 
    }

    Scope result = currentScope;
    if (hasOwnScope)
      decreaseScope();

    return result;
  }

  private List<String> representedNamedScope(ASTCssNode node) {
    switch (node.getType()) {
    case REUSABLE_STRUCTURE:
      return ((ReusableStructure) node).getNames();

    case RULE_SET: {
      RuleSet ruleSet = (RuleSet) node;
      if (ruleSet.usableAsReusableStructure())
        return ruleSet.extractReusableStructureNames();

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
    if (nextChildScopeNames == null) {
      if (currentScope==null)
        currentScope = Scope.createDefaultScope(owner);
      else 
        currentScope = Scope.createUnnamedScope(owner, currentScope);
      
      return;
    }

    currentScope = Scope.createScope(owner, nextChildScopeNames, currentScope);
    nextChildScopeNames = null;
  }

}
