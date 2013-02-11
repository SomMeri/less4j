package com.github.sommeri.less4j.core.validators;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Body;

//this check is that important, the allowed css types check can be
public class SupportedCSSBodyMembers {

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
    case VIEWPORT:
      return createSet(ASTCssNodeType.DECLARATION);

    case RULE_SET:
      return createSet(ASTCssNodeType.DECLARATION);

    case PAGE:
      return createSet(ASTCssNodeType.DECLARATION, ASTCssNodeType.PAGE_MARGIN_BOX);

    case PAGE_MARGIN_BOX:
      return createSet(ASTCssNodeType.DECLARATION);

    case MEDIA:
      return createSet(ASTCssNodeType.RULE_SET);

    case KEYFRAMES:
      return createSet(ASTCssNodeType.RULE_SET);

    default:
      return allNodeTypes();
    }

  }

  private Set<ASTCssNodeType> createSet(ASTCssNodeType... types) {
    return new HashSet<ASTCssNodeType>(Arrays.asList(types));
  }

  private Set<ASTCssNodeType> allNodeTypes() {
    return new HashSet<ASTCssNodeType>(Arrays.asList(ASTCssNodeType.values()));
  }

}
