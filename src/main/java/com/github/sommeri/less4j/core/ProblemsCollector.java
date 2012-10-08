package com.github.sommeri.less4j.core;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.LessCompiler.IProblem;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.RuleSet;

public class ProblemsCollector {

  private List<IProblem> warnings = new ArrayList<IProblem>();

  public ProblemsCollector() {
  }

  public void clean() {
    warnings = new ArrayList<IProblem>();
  }

  public void warnRuleSetWithoutSelector(RuleSet ruleSet) {
    //input file is not a valid less and output file is not a valid css
    warnings.add(new AstNodeWarning(ruleSet, "Ruleset without selector: "));
  }

  public List<IProblem> getWarnings() {
    return warnings;
  }

}

class AstNodeWarning implements IProblem {

  private final ASTCssNode node;
  private final String message;

  public AstNodeWarning(ASTCssNode node, String message) {
    super();
    this.node = node;
    this.message = message;
  }

  public ASTCssNode getNode() {
    return node;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public int getLine() {
    return node.getSourceLine();
  }

  @Override
  public int getCharacter() {
    return node.getCharPositionInSourceLine();
  }

}
