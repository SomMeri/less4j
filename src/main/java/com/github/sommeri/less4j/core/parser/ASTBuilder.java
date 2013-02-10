package com.github.sommeri.less4j.core.parser;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.Token;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Comment;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.core.validators.LessAstValidator;

public class ASTBuilder {
  
  private final ProblemsHandler problemsHandler;
  
  public ASTBuilder(ProblemsHandler problemsHandler) {
    super();
    this.problemsHandler = problemsHandler;
  }

  public StyleSheet parse(HiddenTokenAwareTree tree) {
    ASTBuilderSwitch builder = new ASTBuilderSwitch(problemsHandler);
    ASTCssNode result = builder.switchOn(tree);
    convertComments(result);
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
      convertComments(kid);
    }
  }

  private void inheritCommentsFromToken(ASTCssNode node) {
    HiddenTokenAwareTree underlyingStructure = node.getUnderlyingStructure();
    if (underlyingStructure==null)
      return ;
    
    LessSource source = underlyingStructure.getSource();
    List<Comment> preceding = convertToComments(underlyingStructure.getPreceding(), source);
    node.setOpeningComments(preceding);

    List<Comment> following = convertToComments(underlyingStructure.getFollowing(), source);
    node.setTrailingComments(following);

    List<Comment> orphans = convertToComments(underlyingStructure.getOrphans(), source);
    node.setOrphanComments(orphans);
  }
  
  private List<Comment> convertToComments(List<Token> preceding, LessSource source) {
    List<Comment> result = new ArrayList<Comment>();

    Comment comment = null;
    for (Token token : preceding) {
      if (token.getType() == LessLexer.COMMENT) {
        comment = new Comment(new HiddenTokenAwareTree(token, source));
        result.add(comment);
      }
      if (token.getType() == LessLexer.NEW_LINE) {
        if (comment != null)
          comment.setHasNewLine(true);
      }
    }

    return result;
  }

}

