package com.github.sommeri.less4j.core.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.Token;
import org.antlr.runtime.tree.CommonTree;

import com.github.sommeri.less4j.LessSource;

public class HiddenTokenAwareTree extends CommonTree {

  private final LessSource source;
  private List<CommonToken> preceding = new LinkedList<CommonToken>();
  private List<CommonToken> orphans = new LinkedList<CommonToken>();
  private List<CommonToken> following = new LinkedList<CommonToken>();
  private CommonToken tokenAsCommon;

  public HiddenTokenAwareTree(CommonToken payload, LessSource source) {
    super(payload);
    this.source = source;
    tokenAsCommon = payload;
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

  public boolean hasChildren() {
    @SuppressWarnings("rawtypes")
    List children = super.getChildren();
    return children!=null && !children.isEmpty();
  }

  @Override
  public HiddenTokenAwareTree getParent() {
    return (HiddenTokenAwareTree) super.getParent();
  }

  public List<CommonToken> getPreceding() {
    return preceding;

  }

  public List<CommonToken> chopPreceedingUpToLastOfType(int type) {
    List<CommonToken> result = new ArrayList<CommonToken>();
    List<CommonToken> preceding = getPreceding();
    
    int index = lastTokenOfType(preceding, type);
    for (int i = 0; i <= index; i++) {
      CommonToken next = preceding.remove(0);
      result.add(next);
    }
    
    return result;
  }

  private int lastTokenOfType(List<CommonToken> list, int type) {
    if (list.isEmpty())
      return -1;

    for (int i = list.size() - 1; i >= 0; i--) {
      Token token = (Token) list.get(i);
      if (token.getType() == type)
        return i;
    }
    return -1;
  }

  public List<CommonToken> getFollowing() {
    return following;
  }

  public List<CommonToken> getOrphans() {
    return orphans;
  }

  public void addPreceding(CommonToken token) {
    preceding.add(token);
  }

  public void addPreceding(List<CommonToken> tokens) {
    preceding.addAll(tokens);
  }

  public void addBeforePreceding(List<CommonToken> tokens) {
    preceding.addAll(0, tokens);
  }

  public void addOrphan(CommonToken token) {
    orphans.add(token);
  }

  public void addOrphans(List<CommonToken> tokens) {
    orphans.addAll(tokens);
  }

  public void addFollowing(CommonToken token) {
    following.add(token);
  }

  public void addBeforeFollowing(List<CommonToken> tokens) {
    following.addAll(0, tokens);
  }

  public void addFollowing(List<CommonToken> tokens) {
    following.addAll(tokens);
  }

  public void pushHiddenToKids() {
    List<HiddenTokenAwareTree> children = getChildren();
    if (children == null || children.isEmpty())
      return;

    HiddenTokenAwareTree first = children.get(0);
    first.addBeforePreceding(getPreceding());
    removePreceding();
    HiddenTokenAwareTree last = children.get(children.size() - 1);
    last.addFollowing(getFollowing());
    removeFollowing();
  }

  public void giveHidden(HiddenTokenAwareTree previous, HiddenTokenAwareTree next) {
    if (previous != null)
      previous.addFollowing(getPreceding());
    if (next != null)
      next.addBeforePreceding(getFollowing());
  }

  public void moveHidden(HiddenTokenAwareTree previous, HiddenTokenAwareTree next) {
    if (previous != null) {
      previous.addFollowing(getPreceding());
      preceding = new LinkedList<CommonToken>();
    }
    if (next != null) {
      next.addBeforePreceding(getFollowing());
      following = new LinkedList<CommonToken>();
    }
  }

  public HiddenTokenAwareTree getLastChild() {
    return getChild(getChildCount() - 1);
  }

  /**
   * Lines numbering starts with 1.
   */
  public int getLine() {
    if ( !isReal() ) {
      HiddenTokenAwareTree realChild = getFirstRealDescendant();
      if ( realChild!=null ) {
        return realChild.getLine();
      }
      return 0;
    }
    return token.getLine();
  }

  /**
   * Columns numbering starts with 1.
   */
  public int getColumn() {
    if ( !isReal() ) {
      HiddenTokenAwareTree realChild = getFirstRealDescendant();
      if ( realChild!=null ) {
        return realChild.getCharPositionInLine();
      }
      return 0;
    }
    return token.getCharPositionInLine();
  }

  private HiddenTokenAwareTree getFirstRealDescendant() {
    for (HiddenTokenAwareTree child : getChildren()) {
      if (child.isReal())
        return child;
      else {
        HiddenTokenAwareTree descendant = child.getFirstRealDescendant();
        if (descendant!=null)
          return descendant;
      }
    }
    return null;
  }

  public int getCharPositionInLine() {
    if ( !isReal() ) {
      HiddenTokenAwareTree realChild = getFirstRealDescendant();
      if ( realChild!=null ) {
        return realChild.getCharPositionInLine();
      }
      return 1;
    }
    return token.getCharPositionInLine();
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
    preceding = new ArrayList<CommonToken>();
  }

  public void removeFollowing() {
    following = new ArrayList<CommonToken>();
  }

  public boolean isReal() {
    return tokenAsCommon!=null && tokenAsCommon.getTokenIndex()!=-1;
  }

}
