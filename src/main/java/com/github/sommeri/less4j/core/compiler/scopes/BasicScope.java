package com.github.sommeri.less4j.core.compiler.scopes;

import java.util.Iterator;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.Variable;

public class BasicScope extends ComposedDumbScope implements IScope {

  public BasicScope(ILocalScope localScope, IScopesTree surroundingScopes) {
    super(localScope, surroundingScopes);
  }

  public IScope firstChild() {
    return getChilds().get(0);
  }

  public Expression getValue(Variable variable) {
    return getValue(variable.getName());
  }

  public Expression getValue(String name) {
    Expression value = getLocalValue(name);

    if (value == null && hasParent())
      value = getParent().getValue(name);

    return value;
  }

  public IScope childByOwners(ASTCssNode headNode, ASTCssNode... restNodes) {
    IScope head = getChildOwnerOf(headNode);
    if (head == null)
      return null;

    for (ASTCssNode node : restNodes) {
      head = head.getChildOwnerOf(node);
      if (head == null)
        return null;
    }

    return head;
  }

  public IScope getChildOwnerOf(ASTCssNode body) {
    for (IScope kid : getChilds()) {
      if (kid.getOwner() == body)
        return kid;
    }
    return null;
  }

  public boolean seesLocalDataOf(IScope otherScope) {
    if (getLocalScope().hasTheSameLocalData(otherScope.getLocalScope()))
      return true;

    if (!hasParent())
      return false;

    return getParent().seesLocalDataOf(otherScope);
  }

  public IScope getRootScope() {
    if (!hasParent())
      return this;

    return getParent().getRootScope();
  }

  public IScope skipBodyOwner() {
    if (isBodyOwnerScope())
      return firstChild().skipBodyOwner();

    return this;
  }

  public void setParentKeepConsistency(IScope parent) {
    if (getSurroundingScopes().hasParent()) {
      getParent().getChilds().remove(this);
    }

    setParent(parent);

    if (parent != null)
      parent.addChild(this);
  }

  @Override
  public void setParent(IScope parent) {
    getSurroundingScopes().setParent(parent);
  }

  public void replaceChild(IScope parent, IScope child, List<IScope> replacements) {
    List<IScope> inList = parent.getChilds();
    int indexOf = inList.indexOf(child);
    inList.remove(indexOf);
    inList.addAll(indexOf, replacements);

    for (IScope kid : replacements) {
      kid.setParent(parent);
    }
  }

  @Override
  public String toString() {
    StringBuilder result = new StringBuilder(toFullName());
    return result.toString();
  }

  public String toFullName() {
    if (hasParent())
      return getParent().toFullName() + " > " + toSimpleName();

    return toSimpleName().toString();
  }

  private String toSimpleName() {
    List<String> names = getNames();
    if (names!=null && !names.isEmpty())
      return getType() + names;
    
    if (ScopeFactory.DEFAULT!=getType())
      return getType() + "[" + getOwner().toString() + "]";
    
    return getType();
  }

  public String toLongString() {
    return toLongString(0).toString();
  }

  public StringBuilder toLongString(int level) {
    String prefix = "";
    for (int i = 0; i < level; i++) {
      prefix += "  ";
    }
    StringBuilder text = new StringBuilder(prefix);

    Iterator<String> iNames = getNames().iterator();
    text.append(iNames.next());
    while (iNames.hasNext()) {
      text.append(", ").append(iNames.next());
    }

    text.append("(").append(getLocalVariables().size()).append(", ").append(getLocalMixins().size());
    text.append(") {").append("\n");

    for (IScope kid : getChilds()) {
      text.append(kid.toLongString(level + 1));
    }
    text.append(prefix).append("}").append("\n");
    return text;
  }

}
