package com.github.sommeri.less4j.core.parser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.antlr.runtime.CommonToken;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Comment;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.ast.VariableDeclaration;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.core.validators.LessAstValidator;

public class ASTBuilder {
  
  private final ProblemsHandler problemsHandler;
  
  public ASTBuilder(ProblemsHandler problemsHandler) {
    super();
    this.problemsHandler = problemsHandler;
  }

  public StyleSheet parseStyleSheet(HiddenTokenAwareTree tree) {
    ASTCssNode result = parseAnything(tree);
    return (StyleSheet) result;
  }

  public VariableDeclaration parseVariable(String name, HiddenTokenAwareTree valueTree) {
    Expression value = (Expression) parseAnything(valueTree);
    
    Variable variable = new Variable(value.getUnderlyingStructure(), name);
    VariableDeclaration result = new VariableDeclaration(value.getUnderlyingStructure(), variable, value);
    result.configureParentToAllChilds();
    return result;
  }

  public List<VariableDeclaration> parseVariables(Map<String, HiddenTokenAwareTree> variables) {
    List<VariableDeclaration> result = new ArrayList<VariableDeclaration>();
    for (Entry<String, HiddenTokenAwareTree> entry : variables.entrySet()) {
      result.add(parseVariable(entry.getKey(), entry.getValue()));
    }
    return result;
  }

  public ASTCssNode parseAnything(HiddenTokenAwareTree tree) {
    ASTBuilderSwitch builder = new ASTBuilderSwitch(problemsHandler);
    ASTCssNode result = builder.switchOn(tree);
    convertComments(result);
    solveParentChildRelationShips(result);
    checkForWarnings(result);
    return result;
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
  
  private List<Comment> convertToComments(List<CommonToken> preceding, LessSource source) {
    List<Comment> result = new ArrayList<Comment>();

    Comment comment = null;
    for (CommonToken token : preceding) {
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

