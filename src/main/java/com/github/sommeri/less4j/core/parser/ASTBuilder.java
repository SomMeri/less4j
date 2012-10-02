package com.github.sommeri.less4j.core.parser;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.Token;

import com.github.sommeri.less4j.core.parser.LessLexer;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Comment;
import com.github.sommeri.less4j.core.ast.StyleSheet;

public class ASTBuilder {

  public StyleSheet parse(HiddenTokenAwareTree tree) {
    ASTBuilderSwitch builder = new ASTBuilderSwitch();
    ASTCssNode result = builder.switchOn(tree);
    convertComments(result);
    solveParentChildRelationShips(result);
    return (StyleSheet) result;
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
    
    List<Comment> preceding = convertToComments(underlyingStructure.getPreceding());
    node.setOpeningComments(preceding);

    List<Comment> following = convertToComments(underlyingStructure.getFollowing());
    node.setTrailingComments(following);

    List<Comment> orphans = convertToComments(underlyingStructure.getOrphans());
    node.setOrphanComments(orphans);
  }
  
  private List<Comment> convertToComments(List<Token> preceding) {
    List<Comment> result = new ArrayList<Comment>();

    Comment comment = null;
    for (Token token : preceding) {
      if (token.getType() == LessLexer.COMMENT) {
        comment = new Comment(new HiddenTokenAwareTree(token));
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

