package org.porting.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;
import org.porting.less4j.core.parser.LessLexer;

public class RuleSet extends ASTCssNode {

  private List<Selector> selectors = new ArrayList<Selector>();
  private DeclarationsBody body;

  public RuleSet(HiddenTokenAwareTree token) {
    super(token);
    assert token!=null && token.getType()==LessLexer.RULESET;
  }
  
  public List<Selector> getSelectors() {
   return selectors;
  }
  
  public DeclarationsBody getBody() {
    return body;
  }

  public boolean hasEmptyBody() {
    return body==null? true : body.isEmpty();
  }

  public void setBody(DeclarationsBody body) {
    this.body = body;
  }

  public ASTCssNodeType getType() {
    return ASTCssNodeType.RULE_SET;
  }

  public void addSelectors(List<Selector> selectors) {
    this.selectors.addAll(selectors);
  }

}
