package com.github.sommeri.less4j;

import com.github.sommeri.less4j.core.ast.ASTCssNode;

/**
 * Collects problems and suspicious things encountered by custom code.
 *
 */
public interface LessProblems {
  
  /**
   * Report an error. If an error is reported, generated css is considered incorrect and will
   * not be generated.  
   * 
   * @param errorNode - ast node that caused the problem. It is used to generate line number and column 
   *     number preceding error description. 
   * @param description - description of encountered problem
   */
  public void addError(ASTCssNode errorNode, String description);

  /**
   * Warn user. Warnings are available to user, but css is generated as usually.
   * 
   * @param weirdNode - ast node that caused the problem. It is used to generate line number and column 
   *     number preceding error description.
   * @param description - description of encountered problem
   */
  public void addWarning(ASTCssNode weirdNode, String description);
  
}