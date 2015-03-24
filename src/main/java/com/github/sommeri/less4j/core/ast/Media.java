package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class Media extends Directive {

  private List<MediaQuery> mediums;
  private GeneralBody body;

  public Media(HiddenTokenAwareTree token) {
    super(token);
  }

  public void addMediaQuery(MediaQuery medium) {
    if (mediums==null) {
      mediums = new ArrayList<MediaQuery>();
    }
    mediums.add(medium);
  }

  public List<MediaQuery> getMediums() {
    return mediums;
  }

  public void setMediums(List<MediaQuery> mediums) {
    this.mediums = mediums;
  }

  public void replaceMediaQueries(List<MediaQuery> result) {
    for (MediaQuery oldMediums : mediums) {
      oldMediums.setParent(null);
    }
    mediums = new ArrayList<MediaQuery>();
    mediums.addAll(result);
  }

  public boolean bubleUpWithoutChanges() {
    return false;
  }

  @Override
  @NotAstProperty
  public List<ASTCssNode> getChilds() {
    List<ASTCssNode> childs = new ArrayList<ASTCssNode>();
    childs.addAll(mediums);
    childs.add(body);
    return childs;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.MEDIA;
  }

  @Override
  public Media clone() {
    Media result = (Media) super.clone();
    result.mediums = ArraysUtils.deeplyClonedList(mediums);
    result.body = body==null?null:body.clone();
    result.configureParentToAllChilds();
    return result;
  }

  @Override
  public void setBody(GeneralBody body) {
    this.body = body;
  }

  @Override
  public GeneralBody getBody() {
    return body;
  }

}
