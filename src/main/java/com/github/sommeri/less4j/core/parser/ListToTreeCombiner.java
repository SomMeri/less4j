package com.github.sommeri.less4j.core.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.tree.CommonTree;

import com.github.sommeri.less4j.core.parser.LessLexer;

/**
 * This class is NOT thread-safe. 
 * 
 * The combined merges a list of tokens with abstract syntax tree. The merging algorithm 
 * assigns each token to exactly one node in abstract syntax tree.
 * 
 */
public class ListToTreeCombiner {

  private LinkedList<CommonToken> hiddenTokens;

  public void associate(HiddenTokenAwareTree ast, LinkedList<CommonToken> hiddenTokens) {
    initialize(hiddenTokens);

    LinkedList<HiddenTokenAwareTree> children = getChildren(ast);
    associateAllchilds(children);

    if (children.isEmpty()) {
      addAllContainedTokens(ast);
    } else {
      HiddenTokenAwareTree lastChild = children.getLast();
      lastChild.addFollowing(hiddenTokens);
    }
  }

  private void initialize(LinkedList<CommonToken> hiddenTokens) {
    this.hiddenTokens = hiddenTokens;
  }

  private void associateAsChild(HiddenTokenAwareTree ast) {
    addAllPrecedingTokens(ast);

    LinkedList<HiddenTokenAwareTree> children = getChildren(ast);
    if (children.isEmpty()) {
      addAllContainedTokens(ast);
      return;
    }

    HiddenTokenAwareTree lastChild = associateAllchilds(children);
    addFollowingTokens(lastChild, ast.getTokenStopIndex());
  }

  private HiddenTokenAwareTree associateAllchilds(LinkedList<HiddenTokenAwareTree> children) {
    HiddenTokenAwareTree previousChild = null;
    for (HiddenTokenAwareTree child : children) {
      assignFirstCommentsSegment(previousChild, child);
      associateAsChild(child);
      previousChild = child;
    }
    return previousChild;
  }

  private void addFollowingTokens(HiddenTokenAwareTree target, int stop) {
    List<CommonToken> result = readPrefix(stop);
    target.addFollowing(result);
  }

//  private int stopIndexForLastChild(HiddenTokenAwareTree parent) {
//    int result = parent.getTokenStopIndex();
//    HiddenTokenAwareTree parentsSibling = parent.getNextSibling();
//    if (parentsSibling!=null)
//    return result;
//  }

  private void assignFirstCommentsSegment(HiddenTokenAwareTree firstChild, HiddenTokenAwareTree secondChild) {
    if (firstChild == null)
      return;

    LinkedList<CommonToken> tail = readTillNewLine(secondChild.getTokenStartIndex());
    if (tail.isEmpty())
      return;

    //FIXME: !!!!!!!!!!!! clean this up
    CommonToken lastInTail = tail.peekLast();
    if (lastInTail.getType() == LessLexer.NEW_LINE || lastInTail.getText().endsWith("\n"))
      firstChild.addFollowing(tail);
    else
      secondChild.addPreceding(tail);
  }

  private void addAllContainedTokens(HiddenTokenAwareTree ast) {
    int stop = ast.getTokenStopIndex();
    List<CommonToken> result = readPrefix(stop);
    ast.addOrphans(result);
  }

  private LinkedList<HiddenTokenAwareTree> getChildren(HiddenTokenAwareTree ast) {
    List<HiddenTokenAwareTree> children = ast.getChildren();
    if (children == null)
      return new LinkedList<HiddenTokenAwareTree>();

    LinkedList<HiddenTokenAwareTree> copy = new LinkedList<HiddenTokenAwareTree>(children);
    Collections.sort(copy, new PositionComparator());
    return copy;
  }

  private void addAllPrecedingTokens(HiddenTokenAwareTree target) {
    int start = target.getTokenStartIndex();
    List<CommonToken> tokens = readPrefix(start);
    target.addPreceding(tokens);
  }

  private LinkedList<CommonToken> readTillNewLine(int end) {
    LinkedList<CommonToken> result = new LinkedList<CommonToken>();
    if (hiddenTokens.isEmpty())
      return result;

    CommonToken first = hiddenTokens.peekFirst();
    while (first != null && first.getTokenIndex() < end && first.getType() == LessLexer.COMMENT) {
      result.add(first);
      hiddenTokens.removeFirst();
      first = hiddenTokens.peekFirst();
    }

    if (first == null || first.getTokenIndex() >= end)
      return result;

    result.add(first);
    return result;
  }

  private List<CommonToken> readPrefix(int end) {
    List<CommonToken> result = new ArrayList<CommonToken>();
    if (hiddenTokens.isEmpty())
      return result;

    CommonToken first = hiddenTokens.peekFirst();
    while (first != null && first.getTokenIndex() < end) {
      result.add(first);
      hiddenTokens.removeFirst();
      first = hiddenTokens.peekFirst();
    }
    return result;
  }

  class PositionComparator implements Comparator<CommonTree> {

    @Override
    public int compare(CommonTree arg0, CommonTree arg1) {
      return arg0.getTokenStartIndex() - arg1.getTokenStartIndex();
    }

  }

}
