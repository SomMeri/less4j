package com.github.sommeri.less4j.core.validators;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class CssAstValidator {

  private final ProblemsHandler problemsHandler;
  private final SupportedCSSBodyMembers allowedBodyMembers = new SupportedCSSBodyMembers();

  public CssAstValidator(ProblemsHandler problemsHandler) {
    this.problemsHandler = problemsHandler;
  }

  public void validate(ASTCssNode node) {
    if (node instanceof Body)
      validateBody((Body)node);
    
    List<ASTCssNode> childs = new ArrayList<ASTCssNode>(node.getChilds());
    for (ASTCssNode kid : childs) {
      validate(kid);
    }

  }

  private void validateBody(Body node) {
    Set<ASTCssNodeType> supportedMembers = allowedBodyMembers.getSupportedMembers(node);
    
    for (ASTCssNode member : node.getMembers()) {
      if (!supportedMembers.contains(member.getType())) {
        problemsHandler.wrongMemberInCssBody(member, node);
      }
    }
    
  }

}
