package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class PureMixin extends ASTCssNode {

  private CssClass selector;
  //Allows: variable, argument declaration, pattern
  private List<ASTCssNode> parameters = new ArrayList<ASTCssNode>();
  private List<Guard> guards = new ArrayList<Guard>();
  private RuleSetsBody body;

  public PureMixin(HiddenTokenAwareTree token) {
    super(token);
  }
  
  public PureMixin(HiddenTokenAwareTree token, CssClass className) {
    this(token);
    selector=className;
  }

  public CssClass getSelector() {
   return selector;
  }
  
  public String getName() {
    return getSelector().getName();
   }
  
  public void setSelector(CssClass selector) {
    this.selector = selector;
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

  public List<ASTCssNode> getMandatoryParameters() {
    List<ASTCssNode> result = new ArrayList<ASTCssNode>();
    for (ASTCssNode param : getParameters()) {
      if (param.getType()==ASTCssNodeType.ARGUMENT_DECLARATION) {
        ArgumentDeclaration declaration = (ArgumentDeclaration) param;
        if (!declaration.hasDefaultValue())
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
    List<ASTCssNode> result = ArraysUtils.asNonNullList(selector, body);
    result.addAll(parameters);
    return result;
  }

  public ASTCssNodeType getType() {
    return ASTCssNodeType.PURE_MIXIN;
  }

  @Override
  public PureMixin clone() {
    PureMixin result = (PureMixin) super.clone();
    result.selector = selector==null?null:selector.clone();
    result.parameters = ArraysUtils.deeplyClonedList(parameters);
    result.body = body==null?null:body.clone();
    result.configureParentToAllChilds();
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("PureMixin [");
    builder.append(selector);
    builder.append("]");
    return builder.toString();
  }
  
  
}
