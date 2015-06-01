package com.github.sommeri.less4j.antlr4.core.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;

import com.github.sommeri.less4j.core.parser.LessG4Lexer;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Mandatory_wsContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.WsContext;

/**
 * This class is NOT thread-safe. 
 * 
 * The combined merges a list of tokens with abstract syntax tree. The merging algorithm 
 * assigns each token to exactly one node in abstract syntax tree.
 * 
 */
public class Antlr4_ListToTreeCombiner {

  //FIXME: (antlr4) remove all sysout replace by proper logging 
  private final boolean DEBUG = false;
  private LinkedList<CommonToken> hiddenTokens;
  private final TreeComments comments = new TreeComments();

  public TreeComments associate(ParseTree ast, LinkedList<CommonToken> hiddenTokens) {
    initialize(hiddenTokens);

    LinkedList<ParseTree> children = getChildren(ast);
    associateAllchilds(children);

    if (children.isEmpty()) {
      addAllContainedTokens(ast);
    } else {
      ParseTree lastChild = children.getLast();
      addFollowing(lastChild, hiddenTokens);
    }

    return comments;
  }

  private void initialize(LinkedList<CommonToken> hiddenTokens) {
    this.hiddenTokens = hiddenTokens;
  }

  private void associateAsChild(ParseTree ast) {
    addAllPrecedingTokens(ast);

    LinkedList<ParseTree> children = getChildren(ast);
    if (children.isEmpty()) {
      addAllContainedTokens(ast);
      return;
    }

    ParseTree lastChild = associateAllchilds(children);
    addFollowingTokens(lastChild, getTokenStopIndex(ast));
  }

  private ParseTree associateAllchilds(LinkedList<ParseTree> children) {
    ParseTree previousChild = null;
    for (ParseTree child : children) {
      assignFirstCommentsSegment(previousChild, child);
      associateAsChild(child);
      previousChild = child;
    }
    return previousChild;
  }

  private void addFollowingTokens(ParseTree target, int stop) {
    List<CommonToken> result = readPrefix(stop);
    addFollowing(target, result);
  }

  private void assignFirstCommentsSegment(ParseTree firstChild, ParseTree secondChild) {
    if (firstChild == null)
      return;

    LinkedList<CommonToken> tail = readTillNewLine(getTokenStartIndex(secondChild));
    if (tail.isEmpty())
      return;

    CommonToken lastInTail = tail.peekLast();
    if (lastInTail.getType() == LessG4Lexer.NEW_LINE)
      addFollowing(firstChild, tail);
    else
      addPreceding(secondChild, tail);
  }

  private void addAllContainedTokens(ParseTree ast) {
    int stop = getTokenStopIndex(ast);
    List<CommonToken> result = readPrefix(stop);
    addOrphans(ast, result);
  }

  private LinkedList<ParseTree> getChildren(ParseTree ast) {
    List<ParseTree> children = new LinkedList<ParseTree>();
    for (int i = 0; i < ast.getChildCount(); i++) {
      ParseTree kid = ast.getChild(i);
      if (counts(kid))
        children.add(kid);
    }

    LinkedList<ParseTree> copy = new LinkedList<ParseTree>(children);
    Collections.sort(copy, new PositionComparator());
    return copy;
  }

  private boolean counts(ParseTree kid) {
    if (kid instanceof WsContext) {
      return false;
    }
    if (kid instanceof Mandatory_wsContext) {
      return false;
    }
    if (kid instanceof TerminalNode) {
      return ((TerminalNode) kid).getSymbol().getType() != LessG4Lexer.EOF;
    }

    return true;
  }

  private void addAllPrecedingTokens(ParseTree target) {
    int upTo = getTokenStartIndex(target);
    if (target.getChildCount() == 0)
      upTo = getTokenStopIndex(target);

    List<CommonToken> tokens = readPrefix(upTo);
    addPreceding(target, tokens);
  }

  private LinkedList<CommonToken> readTillNewLine(int end) {
    LinkedList<CommonToken> result = new LinkedList<CommonToken>();
    if (hiddenTokens.isEmpty())
      return result;

    CommonToken first = hiddenTokens.peekFirst();
    while (first != null && first.getTokenIndex() < end && first.getType() == LessG4Lexer.COMMENT) {
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

  private int getTokenStartIndex(ParseTree tree) {
    return tree.getSourceInterval().a;
  }

  private int getTokenStopIndex(ParseTree tree) {
    return tree.getSourceInterval().b;
  }

  private void addFollowing(ParseTree node, List<CommonToken> hiddenTokens) {
    if (hiddenTokens == null || hiddenTokens.isEmpty())
      return;

    if (DEBUG)
      System.out.println("addFollowing: " + node + " comment: " + hiddenTokens.get(0));
    comments.getOrCreate(node).addFollowing(hiddenTokens);
  }

  private void addPreceding(ParseTree node, List<CommonToken> hiddenTokens) {
    if (hiddenTokens == null || hiddenTokens.isEmpty())
      return;

    if (DEBUG)
      System.out.println("addPreceding: " + node.getClass().getSimpleName() + " comment: " + hiddenTokens.get(0));
    comments.getOrCreate(node).addPreceding(hiddenTokens);
  }

  private void addOrphans(ParseTree node, List<CommonToken> hiddenTokens) {
    if (hiddenTokens == null || hiddenTokens.isEmpty())
      return;

    if (DEBUG)
      System.out.println("addOrphans: " + node + " comment: " + hiddenTokens.get(0));
    comments.getOrCreate(node).addOrphans(hiddenTokens);
  }

  class PositionComparator implements Comparator<ParseTree> {

    @Override
    public int compare(ParseTree arg0, ParseTree arg1) {
      return getTokenStartIndex(arg0) - getTokenStartIndex(arg1);
    }

  }

}
