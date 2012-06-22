package org.porting.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.porting.less4j.core.parser.LessLexer;

public class RuleSet extends ASTCssNode {

  private List<Selector> selectors = new ArrayList<Selector>();
  private List<Declaration> body;

  public RuleSet(CommonTree token) {
    super(token);
    assert token!=null && token.getType()==LessLexer.RULESET;
  }
  
  public List<Selector> getSelectors() {
   return selectors;
  }
  
  //FIXME:this is going to return all types of childs later
  public List<Declaration> getDeclarations() {
   if (body==null) {
     body =new ArrayList<Declaration>();
     List<CommonTree> children = extractUnderlyingMembers();
     for (CommonTree kid : children) {
      if (kid.getType()==LessLexer.DECLARATION)
        body.add(new Declaration(kid));
    }
   }
   
   return body;
  }
  
  public boolean hasEmptyBody() {
    List<CommonTree> children = extractUnderlyingMembers();
    for (CommonTree commonTree : children) {
      if (commonTree.getType()!=LessLexer.SELECTOR)
        return false;
    }
    
    return true;
  }

  public ASTCssNodeType getType() {
    return ASTCssNodeType.RULE_SET;
  }

  public void addSelectors(List<Selector> selectors) {
    this.selectors.addAll(selectors);
  }
}
