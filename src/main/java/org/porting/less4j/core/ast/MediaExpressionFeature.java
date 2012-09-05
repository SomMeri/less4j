package org.porting.less4j.core.ast;

import java.util.Collections;
import java.util.List;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

//TODO all those one string nodes should really be a one node named NAME or something similar. There are way too many classes by now. This is partially result of comments handling system, but there is no reason to go that far.
public class MediaExpressionFeature extends ASTCssNode {

  private String feature;

  public MediaExpressionFeature(HiddenTokenAwareTree underlyingStructure, String feature) {
    super(underlyingStructure);
    this.feature = feature;
  }

  public String getFeature() {
    return feature;
  }

  public void setFeature(String feature) {
    this.feature = feature;
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    return Collections.emptyList();
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.MEDIUM_EX_FEATURE;
  }

}
