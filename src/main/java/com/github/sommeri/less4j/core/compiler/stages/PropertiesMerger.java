package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.ast.Declaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.ListExpression;
import com.github.sommeri.less4j.core.ast.ListExpressionOperator;
import com.github.sommeri.less4j.core.ast.ListExpressionOperator.Operator;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionManipulator;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

/**
 * Preconditions: 
 * 1.) properties names must be interpolated,
 * 2.) rulesets bodies must be fully solved.   
 *
 */
public class PropertiesMerger extends TreeDeclarationsVisitor {

  private Map<String, MergedData> mergingProperties = new HashMap<String, MergedData>();
  private ASTManipulator manipulator = new ASTManipulator();
  private ExpressionManipulator expressionManipulator = new ExpressionManipulator();

  public PropertiesMerger() {
  }


  protected void applyToDeclaration(Declaration declaration) {
    if (declaration.isMerging())
      addToPrevious(declaration);
  }

  private void addToPrevious(Declaration declaration) {
    if (declaration.getExpression() == null)
      return;

    String key = toMergingPropertiesKey(declaration);
    if (mergingProperties.containsKey(key)) {
      MergedData mergedDeclaration = mergingProperties.get(key);
      addToMergeData(mergedDeclaration, declaration.getExpression(), declaration.getMergeOperator());
      manipulator.removeFromBody(declaration);
    } else {
      MergedData mergedData = createMergeData(declaration);
      mergingProperties.put(key, mergedData);
    }
  }

  private MergedData createMergeData(Declaration declaration) {
    Expression expression = declaration.getExpression();
    Expression important = null;
    if (expressionManipulator.isImportant(expression)) {
      important = expressionManipulator.cutRightmostListedExpression(expression);
    }
    MergedData mergedData = new MergedData(declaration, important);
    mergedData.add(expression);
    return mergedData;
  }

  private void addToMergeData(MergedData mergeData, Expression expression, Operator merge) {
    if (mergeData.isImportant()) {
      expressionManipulator.cutRightmostListedExpression(expression);
    }
    if (mergeData.getOperator() != merge) {
      Expression mergeExpressions = mergeData.mergeExpressions();
      mergeData.setOperator(merge);
      mergeData.replaceExpressions(mergeExpressions);

    }
    mergeData.add(expression);
  }

  private String toMergingPropertiesKey(Declaration declaration) {
    String cssPropertyName = declaration.getNameAsString();
    boolean important = expressionManipulator.isImportant(declaration.getExpression());
    return cssPropertyName + " " + important;
  }

  protected void enteringBody(Body node) {
    mergingProperties = new HashMap<String, MergedData>();
  }

  protected void leavingBody(Body node) {
    for (MergedData data : mergingProperties.values()) {
      Declaration declaration = data.getDeclaration();
      HiddenTokenAwareTree underlying = declaration.getUnderlyingStructure();

      Expression mergedExpressions = data.mergeExpressions();
      if (data.isImportant()) {
        ListExpression list = bundleInSpaceSeparatedList(underlying, mergedExpressions);
        list.addExpression(data.getImportance());
        list.configureParentToAllChilds();
        mergedExpressions = list;
      }

      declaration.setExpression(mergedExpressions);
      mergedExpressions.setParent(declaration);
    }
  }

  private ListExpression bundleInSpaceSeparatedList(HiddenTokenAwareTree underlying, Expression mergedExpressions) {
    if (expressionManipulator.isSpaceSeparatedList(mergedExpressions))
      return (ListExpression) mergedExpressions;

    List<Expression> iExpressions = ArraysUtils.asList(mergedExpressions);
    ListExpressionOperator space = new ListExpressionOperator(underlying, ListExpressionOperator.Operator.EMPTY_OPERATOR);
    return new ListExpression(underlying, iExpressions, space);
  }
}

class MergedData {

  private final Declaration declaration;
  private final Expression importance;
  private Operator operator;
  private List<Expression> mergedExpressions = new ArrayList<Expression>();

  public MergedData(Declaration declaration, Expression importance) {
    super();
    this.declaration = declaration;
    this.importance = importance;
    this.operator = declaration.getMergeOperator();
  }

  public void replaceExpressions(Expression expression) {
    mergedExpressions = new ArrayList<Expression>();
    mergedExpressions.add(expression);
  }

  public Expression mergeExpressions() {
    HiddenTokenAwareTree underlying = declaration.getUnderlyingStructure();
    List<Expression> expressions = getMergedExpressions();
    if (expressions.size() == 1)
      return expressions.get(0);

    ListExpressionOperator.Operator mergeOperator = getOperator();
    ListExpression result = new ListExpression(underlying, expressions, new ListExpressionOperator(underlying, mergeOperator));
    result.configureParentToAllChilds();
    return result;
  }

  public boolean isImportant() {
    return importance != null;
  }

  public void add(Expression expression) {
    mergedExpressions.add(expression);
  }

  public Declaration getDeclaration() {
    return declaration;
  }

  public Expression getImportance() {
    return importance;
  }

  public Operator getOperator() {
    return operator;
  }

  public void setOperator(Operator operator) {
    this.operator = operator;
  }

  public List<Expression> getMergedExpressions() {
    return mergedExpressions;
  }

}
