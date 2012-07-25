package org.porting.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public class Media extends ASTCssNode {
  // FIXME: grrr http://webdesignerwall.com/tutorials/css3-media-queries
  // FIXME: grrr http://www.w3schools.com/css/css_mediatypes.asp
  // FIXME: add tests from this:
  // http://webdesignerwall.com/tutorials/css3-media-queries
  // TODO: review for specification:
  // http://www.w3.org/TR/2012/REC-css3-mediaqueries-20120619/
//FIXME: make media body extend it general class Body
  private List<ASTCssNode> body = new ArrayList<ASTCssNode>();
  private Medium medium;

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

    if (child.getType() == ASTCssNodeType.MEDIUM)
      setMedium((Medium) child);
    else
      this.body.add(child);
  }

  public Medium getMedium() {
    return medium;
  }

  public void setMedium(Medium medium) {
    this.medium = medium;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.MEDIA;
  }

}
