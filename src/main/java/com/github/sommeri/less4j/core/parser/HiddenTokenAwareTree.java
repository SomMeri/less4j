package com.github.sommeri.less4j.core.parser;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

public class HiddenTokenAwareTree extends CommonTree {
  
  private List<Token> preceding = new LinkedList<Token>();
  private List<Token> orphans = new LinkedList<Token>();
  private List<Token> following = new LinkedList<Token>();

  public HiddenTokenAwareTree(Token payload) {
    super(payload);
  }

  public HiddenTokenAwareTree() {
    super();
  }

  public HiddenTokenAwareTree(CommonTree node) {
    super(node);
  }
  
  @Override
  public HiddenTokenAwareTree getChild(int i) {
    return (HiddenTokenAwareTree) super.getChild(i);
  }

  @SuppressWarnings("unchecked")
  public List<HiddenTokenAwareTree> getChildren() {
    List<HiddenTokenAwareTree> result = super.getChildren();
    if (result == null)
      result = Collections.emptyList();
    return result;
  }

  @Override
  public HiddenTokenAwareTree getParent() {
    return (HiddenTokenAwareTree)super.getParent();
  }

  public List<Token> getPreceding() {
    return preceding;
    
  }

  public List<Token> getFollowing() {
    return following;
  }

  public List<Token> getOrphans() {
    return orphans;
  }

  public void addPreceding(Token token) {
    preceding.add(token);
  }

  public void addPreceding(List<Token> tokens) {
    preceding.addAll(tokens);
  }

  public void addBeforePreceding(List<Token> tokens) {
    preceding.addAll(0, tokens);
  }

  public void addOrphan(Token token) {
    orphans.add(token);
  }

  public void addOrphans(List<Token> tokens) {
    orphans.addAll(tokens);
  }

  public void addFollowing(Token token) {
    following.add(token);
  }

  public void addBeforeFollowing(List<Token> tokens) {
    following.addAll(0, tokens);
  }

  public void addFollowing(List<Token> tokens) {
    following.addAll(tokens);
  }

  public void pushHiddenToKids() {
    List<HiddenTokenAwareTree> children = getChildren();
    if (children==null || children.isEmpty())
      return ;
    
    HiddenTokenAwareTree first = children.get(0);
    first.addBeforePreceding(getPreceding());
    HiddenTokenAwareTree last = children.get(children.size()-1);
    last.addFollowing(getFollowing());
  }

  public void giveHidden(HiddenTokenAwareTree previous, HiddenTokenAwareTree next) {
    if (previous!=null)
      previous.addFollowing(getPreceding());
    if (next!=null)
      next.addBeforePreceding(getFollowing());   
  }

  public HiddenTokenAwareTree getLastChild() {
    return getChild(getChildCount()-1);
  }

  public String toString() {
    return super.toString() +" "+ getLine() + ":"+ getCharPositionInLine()+1;
}

}
