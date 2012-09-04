package org.porting.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public class MediaQuery extends ASTCssNode {

  private Medium medium;
  private List<MediaExpression> expressions;

  public MediaQuery(HiddenTokenAwareTree token) {
    this(token, null, new ArrayList<MediaExpression>());
  }

  public MediaQuery(HiddenTokenAwareTree token, Medium medium, List<MediaExpression> expressions) {
    super(token);
    this.medium = medium;
    this.expressions = expressions;
  }

  public Medium getMedium() {
    return medium;
  }

  public void setMedium(Medium medium) {
    this.medium = medium;
  }

  public List<MediaExpression> getExpressions() {
    return expressions;
  }

  public void setExpressions(List<MediaExpression> expressions) {
    this.expressions = expressions;
  }

  public void addExpression(MediaExpression expression) {
    if (expressions == null)
      expressions = new ArrayList<MediaExpression>();

    this.expressions.add(expression);
  }

  /**
   * May throw class cast exception if the member in parameter is 
   * does not have the right type. 
   */
  public void addMember(ASTCssNode member) {
    if (member.getType() == ASTCssNodeType.MEDIUM) {
      setMedium((Medium) member);
    } else {
      addExpression((MediaExpression) member);
    }
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.MEDIA_QUERY;
  }

}
