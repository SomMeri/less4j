package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public abstract class Body extends ASTCssNode {

    private List<ASTCssNode> body = new ArrayList<ASTCssNode>();

    public Body(HiddenTokenAwareTree underlyingStructure) {
      super(underlyingStructure);
    }

    public Body(HiddenTokenAwareTree underlyingStructure, List<ASTCssNode> declarations) {
      this(underlyingStructure);
      body.addAll(declarations);
    }

    @Override
    public List<ASTCssNode> getChilds() {
      return body;
    }

    public List<ASTCssNode> getMembers() {
      return body;
    }

    protected List<ASTCssNode> getBody() {
      return body;
    }

    public boolean isEmpty() {
      return body.isEmpty() && getOrphanComments().isEmpty();
    }

    public void removeAllMembers() {
      body = new ArrayList<ASTCssNode>();
    }

    public void addMembers(List<ASTCssNode> members) {
      body.addAll(members);
    }

    public void addMembersAfter(List<? extends ASTCssNode> newMembers, ASTCssNode kid) {
      int index = body.indexOf(kid);
      if (index==-1)
        index = body.size();
      else 
        index++;
      
      body.addAll(index, newMembers);
    }

    public void addMemberAfter(ASTCssNode newMember, ASTCssNode kid) {
      int index = body.indexOf(kid);
      if (index==-1)
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
      configureParentToAllChilds();
    }

    public void replaceMember(ASTCssNode oldMember, ASTCssNode newMember) {
      body.add(body.indexOf(oldMember), newMember);
      body.remove(oldMember);
      oldMember.setParent(null);
      newMember.setParent(this);
    }

    public List<ASTCssNode> membersByType(ASTCssNodeType type) {
      List<ASTCssNode> result = new ArrayList<ASTCssNode>();
      List<ASTCssNode> body = getBody();
      for (ASTCssNode node : body) {
        if (node.getType()==type) {
          result.add(node);
        }
      }
      return result;
    }

    public List<ASTCssNode> membersByNotType(ASTCssNodeType type) {
      List<ASTCssNode> result = new ArrayList<ASTCssNode>();
      List<ASTCssNode> body = getBody();
      for (ASTCssNode node : body) {
        if (node.getType()!=type) {
          result.add(node);
        }
      }
      return result;
    }

    public boolean removeMember(ASTCssNode node) {
      return body.remove(node);
    }
    
    public Set<ASTCssNodeType> getSupportedMembers() {
      return new HashSet<ASTCssNodeType>(Arrays.asList(ASTCssNodeType.values()));  
    }

    public Body clone() {
      Body result = (Body) super.clone();
      result.body = ArraysUtils.deeplyClonedList(body);
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
      if (parent!=null && !(parent instanceof BodyOwner))
        throw new IllegalArgumentException("Body parent must be a BodyOwner: " + parent.getType());
      
      super.setParent(parent);
    }


}
