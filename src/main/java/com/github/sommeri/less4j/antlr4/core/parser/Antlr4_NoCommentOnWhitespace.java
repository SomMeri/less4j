package com.github.sommeri.less4j.antlr4.core.parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.sommeri.less4j.core.parser.LessLexer;

/**
 * This class is NOT thread-safe. 
 * 
 * The combined merges a list of tokens with abstract syntax tree. The merging algorithm 
 * assigns each token to exactly one node in abstract syntax tree.
 * 
 */
public class Antlr4_NoCommentOnWhitespace {
  private final TreeComments comments;
  
  public Antlr4_NoCommentOnWhitespace(TreeComments comments) {
    super();
    this.comments = comments;
  }

  public void associate(ParseTree ast) {
//    LinkedList<ParseTree> children = getChildren(ast);
//    for
  }

  private List<ParseTree> getChildren(ParseTree ast) {
    List<ParseTree> children = new LinkedList<ParseTree>();
    for (int i = 0; i < ast.getChildCount(); i++) {
      children.add(ast.getChild(i));
    }

    LinkedList<ParseTree> copy = new LinkedList<ParseTree>(children);
    Collections.sort(copy, new PositionComparator());
    return copy;
  }

  private int getTokenStartIndex(ParseTree tree) {
    return tree.getSourceInterval().a;
  }

  private int getTokenStopIndex(ParseTree tree) {
    return tree.getSourceInterval().b;
  }

  private void addFollowing(ParseTree node, List<CommonToken> hiddenTokens) {
    if (hiddenTokens==null || hiddenTokens.isEmpty()) 
      return ;
    
    System.out.println("addFollowing");
    comments.get(node).addFollowing(hiddenTokens);
  }

  private void addPreceding(ParseTree node, List<CommonToken> hiddenTokens) {
    if (hiddenTokens==null || hiddenTokens.isEmpty()) 
      return ;
    
    System.out.println("addPreceding");
    comments.get(node).addPreceding(hiddenTokens);
  }

  private void addOrphans(ParseTree node, List<CommonToken> hiddenTokens) {
    if (hiddenTokens==null || hiddenTokens.isEmpty()) 
      return ;
    
    System.out.println("addOrphans");
    comments.get(node).addOrphans(hiddenTokens);
  }

  class PositionComparator implements Comparator<ParseTree> {

    @Override
    public int compare(ParseTree arg0, ParseTree arg1) {
      return getTokenStartIndex(arg0) - getTokenStartIndex(arg1);
    }

  }

  //FIXME: (antlr4) clean up ugly method
  public TreeComments getAssociatedMap() {
    return comments;
  }

}
