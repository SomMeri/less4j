package com.github.sommeri.less4j.core.compiler.selectors;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.Nth;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.compiler.expressions.FormalisticExpressionComparator;
import com.github.sommeri.less4j.core.problems.BugHappened;

public class GeneralComparatorForExtend {
  
  private FormalisticExpressionComparator formal = new FormalisticExpressionComparator();
  private SelectorsComparatorForExtend selectorsComparator = new SelectorsComparatorForExtend(this);
  private NthComparatorForExtend nthComparator = new NthComparatorForExtend();

  public boolean equals(ASTCssNode node1, ASTCssNode node2) {
    if (node1==null && node2==null)
      return true;
    
    if (node1==null || node2==null)
      return false;

    if (isExpression(node1) && isExpression(node2))
      return formal.equal((Expression) node1, (Expression) node2);

    if (node1.getType()!=node2.getType())
      return false;
    
    switch (node1.getType()) {
    case SELECTOR:
      return selectorsComparator.equals((Selector) node1, (Selector) node2);

    case NTH:
      return nthComparator.equals((Nth) node1, (Nth) node2);
      
    default:
      throw new BugHappened("Need to compare unexpected node types. " + node1 + " with " + node2, node1);
    }
  }

  private boolean isExpression(ASTCssNode node) {
    return (node instanceof Expression);
  }

  public boolean contains(Selector lookFor, Selector inSelector) {
    return selectorsComparator.contains(lookFor, inSelector);
  }

  public boolean replaceInside(Selector lookFor, Selector inSelector, Selector replaceBy) {
    return selectorsComparator.replace(lookFor, inSelector, replaceBy);
  }
  
}
