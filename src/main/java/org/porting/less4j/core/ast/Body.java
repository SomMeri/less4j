package org.porting.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

public abstract class Body <T extends ASTCssNode> extends ASTCssNode {

    private List<T> body = new ArrayList<T>();

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

    public void addDeclarations(List<T> body) {
      this.body.addAll(body);
    }

}
