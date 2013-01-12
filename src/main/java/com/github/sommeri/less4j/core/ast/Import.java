package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class Import extends ASTCssNode {
  
  private ImportKind kind = ImportKind.IMPORT;
  private Expression urlExpression;
  private List<MediaQuery> mediums = new ArrayList<MediaQuery>();

  public Import(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public ImportKind getKind() {
    return kind;
  }

  public void setKind(ImportKind kind) {
    this.kind = kind;
  }

  public Expression getUrlExpression() {
    return urlExpression;
  }

  public void setUrlExpression(Expression urlExpression) {
    this.urlExpression = urlExpression;
  }

  public List<MediaQuery> getMediums() {
    return mediums;
  }

  public void setMediums(List<MediaQuery> mediums) {
    this.mediums = mediums;
  }

  public void add(MediaQuery medium) {
    mediums.add(medium);
  }

  public boolean hasMediums() {
    return !mediums.isEmpty();
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    List<ASTCssNode> result = ArraysUtils.asNonNullList((ASTCssNode)urlExpression);
    result.addAll(mediums);
    return result;
  }

  @Override
  public Import clone() {
    Import result = (Import) super.clone();
    result.urlExpression = urlExpression==null? null : urlExpression.clone();
    result.mediums = ArraysUtils.deeplyClonedList(mediums);
    result.configureParentToAllChilds();
    return result;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.IMPORT;
  }
  
  public enum ImportKind {
    IMPORT, IMPORT_ONCE, IMPORT_MULTIPLE
  }

}
