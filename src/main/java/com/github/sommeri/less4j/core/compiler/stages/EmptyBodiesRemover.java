package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.BodyOwner;
import com.github.sommeri.less4j.core.ast.Comment;

public class EmptyBodiesRemover {

  private ASTManipulator manipulator = new ASTManipulator();

  public void removeEmptyBodies(ASTCssNode node) {
    List<ASTCssNode> childs = new ArrayList<ASTCssNode>(node.getChilds());
    List<Comment> ownerlessComments = new ArrayList<Comment>();
    for (ASTCssNode kid : childs) {
      removeEmptyBodies(kid);

      if (shouldRemove(kid)) {
        ownerlessComments.addAll(kid.getOpeningComments());
        ownerlessComments.addAll(kid.getTrailingComments());
        manipulator.removeFromBody(kid);
      } else {
        if (!ownerlessComments.isEmpty()) {
          manipulator.addOpeningComments(kid, ownerlessComments);
          ownerlessComments = new ArrayList<Comment>();
        }
      }
    }
  }

  @SuppressWarnings("unchecked")
  private boolean shouldRemove(ASTCssNode kid) {
    if (!AstLogic.isBodyOwner(kid))
      return false;

    BodyOwner<? extends ASTCssNode> bodyOwner = (BodyOwner<? extends ASTCssNode>) kid;
    return bodyOwner.getBody() != null && bodyOwner.getBody().isEmpty();
  }

}
