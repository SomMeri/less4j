package com.github.sommeri.less4j.core.validators;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Body;

//this check is that important, the allowed css types check can be
public class SupportedLessBodyMembers {

  private static final Set<ASTCssNodeType> KEYFRAMES_SUPPORTED_MEMBERS = new HashSet<ASTCssNodeType>();

  static {
    KEYFRAMES_SUPPORTED_MEMBERS.add(ASTCssNodeType.RULE_SET);
    KEYFRAMES_SUPPORTED_MEMBERS.add(ASTCssNodeType.MIXIN_REFERENCE);
    KEYFRAMES_SUPPORTED_MEMBERS.add(ASTCssNodeType.REUSABLE_STRUCTURE);
    KEYFRAMES_SUPPORTED_MEMBERS.add(ASTCssNodeType.VARIABLE_DECLARATION);
  }

  public Set<ASTCssNodeType> getSupportedMembers(Body node) {
    if (node.getParent() != null)
      return getSupportedMembers(node.getType(), node.getParent().getType());

    return getSupportedMembers(node.getType(), null);
  }

  public Set<ASTCssNodeType> getSupportedMembers(ASTCssNodeType bodyType, ASTCssNodeType ownerType) {
    //special case - style sheet does not have owner 
    switch (bodyType) {
    case STYLE_SHEET:
      Set<ASTCssNodeType> result = allNodeTypes();
      // removed only the elements that are likely to end there, this method could be done in more precise way  
      result.remove(ASTCssNodeType.DECLARATION);
      result.remove(ASTCssNodeType.PAGE_MARGIN_BOX);
      return result;

    }

    if (ownerType == null)
      return allNodeTypes();

    switch (ownerType) {
    case KEYFRAMES:
      return Collections.unmodifiableSet(KEYFRAMES_SUPPORTED_MEMBERS);
    // turns out that the owner type to be is known very rarely
    // there is no reason to fill this with specifics - 
    // I'm leaving this here so I do not waste time attempting to do the same thing 
    
    default:
      return allNodeTypes();
    }

  }

  private Set<ASTCssNodeType> allNodeTypes() {
    return new HashSet<ASTCssNodeType>(Arrays.asList(ASTCssNodeType.values()));
  }

}
