package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.ArraysUtils;

public abstract class Body <T extends ASTCssNode> extends ASTCssNode {

    private List<T> body = new ArrayList<T>();

    public Body() {
      this(null);
    }

    public Body(HiddenTokenAwareTree underlyingStructure) {
      super(underlyingStructure);
    }

    public Body(HiddenTokenAwareTree underlyingStructure, List<T> declarations) {
      this(underlyingStructure);
      body.addAll(declarations);
    }

    @Override
    public List<T> getChilds() {
      return body;
    }

    public boolean isEmpty() {
      return body.isEmpty() && getOrphanComments().isEmpty();
    }

    public void addMembers(List<? extends T> members) {
      body.addAll(members);
    }

    public void addMembersAfter(List<? extends T> nestedRulesets, ASTCssNode kid) {
      int index = body.indexOf(kid);
      if (index==-1)
        index = body.size();
      else 
        index++;
      
      body.addAll(index, nestedRulesets);

      
    }

    public void addMember(T member) {
      body.add(member);
    }

    public void replaceMember(T oldMember, List<T> newMembers) {
      body.addAll(body.indexOf(oldMember), newMembers);
      body.remove(oldMember);
    }

    public List<T> membersByType(ASTCssNodeType type) {
      List<T> result = new ArrayList<T>();
      List<T> body = getChilds();
      for (T node : body) {
        if (node.getType()==type) {
          result.add(node);
        }
      }
      return result;
    }

    public boolean removeMember(T node) {
      return body.remove(node);
    }
    
    @SuppressWarnings("unchecked")
    public Body<T> clone() {
      Body<T> result = (Body<T>) super.clone();
      result.body = ArraysUtils.deeplyClonedList(body);
      result.configureParentToAllChilds();
      return result;
    }

}
