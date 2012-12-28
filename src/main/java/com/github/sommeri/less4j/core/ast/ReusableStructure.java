package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class ReusableStructure extends ASTCssNode {

  private List<ElementSubsequent> selectors;
  //Allows: variable, argument declaration, pattern
  private List<ASTCssNode> parameters = new ArrayList<ASTCssNode>();
  private List<Guard> guards = new ArrayList<Guard>();
  private RuleSetsBody body;

  public ReusableStructure(HiddenTokenAwareTree token) {
    super(token);
  }
  
  public ReusableStructure(HiddenTokenAwareTree token, List<ElementSubsequent> selectors) {
    this(token);
    this.selectors=selectors;
  }

  public List<ElementSubsequent> getSelectors() {
   return selectors;
  }
  
  public List<String> getNames() {
    List<String> result = new ArrayList<String>();
    for (ElementSubsequent selector : getSelectors()) {
      result.add(selector.getFullName());
    }
    return result;
   }
  
  public void setSelectors(List<ElementSubsequent> selectors) {
    this.selectors = selectors;
  }

  public RuleSetsBody getBody() {
    return body;
  }

  public boolean hasEmptyBody() {
    return body==null? true : body.isEmpty();
  }

  public void setBody(RuleSetsBody body) {
    this.body = body;
  }

  public List<ASTCssNode> getParameters() {
    return parameters;
  }

  public boolean hasCollectorParameter() {
    if (parameters==null || parameters.isEmpty())
      return false;
    
    ASTCssNode last = parameters.get(parameters.size()-1);
    if (last.getType()!=ASTCssNodeType.ARGUMENT_DECLARATION)
      return false;
    
    return ((ArgumentDeclaration) last).isCollector();
  }

  public List<ASTCssNode> getMandatoryParameters() {
    List<ASTCssNode> result = new ArrayList<ASTCssNode>();
    for (ASTCssNode param : getParameters()) {
      if (param.getType()==ASTCssNodeType.ARGUMENT_DECLARATION) {
        ArgumentDeclaration declaration = (ArgumentDeclaration) param;
        if (declaration.isMandatory())
          result.add(declaration);
      }
    }
    return result;
  }

  public void addParameter(ASTCssNode parameter) {
    if (parameter.getType()!=ASTCssNodeType.ARGUMENT_DECLARATION && !(parameter instanceof Expression))
      throw new IllegalArgumentException("The node can not be used as a mixin parameter: " + parameter);
    
    this.parameters.add(parameter);
  }

  public List<Guard> getGuards() {
    return guards;
  }

  public void addGuard(Guard guard) {
    this.guards.add(guard);
  }

  @Override
  public List<? extends ASTCssNode> getChilds() {
    List<ASTCssNode> result = ArraysUtils.asNonNullList((ASTCssNode)body);
    result.addAll(selectors);
    result.addAll(parameters);
    return result;
  }

  public ASTCssNodeType getType() {
    return ASTCssNodeType.REUSABLE_STRUCTURE;
  }

  @Override
  public ReusableStructure clone() {
    ReusableStructure result = (ReusableStructure) super.clone();
    result.selectors = ArraysUtils.deeplyClonedList(selectors);
    result.parameters = ArraysUtils.deeplyClonedList(parameters);
    result.body = body==null?null:body.clone();
    result.configureParentToAllChilds();
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("ReusableStructure [");
    builder.append(selectors);
    builder.append("]");
    return builder.toString();
  }
  
}
