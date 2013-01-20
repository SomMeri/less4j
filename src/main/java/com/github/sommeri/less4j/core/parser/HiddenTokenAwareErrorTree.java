package com.github.sommeri.less4j.core.parser;

import java.net.URL;
import java.util.List;

import org.antlr.runtime.RecognitionException;
import org.antlr.runtime.Token;
import org.antlr.runtime.TokenStream;
import org.antlr.runtime.tree.CommonErrorNode;
import org.antlr.runtime.tree.Tree;

public class HiddenTokenAwareErrorTree extends HiddenTokenAwareTree {

  private final CommonErrorNode errorNode;

  public HiddenTokenAwareErrorTree(TokenStream input, Token start, Token stop, RecognitionException e, URL inputFile) {
    super(inputFile);
    this.errorNode = new CommonErrorNode(input, start, stop, e);
  }

  public HiddenTokenAwareTree getChild(int i) {
    return (HiddenTokenAwareTree) errorNode.getChild(i);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public List getChildren() {
    return errorNode.getChildren();
  }

  public boolean isNil() {
    return errorNode.isNil();
  }

  public int getType() {
    return errorNode.getType();
  }

  public String getText() {
    return errorNode.getText();
  }

  public Tree getFirstChildWithType(int type) {
    return errorNode.getFirstChildWithType(type);
  }

  public Token getToken() {
    return errorNode.getToken();
  }

  public Tree dupNode() {
    return errorNode.dupNode();
  }

  public void addChild(Tree t) {
    errorNode.addChild(t);
  }

  public String toString() {
    return errorNode.toString();
  }

  public int getCharPositionInLine() {
    if (errorNode.trappedException!=null && errorNode.trappedException.token!=null) {
      return errorNode.trappedException.token.getCharPositionInLine();
    }
    return errorNode.getCharPositionInLine();
  }

  public int getTokenStartIndex() {
    return errorNode.getTokenStartIndex();
  }

  public void setTokenStartIndex(int index) {
    errorNode.setTokenStartIndex(index);
  }

  public int getTokenStopIndex() {
    return errorNode.getTokenStopIndex();
  }

  public void setTokenStopIndex(int index) {
    errorNode.setTokenStopIndex(index);
  }

  public void setUnknownTokenBoundaries() {
    errorNode.setUnknownTokenBoundaries();
  }

  public void addChildren(@SuppressWarnings("rawtypes") List kids) {
    errorNode.addChildren(kids);
  }

  public void setChild(int i, Tree t) {
    errorNode.setChild(i, t);
  }

  public void setParent(Tree t) {
    errorNode.setParent(t);
  }

  public void setChildIndex(int index) {
    errorNode.setChildIndex(index);
  }

  public Object deleteChild(int i) {
    return errorNode.deleteChild(i);
  }

  public boolean equals(Object obj) {
    return errorNode.equals(obj);
  }

  public int getChildCount() {
    return errorNode.getChildCount();
  }

  public int getLine() {
    if (errorNode.trappedException!=null && errorNode.trappedException.token!=null) {
      return errorNode.trappedException.token.getLine();
    }
    return errorNode.getLine();
  }

  public int getChildIndex() {
    return errorNode.getChildIndex();
  }

  public HiddenTokenAwareTree getParent() {
    return (HiddenTokenAwareTree)errorNode.getParent();
  }

  public void freshenParentAndChildIndexes() {
    errorNode.freshenParentAndChildIndexes();
  }

  public void freshenParentAndChildIndexes(int offset) {
    errorNode.freshenParentAndChildIndexes(offset);
  }

  public void freshenParentAndChildIndexesDeeply() {
    errorNode.freshenParentAndChildIndexesDeeply();
  }

  public void freshenParentAndChildIndexesDeeply(int offset) {
    errorNode.freshenParentAndChildIndexesDeeply(offset);
  }

  public void sanityCheckParentAndChildIndexes(Tree parent, int i) {
    errorNode.sanityCheckParentAndChildIndexes(parent, i);
  }

  public boolean hasAncestor(int ttype) {
    return errorNode.hasAncestor(ttype);
  }

  public Tree getAncestor(int ttype) {
    return errorNode.getAncestor(ttype);
  }

  public int hashCode() {
    return errorNode.hashCode();
  }

  public void insertChild(int i, Object t) {
    errorNode.insertChild(i, t);
  }

  public void replaceChildren(int startChildIndex, int stopChildIndex, Object t) {
    errorNode.replaceChildren(startChildIndex, stopChildIndex, t);
  }

  public void sanityCheckParentAndChildIndexes() {
    errorNode.sanityCheckParentAndChildIndexes();
  }

  @SuppressWarnings("rawtypes")
  public List getAncestors() {
    return errorNode.getAncestors();
  }

  public String toStringTree() {
    return errorNode.toStringTree();
  }

  public Token getStart() {
    return errorNode.start;
  }

  public Token getStop() {
    return errorNode.stop;
  }
  
  

}
