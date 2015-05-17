package com.github.sommeri.less4j.antlr4.core.parser;

import java.util.ArrayList;
import java.util.List;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.ParserRuleContext;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Comment;
import com.github.sommeri.less4j.core.ast.Declaration;
import com.github.sommeri.less4j.core.parser.LessG4Lexer;
import com.github.sommeri.less4j.core.parser.LessG4Parser.DeclarationContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.General_body_memberContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.Top_level_elementContext;
import com.github.sommeri.less4j.core.problems.BugHappened;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class CommentsShiftingBuilderSwitch extends Antlr4_ASTBuilderSwitch {

  private final TreeComments treeComments;

  public CommentsShiftingBuilderSwitch(ProblemsHandler problemsHandler, TreeComments treeComments) {
    super(problemsHandler);
    this.treeComments = treeComments;
  }

  @Override
  public ASTCssNode visitTop_level_element(Top_level_elementContext ctx) {
    ASTCssNode result = super.visitTop_level_element(ctx);
    inheritCommentsFromToken(result, ctx);
    return result;
  }

  @Override
  public Declaration visitDeclaration(DeclarationContext ctx) {
    Declaration result = super.visitDeclaration(ctx);
    inheritCommentsFromToken(result, ctx);
    return result;
  }

  @Override
  public ASTCssNode visitGeneral_body_member(General_body_memberContext ctx) {
    ASTCssNode result = super.visitGeneral_body_member(ctx);
    inheritCommentsFromToken(result, ctx);
    return result;
  }


  private void inheritCommentsFromToken(ASTCssNode node, ParserRuleContext ctx) {
    NodeCommentsHolder comments = treeComments.get(ctx);
    List<Comment> preceding = convertToComments(comments.getPreceding(), ctx);
    node.setOpeningComments(preceding);

    List<Comment> following = convertToComments(comments.getFollowing(), ctx);
    node.setTrailingComments(following);

    List<Comment> orphans = convertToComments(comments.getOrphans(), ctx);
    node.setOrphanComments(orphans);
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
