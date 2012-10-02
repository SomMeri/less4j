package com.github.sommeri.less4j.core.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.antlr.runtime.Token;
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

  private LinkedList<Token> hiddenTokens;

  public void associate(HiddenTokenAwareTree ast, LinkedList<Token> hiddenTokens) {
    initialize(hiddenTokens);

    LinkedList<HiddenTokenAwareTree> children = getChildren(ast);
    for (HiddenTokenAwareTree child : children) {
      associateAsChild(child);
    }

    if (children.isEmpty()) {
      addAllContainedTokens(ast);
    } else {
      HiddenTokenAwareTree lastChild = children.getLast();
      lastChild.addFollowing(hiddenTokens);
    }
  }

  private void initialize(LinkedList<Token> hiddenTokens) {
    this.hiddenTokens = hiddenTokens;
  }

  private void associateAsChild(HiddenTokenAwareTree ast) {
    addAllPrecedingTokens(ast);

    LinkedList<HiddenTokenAwareTree> children = getChildren(ast);
    if (children.isEmpty()) {
      addAllContainedTokens(ast);
      return;
    }

    HiddenTokenAwareTree previousChild = null;
    for (HiddenTokenAwareTree child : children) {
      assignFirstCommentsSegment(previousChild, child);
      associateAsChild(child);
      previousChild = child;
    }

    addFollowingTokens(previousChild, ast.getTokenStopIndex());
  }

  private void assignFirstCommentsSegment(HiddenTokenAwareTree firstChild, HiddenTokenAwareTree secondChild) {
    if (firstChild == null)
      return;

    LinkedList<Token> tail = readTillNewLine(secondChild.getTokenStartIndex());
    if (tail.isEmpty())
      return;

    if (tail.peekLast().getType() == LessLexer.NEW_LINE)
      firstChild.addFollowing(tail);
    else
      secondChild.addPreceding(tail);
  }

  private void addAllContainedTokens(HiddenTokenAwareTree ast) {
    int stop = ast.getTokenStopIndex();
    List<Token> result = readPrefix(stop);
    ast.addOrphans(result);
  }

  private void addFollowingTokens(HiddenTokenAwareTree target, int stop) {
    List<Token> result = readPrefix(stop);
    target.addFollowing(result);
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
    List<Token> tokens = readPrefix(start);
    target.addPreceding(tokens);
  }

  private LinkedList<Token> readTillNewLine(int end) {
    LinkedList<Token> result = new LinkedList<Token>();
    if (hiddenTokens.isEmpty())
      return result;

    Token first = hiddenTokens.peekFirst();
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

  private List<Token> readPrefix(int end) {
    List<Token> result = new ArrayList<Token>();
    if (hiddenTokens.isEmpty())
      return result;

    Token first = hiddenTokens.peekFirst();
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
