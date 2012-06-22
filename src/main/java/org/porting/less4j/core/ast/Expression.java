package org.porting.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import org.antlr.runtime.tree.CommonTree;

public class Expression extends ASTCssNode {

  public Expression(CommonTree token) {
    super(token);
  }

  //FIXME:only temporary method
  public List<ASTCssNode> getChilds() {
    List<ASTCssNode> result = new ArrayList<ASTCssNode>();
    List<CommonTree> members = extractUnderlyingMembers();
    for (CommonTree subTree : members) {
      result.add(new Unknown(subTree));
    }
    return result;
  }

  @Override
  public ASTCssNodeType getType() {
    return ASTCssNodeType.EXPRESSION;
  }
}
