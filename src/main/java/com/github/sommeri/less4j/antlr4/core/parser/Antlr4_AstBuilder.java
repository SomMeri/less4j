package com.github.sommeri.less4j.antlr4.core.parser;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.CommonToken;
import org.antlr.v4.runtime.tree.ParseTree;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Comment;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.parser.LessG4Lexer;
import com.github.sommeri.less4j.core.parser.LessG4Visitor;
import com.github.sommeri.less4j.core.problems.BugHappened;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.core.validators.LessAstValidator;

public class Antlr4_AstBuilder {

  private final ProblemsHandler problemsHandler;

  public Antlr4_AstBuilder(ProblemsHandler problemsHandler) {
    this.problemsHandler = problemsHandler;
  }

  public StyleSheet parse(ParseTree tree, TreeComments treeComments) {
    LessG4Visitor<ASTCssNode> builder = new CommentsShiftingBuilderSwitch(problemsHandler, treeComments);
    ASTCssNode result = builder.visit(tree);
    //convertComments(result);
    solveParentChildRelationShips(result);
    checkForWarnings(result);
    return (StyleSheet) result;
  }

  private void checkForWarnings(ASTCssNode result) {
    LessAstValidator validator = new LessAstValidator(problemsHandler);
    validator.validate(result);
  }

  private void solveParentChildRelationShips(ASTCssNode node) {
    for (ASTCssNode kid : node.getChilds()) {
      kid.setParent(node);
      solveParentChildRelationShips(kid);
    }
  }

  private void convertComments(ASTCssNode node) {
    inheritCommentsFromToken(node);
    for (ASTCssNode kid : node.getChilds()) {
      if (kid == null)
        throw new BugHappened("Instance of " + node.getClass() + " has null child.", node);
      convertComments(kid);
    }
  }

  private void inheritCommentsFromToken(ASTCssNode node) {
    HiddenTokenAwareTree underlyingStructure = node.getUnderlyingStructure();
    if (underlyingStructure == null)
      return;

    LessSource source = underlyingStructure.getSource();
    List<Comment> preceding = convertToComments(underlyingStructure.getPreceding(), source);
    node.setOpeningComments(preceding);

    List<Comment> following = convertToComments(underlyingStructure.getFollowing(), source);
    node.setTrailingComments(following);

    List<Comment> orphans = convertToComments(underlyingStructure.getOrphans(), source);
    node.setOrphanComments(orphans);
  }

  private List<Comment> convertToComments(List<CommonToken> preceding, LessSource source) {
    List<Comment> result = new ArrayList<Comment>();

    Comment comment = null;
    for (CommonToken token : preceding) {
      if (token.getType() == LessG4Lexer.COMMENT) {
        comment = new Comment(new HiddenTokenAwareTree(token, source), token.getText());
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
