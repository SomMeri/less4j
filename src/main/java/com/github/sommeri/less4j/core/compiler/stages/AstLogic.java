package com.github.sommeri.less4j.core.compiler.stages;

import java.util.List;
import java.util.Set;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.BodyOwner;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.SimpleSelector;
import com.github.sommeri.less4j.core.ast.SelectorCombinator.Combinator;
import com.github.sommeri.less4j.core.problems.BugHappened;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.core.validators.SupportedLessBodyMembers;

public class AstLogic {

  public static boolean hasOwnScope(ASTCssNode node) {
    return isBody(node) || isBodyOwner(node);
  }

  public static boolean isBodyOwner(ASTCssNode node) {
    return node instanceof BodyOwner;
  }

  protected static boolean isBody(ASTCssNode node) {
    return node instanceof Body;
  }
  
  public static boolean canHaveArguments(ASTCssNode owner) {
    if (owner.getType()!=ASTCssNodeType.REUSABLE_STRUCTURE) 
      return false;
    
    return ((ReusableStructure)owner).hasParameters();
  }
  
  public static void validateLessBodyCompatibility(ASTCssNode reference, List<ASTCssNode> members, ProblemsHandler problemsHandler) {
    ASTCssNode parent = reference.getParent();
    if (!isBody(parent)) {
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

  public static boolean hasNonSpaceCombinator(SimpleSelector selector) {
    return selector.hasLeadingCombinator() && selector.getLeadingCombinator().getCombinator()!=Combinator.DESCENDANT;
  }

}
