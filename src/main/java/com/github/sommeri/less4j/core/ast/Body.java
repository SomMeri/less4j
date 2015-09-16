package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public abstract class Body extends ASTCssNode {

  private SyntaxOnlyElement openingCurlyBrace;
  private SyntaxOnlyElement closingCurlyBrace;

  private List<ASTCssNode> body = new ArrayList<ASTCssNode>();

  public Body(HiddenTokenAwareTree underlyingStructure) {
    super(underlyingStructure);
  }

  public Body(HiddenTokenAwareTree underlyingStructure, SyntaxOnlyElement lbrace, SyntaxOnlyElement rbrace, List<ASTCssNode> members) {
    this(underlyingStructure);
    openingCurlyBrace = lbrace;
    closingCurlyBrace = rbrace;
    body.addAll(members);
  }

  public SyntaxOnlyElement getOpeningCurlyBrace() {
    return openingCurlyBrace;
  }

  public void setOpeningCurlyBrace(SyntaxOnlyElement openingCurlyBrace) {
    this.openingCurlyBrace = openingCurlyBrace;
  }

  public SyntaxOnlyElement getClosingCurlyBrace() {
    return closingCurlyBrace;
  }

  public void setClosingCurlyBrace(SyntaxOnlyElement closingCurlyBrace) {
    this.closingCurlyBrace = closingCurlyBrace;
  }

  @Override
  @NotAstProperty
  public List<ASTCssNode> getChilds() {
    List<ASTCssNode> result = ArraysUtils.asNonNullList((ASTCssNode) openingCurlyBrace);
    result.addAll(body);

    if (closingCurlyBrace != null)
      result.add(closingCurlyBrace);

    return result;
  }

  public List<ASTCssNode> getMembers() {
    return body;
  }

  public boolean isEmpty() {
    return body.isEmpty() && getOrphanComments().isEmpty();
  }

  public void removeAllMembers() {
    body = new ArrayList<ASTCssNode>();
  }

  public void addMembers(List<? extends ASTCssNode> members) {
    body.addAll(members);
  }

  public void addMembersAfter(List<? extends ASTCssNode> newMembers, ASTCssNode kid) {
    int index = body.indexOf(kid);
    if (index == -1)
      index = body.size();
    else
      index++;

    body.addAll(index, newMembers);
  }

  public void addMemberAfter(ASTCssNode newMember, ASTCssNode kid) {
    int index = body.indexOf(kid);
    if (index == -1)
      index = body.size();
    else
      index++;

    body.add(index, newMember);

  }

  public void addMember(ASTCssNode member) {
    body.add(member);
  }

  public void replaceMember(ASTCssNode oldMember, List<ASTCssNode> newMembers) {
    body.addAll(body.indexOf(oldMember), newMembers);
    body.remove(oldMember);
    oldMember.setParent(null);
    configureParentToAllChilds();//POSSIBLE OPTIMIZATION: all these could be more targetted
  }

  public void replaceMember(ASTCssNode oldMember, ASTCssNode newMember) {
    body.add(body.indexOf(oldMember), newMember);
    body.remove(oldMember);
    oldMember.setParent(null);
    newMember.setParent(this);
  }

  public List<ASTCssNode> membersByType(ASTCssNodeType type) {
    List<ASTCssNode> result = new ArrayList<ASTCssNode>();
    List<ASTCssNode> body = getMembers();
    for (ASTCssNode node : body) {
      if (node.getType() == type) {
        result.add(node);
      }
    }
    return result;
  }

  public List<ASTCssNode> membersByNotType(ASTCssNodeType type) {
    List<ASTCssNode> result = new ArrayList<ASTCssNode>();
    List<ASTCssNode> body = getMembers();
    for (ASTCssNode node : body) {
      if (node.getType() != type) {
        result.add(node);
      }
    }
    return result;
  }

  public boolean removeMember(ASTCssNode node) {
    return body.remove(node);
  }

  public Body clone() {
    Body result = (Body) super.clone();
    result.body = ArraysUtils.deeplyClonedList(body);
    result.openingCurlyBrace = openingCurlyBrace == null ? null : openingCurlyBrace.clone();
    result.closingCurlyBrace = closingCurlyBrace == null ? null : closingCurlyBrace.clone();
    result.configureParentToAllChilds();
    return result;
  }

  public Body emptyClone() {
    Body result = (Body) super.clone();
    result.body = new ArrayList<ASTCssNode>();
    return result;
  }

  public List<ASTCssNode> getDeclarations() {
    return membersByType(ASTCssNodeType.DECLARATION);
  }

  public List<ASTCssNode> getNotDeclarations() {
    return membersByNotType(ASTCssNodeType.DECLARATION);
  }

  @Override
  public void setParent(ASTCssNode parent) {
    if (parent != null && !(parent instanceof BodyOwner))
      throw new IllegalArgumentException("Body parent must be a BodyOwner: " + parent.getType());

    super.setParent(parent);
  }

}
