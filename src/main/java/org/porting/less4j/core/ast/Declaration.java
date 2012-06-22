package org.porting.less4j.core.ast;

import java.util.List;

import org.antlr.runtime.tree.CommonTree;
import org.porting.less4j.core.parser.LessLexer;

public class Declaration extends ASTCssNode {

  private String name;
  private Expression expression;
  private boolean priority;

  public Declaration(CommonTree token) {
    super(token);
    // TODO:add assert
    List<CommonTree> members = extractUnderlyingMembers();
    name = extractName(members);
    expression = extractExpression(members);
    priority = extractPriority(members);
  }

  private Expression extractExpression(List<CommonTree> members) {
    if (members!=null && members.size()>1)
      return new Expression(members.get(1));
    
    return null;
  }

  private String extractName(List<CommonTree> members) {
    if (members!=null && members.size()>0)
      return members.get(0).getText();
    
    return null;
  }

  private boolean extractPriority(List<CommonTree> members) {
    if (members!=null && members.size()>2)
      return members.get(2).getType()==LessLexer.IMPORTANT_SYM;
    
    return false;
  }

  public String getName() {
    return name;
  }

  public Expression getExpression() {
    return expression;
  }

  public boolean hasPriority() {
    return priority;
  }
  
  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.DECLARATION;
  }
}
