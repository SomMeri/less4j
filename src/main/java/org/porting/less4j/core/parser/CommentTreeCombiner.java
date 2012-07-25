package org.porting.less4j.core.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.antlr.runtime.Token;

/**
 * This class is NOT thread-safe.
 * 
 */
public class CommentTreeCombiner {

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

  public void associateAsChild(HiddenTokenAwareTree ast) {
    addAllPreceedingTokens(ast);

    LinkedList<HiddenTokenAwareTree> children = getChildren(ast);
    HiddenTokenAwareTree previousChild = null;
    for (HiddenTokenAwareTree child : children) {
      assignFirstCommentsSegment(previousChild, child);
      associateAsChild(child);
      previousChild = child;
    }

    if (children.isEmpty()) {
      addAllContainedTokens(ast);
    } else {
      addFollowingTokens(children.getLast(), ast.getTokenStopIndex());
    }
  }

  public void assignFirstCommentsSegment(HiddenTokenAwareTree firstChild, HiddenTokenAwareTree secondChild) {
    LinkedList<Token> tail = readTillNewLine(hiddenTokens, secondChild.getTokenStartIndex());
    if (!tail.isEmpty() && firstChild != null) {
      if (tail.peekLast().getType() == LessLexer.NEW_LINE)
        firstChild.addFollowing(tail);
      else
        secondChild.addPreceeding(tail);
    }
  }

  private void addAllContainedTokens(HiddenTokenAwareTree ast) {
    int stop = ast.getTokenStopIndex();
    List<Token> result = readPrefix(hiddenTokens, stop);
    ast.addOrphans(result);
  }

  private void addFollowingTokens(HiddenTokenAwareTree target, int stop) {
    List<Token> result = readPrefix(hiddenTokens, stop);
    target.addFollowing(result);
  }

  private LinkedList<HiddenTokenAwareTree> getChildren(HiddenTokenAwareTree ast) {
    List<HiddenTokenAwareTree> children = ast.getChildren();
    if (children == null)
      return new LinkedList<HiddenTokenAwareTree>();
    // FIXME can not handle error nodes !!!
    LinkedList<HiddenTokenAwareTree> copy = new LinkedList<HiddenTokenAwareTree>(children);
    Collections.sort(copy, new PositionComparator());
    return copy;
  }

  private void addAllPreceedingTokens(HiddenTokenAwareTree target) {
    int start = target.getTokenStartIndex();
    List<Token> tokens = readPrefix(hiddenTokens, start);
    target.addPreceeding(tokens);
  }

  // FIXME: document comments and new lines only condition
  private LinkedList<Token> readTillNewLine(LinkedList<Token> hiddenTokens, int end) {
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

  private List<Token> readPrefix(LinkedList<Token> hiddenTokens, int end) {
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

  class PositionComparator implements Comparator<HiddenTokenAwareTree> {

    @Override
    public int compare(HiddenTokenAwareTree arg0, HiddenTokenAwareTree arg1) {
      return arg0.getTokenStartIndex() - arg1.getTokenStopIndex();
    }

  }

}
