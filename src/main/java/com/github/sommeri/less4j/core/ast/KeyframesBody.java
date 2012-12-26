package com.github.sommeri.less4j.core.ast;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class KeyframesBody extends Body<ASTCssNode> {
  
  private static final Set<ASTCssNodeType> SUPPORTED_MEMBERS = new HashSet<ASTCssNodeType>();
  
  static {
    SUPPORTED_MEMBERS.add(ASTCssNodeType.RULE_SET);
    SUPPORTED_MEMBERS.add(ASTCssNodeType.MIXIN_REFERENCE);
    SUPPORTED_MEMBERS.add(ASTCssNodeType.NAMESPACE_REFERENCE);
    SUPPORTED_MEMBERS.add(ASTCssNodeType.REUSABLE_STRUCTURE);
    SUPPORTED_MEMBERS.add(ASTCssNodeType.VARIABLE_DECLARATION);
  }

  public KeyframesBody(HiddenTokenAwareTree token) {
    super(token);
  }

  public KeyframesBody(HiddenTokenAwareTree underlyingStructure, List<ASTCssNode> declarations) {
    super(underlyingStructure, declarations);
  }

  public List<ASTCssNode> getBody() {
    return super.getBody();
  }
  
  @Override
  public List<ASTCssNode> getChilds() {
    return super.getChilds();
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.KEYFRAMES_BODY;
  }

  public Set<ASTCssNodeType> getSupportedMembers() {
    return Collections.unmodifiableSet(SUPPORTED_MEMBERS);
  }

  @Override
  public KeyframesBody clone() {
    KeyframesBody result = (KeyframesBody) super.clone();
    result.configureParentToAllChilds();
    return result;
  }

}
