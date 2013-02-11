package com.github.sommeri.less4j.core.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

import com.github.sommeri.less4j.LessSource;

public class HiddenTokenAwareTree extends CommonTree {

  private final LessSource source;
  private List<Token> preceding = new LinkedList<Token>();
  private List<Token> orphans = new LinkedList<Token>();
  private List<Token> following = new LinkedList<Token>();

  public HiddenTokenAwareTree(Token payload, LessSource source) {
    super(payload);
    this.source = source;
  }

  public HiddenTokenAwareTree(LessSource source) {
    super();
    this.source = source;
  }

  public HiddenTokenAwareTree(CommonTree node, LessSource source) {
    super(node);
    this.source = source;
  }

  public LessSource getSource() {
    return source;
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
    return (HiddenTokenAwareTree) super.getParent();
  }

  public List<Token> getPreceding() {
    return preceding;

  }

  public List<Token> chopPreceedingUpToLastOfType(int type) {
    List<Token> result = new ArrayList<Token>();
    List<Token> preceding = getPreceding();
    
    int index = lastTokenOfType(preceding, type);
    for (int i = 0; i <= index; i++) {
      Token next = preceding.remove(0);
      result.add(next);
    }
    
    return result;
  }

  private int lastTokenOfType(List<Token> list, int type) {
    if (list.isEmpty())
      return -1;

    for (int i = list.size() - 1; i >= 0; i--) {
      Token token = (Token) list.get(i);
      if (token.getType() == type)
        return i;
    }
    return -1;
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
    if (children == null || children.isEmpty())
      return;

    HiddenTokenAwareTree first = children.get(0);
    first.addBeforePreceding(getPreceding());
    HiddenTokenAwareTree last = children.get(children.size() - 1);
    last.addFollowing(getFollowing());
  }

  public void giveHidden(HiddenTokenAwareTree previous, HiddenTokenAwareTree next) {
    if (previous != null)
      previous.addFollowing(getPreceding());
    if (next != null)
      next.addBeforePreceding(getFollowing());
  }

  public HiddenTokenAwareTree getLastChild() {
    return getChild(getChildCount() - 1);
  }

  public String toString() {
    return super.toString() + " " + getLine() + ":" + getCharPositionInLine() + 1;
  }

  public HiddenTokenAwareTree getNextSibling() {
    if (getParent() == null)
      return null;

    List<HiddenTokenAwareTree> siblings = getParent().getChildren();
    int indx = siblings.indexOf(this) + 1;
    return indx >= siblings.size() ? null : siblings.get(indx);
  }

  public HiddenTokenAwareTree getPreviousSibling() {
    if (getParent() == null)
      return null;

    List<HiddenTokenAwareTree> siblings = getParent().getChildren();
    int indx = siblings.indexOf(this) - 1;
    return indx < 0 ? null : siblings.get(indx);
  }

  public void pushHiddenToSiblings() {
    HiddenTokenAwareTree previousSibling = getPreviousSibling();
    if (previousSibling != null) {
      previousSibling.addFollowing(getPreceding());
    }

    HiddenTokenAwareTree nextSibling = getNextSibling();
    if (nextSibling != null) {
      nextSibling.addBeforePreceding(getFollowing());
    }
  }

  public void removePreceding() {
    preceding = new ArrayList<Token>();
  }

}
