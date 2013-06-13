package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class ReusableStructure extends ASTCssNode implements BodyOwner<GeneralBody> {

  private List<ReusableStructureName> names = new ArrayList<ReusableStructureName>();
  //Allows: variable, argument declaration, pattern
  private List<ASTCssNode> parameters = new ArrayList<ASTCssNode>();
  private List<Guard> guards = new ArrayList<Guard>();
  private GeneralBody body;

  public ReusableStructure(HiddenTokenAwareTree token) {
    super(token);
  }
  
  public ReusableStructure(HiddenTokenAwareTree token, List<ReusableStructureName> names) {
    this(token);
    this.names=names;
  }

  public void addName(ReusableStructureName name) {
    names.add(name);
  }
  
  public List<ReusableStructureName> getNames() {
   return names;
  }
  
  public List<String> getNamesAsStrings() {
    List<String> result = new ArrayList<String>();
    for (ReusableStructureName name : getNames()) {
      result.add(name.asString());
    }
    return result;
   }
  
  public boolean hasName(String name) {
    return getNamesAsStrings().contains(name);
  }

  public void setNames(List<ReusableStructureName> names) {
    this.names = names;
  }

  public GeneralBody getBody() {
    return body;
  }

  public boolean hasEmptyBody() {
    return body==null? true : body.isEmpty();
  }

  public void setBody(GeneralBody body) {
    this.body = body;
  }

  public List<ASTCssNode> getParameters() {
    return parameters;
  }

  public boolean hasParameters() {
    return !getParameters().isEmpty();
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
    result.addAll(names);
    result.addAll(parameters);
    return result;
  }

  public ASTCssNodeType getType() {
    return ASTCssNodeType.REUSABLE_STRUCTURE;
  }

  @Override
  public ReusableStructure clone() {
    ReusableStructure result = (ReusableStructure) super.clone();
    result.names = ArraysUtils.deeplyClonedList(names);
    result.parameters = ArraysUtils.deeplyClonedList(parameters);
    result.body = body==null?null:body.clone();
    result.configureParentToAllChilds();
    return result;
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append("ReusableStructure [");
    builder.append(names);
    builder.append("]");
    return builder.toString();
  }

}
