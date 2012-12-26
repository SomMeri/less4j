package com.github.sommeri.less4j.core.ast;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public class StyleSheet extends Body<ASTCssNode> {
  
  public StyleSheet(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  @Override
  public Set<ASTCssNodeType> getSupportedMembers() {
    HashSet<ASTCssNodeType> result = new HashSet<ASTCssNodeType>(Arrays.asList(ASTCssNodeType.values()));
    // removed only the elements that are likely to end there, this method could be done in more precise way  
    result.remove(ASTCssNodeType.DECLARATION);
    return result;  
  }
  
  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.STYLE_SHEET;
  }

  @Override
  public StyleSheet clone() {
    return (StyleSheet) super.clone();
  }

}
