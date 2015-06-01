package com.github.sommeri.less4j.antlr4.core.parser;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Comment;
import com.github.sommeri.less4j.core.parser.LessG4Lexer;

public class Antlr4_ASTBuilderBase {

  private final TreeComments treeComments;

  public Antlr4_ASTBuilderBase(TreeComments treeComments) {
    this.treeComments = treeComments;
  }

  protected <T extends ASTCssNode> T handleComments(T node, ParserRuleContext ctx) {
    return inheritCommentsFromToken(node, ctx);
  }

  private <T extends ASTCssNode> T inheritCommentsFromToken(T node, ParserRuleContext ctx) {
    NodeCommentsHolder comments = treeComments.getOrCreate(ctx);
    List<Comment> preceding = convertToComments(comments.getPreceding(), ctx);
    node.setOpeningComments(preceding);

    List<Comment> following = convertToComments(comments.getFollowing(), ctx);
    node.setTrailingComments(following);

    List<Comment> orphans = convertToComments(comments.getOrphans(), ctx);
    node.setOrphanComments(orphans);
    
    return node;
  }

  private List<Comment> convertToComments(List<CommonToken> list, ParserRuleContext ctx) {
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
