package com.github.sommeri.less4j.core.parser;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.Token;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Comment;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

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
    
    File inputFile = underlyingStructure.getInputFile();
    List<Comment> preceding = convertToComments(underlyingStructure.getPreceding(), inputFile);
    node.setOpeningComments(preceding);

    List<Comment> following = convertToComments(underlyingStructure.getFollowing(), inputFile);
    node.setTrailingComments(following);

    List<Comment> orphans = convertToComments(underlyingStructure.getOrphans(), inputFile);
    node.setOrphanComments(orphans);
  }
  
  private List<Comment> convertToComments(List<Token> preceding, File inputFile) {
    List<Comment> result = new ArrayList<Comment>();

    Comment comment = null;
    for (Token token : preceding) {
      if (token.getType() == LessLexer.COMMENT) {
        comment = new Comment(new HiddenTokenAwareTree(token, inputFile));
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

