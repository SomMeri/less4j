package com.github.sommeri.less4j;

import java.util.List;

import com.github.sommeri.less4j.core.ast.BinaryExpression;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;

/**
 * Implement this interface to create custom less function. 
 *
 */
public interface LessFunction {

  /**
   * Returns true if this function can evaluate the less function.    
   * 
   * @param input - function to be evaluated
   * @param parameters - function parameters 
   * 
   * @return <code>true</code> only if the implementation can evaluate the input function.
   */
  public boolean canEvaluate(FunctionExpression input, List<Expression> parameters);

  /**
   * Evaluates less function in parameter. Will be called only if {@link #canEvaluate(FunctionExpression, List)} returns <code>true</code>.
   * 
   * The <code>evaluatedParameter</code> contains function arguments as parsed into a single abstract 
   * syntax tree node. A function called with multiple arguments would be sent an instance of 
   * {@link BinaryExpression} with  arguments bundled in as childs. 
   * 
   * The <code>evaluatedParameter</code> contains list of function arguments. It is convenience argument 
   * and contains <code>evaluatedParameter</code> expression split by commas.
   * 
   * @param input - input function
   * @param parameters - function arguments split (evaluated)
   * @param evaluatedParameter - all function arguments as a single expression (evaluated)  
   * @param problems - errors and warnings collector
   * 
   * @return result of function evaluation. Must NOT return <code>null</code> and should return 
   *     correct abstract syntax tree node. Eg. 
   *     * solved parent child relationships (both ways), 
   *     * each node instance can appear in the three only once,
   *     * correctly filled underlying structure properties.
   */
  public Expression evaluate(FunctionExpression input, List<Expression> parameters, Expression evaluatedParameter, LessProblems problems);

}
