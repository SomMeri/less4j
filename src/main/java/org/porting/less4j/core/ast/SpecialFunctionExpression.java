package org.porting.less4j.core.ast;

import org.antlr.runtime.tree.CommonTree;

/**
 * URIs and other functions that require special handling. 
 *
 */
public abstract class SpecialFunctionExpression extends Expression {

  public SpecialFunctionExpression(CommonTree underlyingStructure) {
    super(underlyingStructure);
  }

}
