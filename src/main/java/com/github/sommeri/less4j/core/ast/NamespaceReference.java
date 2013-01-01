package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class NamespaceReference extends ASTCssNode {

  private List<ReusableStructureName> nameChain = new ArrayList<ReusableStructureName>();
  private MixinReference finalReference;

  public NamespaceReference(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public List<ReusableStructureName> getNameChain() {
    return nameChain;
  }

  public boolean hasInterpolatedName() {
    for (ReusableStructureName name : nameChain) {
      if (name.isInterpolated())
        return true;
    }
    return false;
  }

  public List<String> getNameChainAsStrings() {
    List<String> result = new ArrayList<String>();
    for (ReusableStructureName name : nameChain) {
      result.add(name.asString());
    }
    return result;
  }

  public void setNameChain(List<ReusableStructureName> nameChain) {
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
    List<ASTCssNode> result = ArraysUtils.asNonNullList((ASTCssNode) finalReference);
    result.addAll(nameChain);
    return result;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.NAMESPACE_REFERENCE;
  }

  @Override
  public NamespaceReference clone() {
    NamespaceReference result = (NamespaceReference) super.clone();
    result.nameChain = nameChain == null ? null : new ArrayList<ReusableStructureName>(nameChain);
    result.finalReference = finalReference == null ? null : finalReference.clone();
    return result;
  }

  public void addName(ReusableStructureName text) {
    nameChain.add(text);
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    for (ReusableStructureName str : nameChain) {
      builder.append(str.asString()).append(" > ");
    }
    builder.append(getFinalReference().toString());
    return builder.toString();
  }

}
