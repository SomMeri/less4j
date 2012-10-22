package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class NamespaceReference extends ASTCssNode {
  
  private List<String> nameChain = new ArrayList<String>();
  private MixinReference finalReference;

  public NamespaceReference(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public List<String> getNameChain() {
    return nameChain;
  }

  public void setNameChain(List<String> nameChain) {
    this.nameChain = nameChain;
  }

  public MixinReference getFinalReference() {
    return finalReference;
  }

  public void setFinalReference(MixinReference finalReference) {
    this.finalReference = finalReference;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return ArraysUtils.asNonNullList(finalReference);
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.NAMESPACE_REFERENCE;
  }

  @Override
  public NamespaceReference clone() {
    NamespaceReference result = (NamespaceReference) super.clone();
    result.nameChain = nameChain==null?null:new ArrayList<String>(nameChain);
    result.finalReference = finalReference==null?null:finalReference.clone();
    return result;
  }

  public void addName(String text) {
    nameChain.add(text);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (String str : nameChain) {
      builder.append(str).append(" > ");
    }
    builder.append(getFinalReference().toString());
    return builder.toString();
  }
  
  
}
