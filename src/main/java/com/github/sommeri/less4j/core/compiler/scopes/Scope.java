package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.AbstractVariableDeclaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.NamespaceReference;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.compiler.problems.ProblemsHandler;

public class Scope {
  
  private final ASTCssNode owner;
  private boolean presentInTree = true;
  private VariablesScope variables = new VariablesScope();
  private MixinsScope mixins = new MixinsScope(); 

  private Scope parent;
  private List<Scope> childs = new ArrayList<Scope>();
  private String name;

  protected Scope(ASTCssNode owner, String name, Scope parent) {
    super();
    this.name = name;
    this.owner = owner;
    setParent(parent);
  }

  protected Scope(ASTCssNode owner, String name) {
    this(owner, name, null);
  }

  private void addChild(Scope child) {
    childs.add(child);
  }

  public Scope getParent() {
    return parent;
  }

  public List<Scope> getChilds() {
    return childs;
  }

  public String getName() {
    return name;
  }

  public boolean hasParent() {
    return getParent()!=null;
  }

  @Override
  public String toString() {
    if (hasParent())
      return getParent() + " > " + getName();
    return getName();
  }

  public void registerVariable(AbstractVariableDeclaration declaration) {
    variables.addDeclaration(declaration);
  }

  public void registerVariable(AbstractVariableDeclaration node, Expression replacementValue) {
    variables.addDeclaration(node, replacementValue);
  }

  public void registerVariableIfNotPresent(String name, Expression replacementValue) {
    variables.addDeclarationIfNotPresent(name, replacementValue);
  }

  public Expression getValue(Variable variable) {
    return getValue(variable.getName());
  }

  public Expression getValue(String name) {
    Expression value = variables.getValue(name);
    if (value==null && hasParent()) 
      return getParent().getValue(name);
    
    return value;
  }
  
  public void registerMixin(ReusableStructure mixin, Scope mixinsBodyScope) {
    mixins.registerMixin(new FullMixinDefinition(mixin, mixinsBodyScope));
  }

  public List<FullMixinDefinition> getNearestMixins(MixinReference reference) {
    return getNearestMixins(reference.getName());
  }

  public List<FullMixinDefinition> getNearestMixins(String name) {
    List<FullMixinDefinition> value = mixins.getMixins(name);
    if ((value==null || value.isEmpty()) && hasParent()) 
      return getParent().getNearestMixins(name);
    
    return value==null? new ArrayList<FullMixinDefinition>() : value;
  }

  public List<FullMixinDefinition> getNearestMixins(NamespaceReference reference, ProblemsHandler problemsHandler) {
    List<Scope> namespaces = getNearestNamespaces(reference);
    if (namespaces.isEmpty())
      problemsHandler.undefinedNamespace(reference);
    
    List<FullMixinDefinition> result = new ArrayList<FullMixinDefinition>();
    for (Scope scope : namespaces) {
      result.addAll(scope.getNearestMixins(reference.getFinalReference()));
    }
    return result;
  }

  private List<Scope> getNearestNamespaces(NamespaceReference reference) {
    List<String> nameChain = reference.getNameChain();
    Scope space = this;
    List<Scope> result = findMatchingChilds(nameChain);
    while (result.isEmpty() && space.hasParent()) {
      space = space.getParent();
      result = space.findMatchingChilds(nameChain);
    }
    return result;
  }

  public List<Scope> findMatchingChilds(List<String> nameChain) {
    if (nameChain.isEmpty())
      return Arrays.asList(this);
    
    String fistName = nameChain.get(0);
    List<String> theRest = nameChain.subList(1, nameChain.size()); 
    List<Scope> result = new ArrayList<Scope>();
    for (Scope kid : getChilds()) {
      if (kid.getName().equals(fistName)) {
        result.addAll(kid.findMatchingChilds(theRest));
      }
    }
    return result;
  }

  public static Scope createDefaultScope(ASTCssNode owner) {
    return new Scope(owner, "#default#");
  }

  public static Scope createUnnamedScope(ASTCssNode owner, Scope parent) {
    return new Scope(owner, "#unnamed#", parent);
  }

  public static Scope createScope(ASTCssNode owner, String name, Scope parent) {
    return new Scope(owner, name, parent);
  }

  public String toLongString() {
    return toLongString(0).toString();
  }

  private StringBuilder toLongString(int level) {
    String prefix = "";
    for (int i=0;i<level;i++) {
      prefix +="  ";
    }
    StringBuilder text = new StringBuilder(prefix);
    text.append(getName()).append("(").append(variables.size()).append(", ").append(mixins.size());
    text.append(") {").append("\n");
    
    for (Scope kid : getChilds()) {
      text.append(kid.toLongString(level+1));
    }
    text.append(prefix).append("}").append("\n");;
    return text;
  }

  public Scope copyWithChildChain() {
    return copyWithChildChain(null);
  }
  
  public Scope copyWithChildChain(Scope parent) {
    Scope result = new Scope(owner, getName(), parent);
    result.variables=variables;
    result.mixins=mixins;
    result.presentInTree = presentInTree;
    for (Scope kid : getChilds()) {
      kid.copyWithChildChain(result);
    }
    
    return result;
  }

  public Scope copyWithParentsChain() {
    Scope parent = hasParent()? getParent().copyWithParentsChain() : null;
    Scope result = new Scope(owner, getName(), parent);
    result.variables=variables;
    result.mixins=mixins;
    result.presentInTree = presentInTree;
    return result;
  }

  public void setParent(Scope parent) {
    if (parent == null && hasParent()) {
      getParent().getChilds().remove(this);
    }

    this.parent = parent;
    
    if (parent != null)
      parent.addChild(this);
  }

  public void removedFromTree() {
    presentInTree = false;
  }

  public boolean isPresentInTree() {
    return presentInTree;
  }

  public Scope getRootScope() {
    if (!hasParent())
      return this;
    
    return getParent().getRootScope();
  }

  public Scope getChildOwnerOf(ASTCssNode body) {
    for (Scope kid : getChilds()) {
      if (kid.owner==body)
        return kid;
    }
    return null;
  }

}
