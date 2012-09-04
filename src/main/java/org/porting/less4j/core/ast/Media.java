package org.porting.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public class Media extends Body<ASTCssNode> {
  //TODO document: less.js keeps whitespaces in media and we are not e.g. less.js 
  //* @media screen , screen => @media screen , screen
  //* @media screen,print => media screen,print
  //We:
  //* @media screen , screen => @media screen, screen
  //* @media screen,print => media screen, print
  private List<MediaQuery> mediums;

  public Media(HiddenTokenAwareTree token) {
    super(token);
  }

  public List<ASTCssNode> getDeclarations() {
    return membersByType(ASTCssNodeType.DECLARATION);
  }

  public List<ASTCssNode> getRuleSets() {
    return membersByType(ASTCssNodeType.RULE_SET);
  }

  public void addDeclaration(Declaration declaration) {
    addMember(declaration);
  }

  public void addRuleSet(RuleSet ruleSet) {
    addMember(ruleSet);
  }

  public void addChild(ASTCssNode child) {
    assert child.getType() == ASTCssNodeType.RULE_SET || child.getType() == ASTCssNodeType.DECLARATION || child.getType() == ASTCssNodeType.MEDIUM;

    if (child.getType() == ASTCssNodeType.MEDIA_QUERY)
      addMediaQuery((MediaQuery) child);
    else
      addMember(child);
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

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.MEDIA;
  }

}
