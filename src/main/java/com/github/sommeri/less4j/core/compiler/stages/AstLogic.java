package com.github.sommeri.less4j.core.compiler.stages;

import java.util.List;
import java.util.Set;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.problems.BugHappened;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.core.validators.SupportedLessBodyMembers;

public class AstLogic {

  public static boolean hasOwnScope(ASTCssNode node) {
    return (node instanceof Body);
  }
  
  public static void validateLessBodyCompatibility(ASTCssNode reference, List<ASTCssNode> members, ProblemsHandler problemsHandler) {
    ASTCssNode parent = reference.getParent();
    if (!(parent instanceof Body)) {
      throw new BugHappened("Parent is not a body instance. " + parent, parent);
    }
    
    SupportedLessBodyMembers allowedBodyMembers = new SupportedLessBodyMembers();
    Set<ASTCssNodeType> supportedMembers = allowedBodyMembers.getSupportedMembers((Body) parent);
    
    for (ASTCssNode member : members) {
      if (!supportedMembers.contains(member.getType())) {
        problemsHandler.wrongMemberBroughtIntoBody(reference, member, parent);
      }
    }
    
  }
  
}
