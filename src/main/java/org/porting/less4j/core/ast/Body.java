package org.porting.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

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

    public void addMembers(List<T> members) {
      this.body.addAll(members);
    }

    public void addMember(T member) {
      this.body.add(member);
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

}
