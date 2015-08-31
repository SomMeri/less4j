package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class Import extends ASTCssNode {

  private ImportMultiplicity multiplicity = ImportMultiplicity.IMPORT;
  private ImportContent contentKind = ImportContent.SUFFIX_BASED;
  private boolean isInline = false;
  private boolean isReferenceOnly = false;
  private boolean isOptional = false;
  private Expression urlExpression;
  private List<MediaQuery> mediums = new ArrayList<MediaQuery>();

  public Import(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public boolean isReferenceOnly() {
    return isReferenceOnly;
  }

  public void setReferenceOnly(boolean isReferenceOnly) {
    this.isReferenceOnly = isReferenceOnly;
  }

  public boolean isInline() {
    return isInline;
  }

  public void setInline(boolean isInline) {
    this.isInline = isInline;
  }

  public boolean isOptional() {
    return isOptional;
  }

  public void setOptional(boolean isOptional) {
    this.isOptional = isOptional;
  }

  public ImportContent getContentKind() {
    return contentKind;
  }

  public void setContentKind(ImportContent contentKind) {
    this.contentKind = contentKind;
  }

  public ImportMultiplicity getMultiplicity() {
    return multiplicity;
  }

  public void setMultiplicity(ImportMultiplicity multiplicity) {
    this.multiplicity = multiplicity;
  }

  public boolean isImportOnce() {
    ImportMultiplicity multiplicity = getMultiplicity();
    return multiplicity == ImportMultiplicity.IMPORT || multiplicity == ImportMultiplicity.IMPORT_ONCE;
  }

  public boolean isImportMultiple() {
    return !isImportOnce();
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
  @NotAstProperty
  public List<? extends ASTCssNode> getChilds() {
    List<ASTCssNode> result = ArraysUtils.asNonNullList((ASTCssNode) urlExpression);
    result.addAll(mediums);
    return result;
  }

  @Override
  public Import clone() {
    Import result = (Import) super.clone();
    result.urlExpression = urlExpression == null ? null : urlExpression.clone();
    result.mediums = ArraysUtils.deeplyClonedList(mediums);
    result.configureParentToAllChilds();
    return result;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.IMPORT;
  }

  public enum ImportMultiplicity {
    IMPORT, IMPORT_ONCE, IMPORT_MULTIPLE
  }

  public enum ImportContent {
    LESS, CSS, SUFFIX_BASED
  }

}
