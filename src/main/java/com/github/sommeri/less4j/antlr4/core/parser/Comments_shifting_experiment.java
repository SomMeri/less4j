package com.github.sommeri.less4j.antlr4.core.parser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Comment;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.parser.LessG4Lexer;
import com.github.sommeri.less4j.core.problems.BugHappened;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class Comments_shifting_experiment {

  private final TreeComments treeComments;

  public Comments_shifting_experiment(ProblemsHandler problemsHandler, TreeComments treeComments) {
    this.treeComments = treeComments;
  }

  public void commentsForNode(ASTCssNode node) {
    ParseTree ctx = treeNode(node);
    commentsForNode(node, ctx);
  }

  public void commentsForNode(ASTCssNode node, ParseTree ctx) {
    inheritCommentsFromToken(node, ctx);

    //inherit from own underlying
    List<? extends ASTCssNode> childs = node.getChilds();
    List<ParseTree> kids = getChildren(ctx);
    Map<ParseTree, ASTCssNode> used = new HashMap<ParseTree, ASTCssNode>();
    Set<ParseTree> parentsChain = extractParents(ctx);

    for (ASTCssNode kid : childs) {
      ParseTree kidNode = treeNode(kid);
      commentsForNode(kid, kidNode);
      while (!parentsChain.contains(kidNode.getParent())) {
        ParseTree parent = kidNode.getParent();
        inheritCommentsFromTokenChilds(kid, parent, kidNode);
        kidNode = parent;
      }
      used.put(kidNode, kid);
    }

    //middle comments
    ASTCssNode lastKid = null;
    List<Comment> collectedComments = new ArrayList<Comment>();
    for (ParseTree kid : kids) {
      if (used.containsKey(kid)) {
        //preceed parse tree by all preceeding
        lastKid = used.get(kid);
        lastKid.addBeforeOpeningComments(collectedComments);
        collectedComments = new ArrayList<Comment>();
      } else {
        //collect comments add to collect list
        NodeCommentsHolder comments = treeComments.getOrCreate(kid);
        collectedComments.addAll(convertToComments(comments.getPreceding()));
        collectedComments.addAll(convertToComments(comments.getOrphans()));
        collectedComments.addAll(convertToComments(comments.getFollowing()));
      }
    }

    //attach all collected after last kid
    if (lastKid != null)
      lastKid.addTrailingComments(collectedComments);
    
  }

  private Set<ParseTree> extractParents(ParseTree ctx) {
    Set<ParseTree> parentsChain = new HashSet<ParseTree>();
    ParseTree parentTree = ctx;
    while (parentTree != null) {
      parentsChain.add(parentTree);
      parentTree = parentTree.getParent();
    }
    return parentsChain;
  }

  private void inheritCommentsFromTokenChilds(ASTCssNode node, ParseTree parent, ParseTree kidNode) {
    inheritCommentsFromToken(node, parent);

    boolean before = true;
    int ctxCnt = parent.getChildCount();
    for (int i = 0; i < ctxCnt; i++) {
      ParseTree child = parent.getChild(i);
      if (child == kidNode) {
        before = false;
      } else {
        NodeCommentsHolder comments = treeComments.getOrCreate(child);
        List<Comment> allComments = new ArrayList<Comment>();
        allComments.addAll(convertToComments(comments.getPreceding()));
        allComments.addAll(convertToComments(comments.getOrphans()));
        allComments.addAll(convertToComments(comments.getFollowing()));
        if (before) {
          node.addBeforeOpeningComments(allComments);
        } else {
          node.addTrailingComments(allComments);
        }
      }

    }
  }

  private ParseTree treeNode(ASTCssNode kid) {
    HiddenTokenAwareTree underlyingStructure = kid.getUnderlyingStructure();
    if (underlyingStructure instanceof HiddenTokenAwareTreeAdapter) {
      HiddenTokenAwareTreeAdapter adapter = (HiddenTokenAwareTreeAdapter) underlyingStructure;
      ParseTree underlyingNode = adapter.getUnderlyingNode();
      return underlyingNode;
    }
    throw new BugHappened("Unexpected underlying type.", kid);
  }

  private List<ParseTree> getChildren(ParseTree ctx) {
    List<ParseTree> treeChildren = new ArrayList<ParseTree>();
    int ctxCnt = ctx.getChildCount();
    for (int i = 0; i < ctxCnt; i++) {
      ParseTree child = ctx.getChild(i);
      treeChildren.add(child);
    }
    return treeChildren;
  }

  private void inheritCommentsFromToken(ASTCssNode node, ParseTree ctx) {
    NodeCommentsHolder comments = treeComments.getOrCreate(ctx);
    List<Comment> preceding = convertToComments(comments.getPreceding());
    node.addOpeningComments(preceding);

    List<Comment> following = convertToComments(comments.getFollowing());
    node.addTrailingComments(following);

    List<Comment> orphans = convertToComments(comments.getOrphans());
    node.addOrphanComments(orphans);

    comments.removeAll();
  }

  private List<Comment> convertToComments(List<CommonToken> list) {
    List<Comment> result = new ArrayList<Comment>();

    Comment comment = null;
    for (CommonToken token : list) {
      if (token.getType() == LessG4Lexer.COMMENT) {
        comment = new Comment(new HiddenTokenAwareTreeAdapter(token));
        result.add(comment);
      }
      if (token.getType() == LessG4Lexer.NEW_LINE) {
        if (comment != null)
          comment.setHasNewLine(true);
      }
    }

    return result;
  }

}
