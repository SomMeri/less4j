package com.github.sommeri.less4j.core.compiler.selectors;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.Nth;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.compiler.expressions.PatternsComparator;

/**
 *
 * FIXME: (!!!) extend should not be among candidates!!!!! 
 * EXTEND
 * SELECTOR
 * NTH

 * IDENTIFIER_EXPRESSION
 * VARIABLE
 * NUMBER
 * STRING_EXPRESSION
 *
 */
public class GeneralComparator {
  
  private PatternsComparator simpleExpressionComparator = new PatternsComparator();
  private SelectorsComparator selectorsComparator = new SelectorsComparator(this);
  private NthComparator nthComparator = new NthComparator();

  public boolean equals(ASTCssNode node1, ASTCssNode node2) {
    if (node1==null && node2==null)
      return true;
    
    if (node1==null || node2==null)
      return false;

    if (isExpression(node1) && isExpression(node2))
      return simpleExpressionComparator.equal((Expression) node1, (Expression) node2);

    if (node1.getType()!=node2.getType())
      return false;
    
    switch (node1.getType()) {
    case SELECTOR:
      return selectorsComparator.equals((Selector) node1, (Selector) node2);

    case NTH:
      return nthComparator.equals((Nth) node1, (Nth) node2);
    default:
      //FIXME (!!!) report error
      break;
    }
    return false;
  }

  private boolean isExpression(ASTCssNode node) {
    return (node instanceof Expression);
  }
  
}
