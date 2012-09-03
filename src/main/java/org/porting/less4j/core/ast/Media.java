package org.porting.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public class Media extends ASTCssNode {
  // TODO: review for specification:
  // http://www.w3.org/TR/2012/REC-css3-mediaqueries-20120619/
//FIXME: make media body extend it general class Body
  //TODO document: less.js keeps whitespaces in media and we are not e.g. less.js 
  //* @media screen , screen => @media screen , screen
  //* @media screen,print => media screen,print
  //We:
  //* @media screen , screen => @media screen, screen
  //* @media screen,print => media screen, print
  //TODO test suite and document: http://www.w3.org/Style/CSS/Test/MediaQueries/20120229/
  private List<ASTCssNode> body = new ArrayList<ASTCssNode>();
  private List<MediaQuery> mediums;

  public Media(HiddenTokenAwareTree token) {
    super(token);
  }

  public List<ASTCssNode> getBody() {
    return body;
  }

  public List<ASTCssNode> getDeclarations() {
    return bodyByType(ASTCssNodeType.DECLARATION);
  }

  public List<ASTCssNode> getRuleSets() {
    return bodyByType(ASTCssNodeType.RULE_SET);
  }

  private List<ASTCssNode> bodyByType(ASTCssNodeType type) {
    List<ASTCssNode> result = new ArrayList<ASTCssNode>();
    List<ASTCssNode> body = getBody();
    for (ASTCssNode node : body) {
      if (node.getType()==type) {
        result.add(node);
      }
    }
    return result;
  }

  public boolean hasEmptyBody() {
    return body.isEmpty();
  }

  public void addDeclaration(Declaration declaration) {
    this.body.add(declaration);
  }

  public void addRuleSet(RuleSet ruleSet) {
    this.body.add(ruleSet);
  }

  public void addChild(ASTCssNode child) {
    assert child.getType() == ASTCssNodeType.RULE_SET || child.getType() == ASTCssNodeType.DECLARATION || child.getType() == ASTCssNodeType.MEDIUM;

    if (child.getType() == ASTCssNodeType.MEDIA_QUERY)
      addMediaQuery((MediaQuery) child);
    else
      this.body.add(child);
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
