package org.porting.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.porting.less4j.core.parser.LessLexer;

public class RuleSet extends ASTCssNode {

  private List<Selector> selectors = new ArrayList<Selector>();
  private List<Declaration> body = new ArrayList<Declaration>();

  public RuleSet(CommonTree token) {
    super(token);
    assert token!=null && token.getType()==LessLexer.RULESET;
  }
  
  public List<Selector> getSelectors() {
   return selectors;
  }
  
  public List<Declaration> getDeclarations() {
   return body;
  }
  
  public boolean hasEmptyBody() {
    return body.isEmpty();
  }

  public ASTCssNodeType getType() {
    return ASTCssNodeType.RULE_SET;
  }

  public void addSelectors(List<Selector> selectors) {
    this.selectors.addAll(selectors);
  }

  public void addDeclarations(List<Declaration> body) {
    this.body.addAll(body);
  }
}
