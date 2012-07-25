package org.porting.less4j.core.parser;

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
  
  @SuppressWarnings("unchecked")
  public List<HiddenTokenAwareTree> getChildren() {
    return super.getChildren();
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

  public void addOrphan(Token token) {
    orphans.add(token);
  }

  public void addOrphans(List<Token> tokens) {
    orphans.addAll(tokens);
  }

  public void addFollowing(Token token) {
    following.add(token);
  }

  public void addFollowing(List<Token> tokens) {
    following.addAll(tokens);
  }

}
