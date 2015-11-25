package com.github.sommeri.less4j.core.validators;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Body;

//this check is that important, the allowed css types check can be
public class SupportedLessBodyMembers {

  private static final EnumSet<ASTCssNodeType> KEYFRAMES_SUPPORTED_MEMBERS;
  private static final EnumSet<ASTCssNodeType> ALL_NODE_TYPES;
  private static final EnumSet<ASTCssNodeType> STYLE_SHEET_SUPPORTED_MEMBERS;

  static {
    KEYFRAMES_SUPPORTED_MEMBERS = EnumSet.of(ASTCssNodeType.RULE_SET, ASTCssNodeType.MIXIN_REFERENCE, ASTCssNodeType.REUSABLE_STRUCTURE, ASTCssNodeType.VARIABLE_DECLARATION);

    ALL_NODE_TYPES = EnumSet.allOf(ASTCssNodeType.class);

    STYLE_SHEET_SUPPORTED_MEMBERS = EnumSet.allOf(ASTCssNodeType.class);
    STYLE_SHEET_SUPPORTED_MEMBERS.remove(ASTCssNodeType.DECLARATION);
    STYLE_SHEET_SUPPORTED_MEMBERS.remove(ASTCssNodeType.PAGE_MARGIN_BOX);
  }

  public Set<ASTCssNodeType> getSupportedMembers(Body node) {
    if (node.getParent() != null)
      return getSupportedMembers(node.getType(), node.getParent().getType());

    return getSupportedMembers(node.getType(), null);
  }

  public Set<ASTCssNodeType> getSupportedMembers(ASTCssNodeType bodyType, ASTCssNodeType ownerType) {
    // special case - style sheet does not have owner
    switch (bodyType) {
    case STYLE_SHEET:
      return Collections.unmodifiableSet(STYLE_SHEET_SUPPORTED_MEMBERS);

    default:
      // nothing is needed
    }

    if (ownerType == null)
      return allNodeTypes();

    switch (ownerType) {
    case KEYFRAMES:
      return Collections.unmodifiableSet(KEYFRAMES_SUPPORTED_MEMBERS);
    // turns out that the owner type to be is known very rarely
    // there is no reason to fill this with specifics -
    // I'm leaving this here so I do not waste time attempting to do the same
    // thing

    default:
      return allNodeTypes();
    }

  }

  private Set<ASTCssNodeType> allNodeTypes() {
    return Collections.unmodifiableSet(ALL_NODE_TYPES);
  }

}
