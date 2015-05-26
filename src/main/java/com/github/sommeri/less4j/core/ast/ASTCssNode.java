package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.ast.annotations.NotAstProperty;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.utils.PubliclyCloneable;

public abstract class ASTCssNode implements PubliclyCloneable {

  private ASTCssNode parent;
  private boolean isSilent = false;
  //I'm using underlying structure as identified in cycle detector. If it stops to be identifying,
  //cycle detector must be modified. !
  private HiddenTokenAwareTree underlyingStructure;
  private List<Comment> openingComments = new ArrayList<Comment>();
  private List<Comment> orphanComments = new ArrayList<Comment>();
  private List<Comment> trailingComments = new ArrayList<Comment>();

  public ASTCssNode(HiddenTokenAwareTree underlyingStructure) {
    this.underlyingStructure = underlyingStructure;
    if (underlyingStructure == null)
      throw new IllegalArgumentException("Underlying can not be null. It is used for error reporting, so place there the closest token possible.");
  }

  public boolean isSilent() {
    return isSilent;
  }

  public void setSilent(boolean isSilent) {
    this.isSilent = isSilent;
  }

  /**
   * WARNING: it is up to the programmer to keep parent and childs getters and
   * setters consistent. Members of this hierarchy are not responsible for that.
   */
  @NotAstProperty
  public abstract List<? extends ASTCssNode> getChilds();

  /**
   * WARNING: it is up to the programmer to keep parent and childs getters and
   * setters consistent. Members of this hierarchy are not responsible for that.
   */
  public ASTCssNode getParent() {
    return parent;
  }

  public boolean hasParent() {
    return getParent()!=null;
  }

  /**
   * WARNING: it is up to the programmer to keep parent and childs getters and
   * setters consistent. Members of this hierarchy are not responsible for that.
   */
  public void setParent(ASTCssNode parent) {
    this.parent = parent;
  }

  public HiddenTokenAwareTree getUnderlyingStructure() {
    return underlyingStructure;
  }

  public void setUnderlyingStructure(HiddenTokenAwareTree underlyingStructure) {
    this.underlyingStructure = underlyingStructure;
  }

  @NotAstProperty
  public List<Comment> getTrailingComments() {
    return trailingComments;
  }

  public void setTrailingComments(List<Comment> trailingComments) {
    this.trailingComments = trailingComments;
  }

  public void addTrailingComments(List<Comment> comments) {
    this.trailingComments.addAll(comments);
  }

  public void addTrailingComment(Comment comment) {
    this.trailingComments.add(comment);
  }

  @NotAstProperty
  public List<Comment> getOpeningComments() {
    return openingComments;
  }

  public void setOpeningComments(List<Comment> openingComments) {
    this.openingComments = openingComments;
  }

  public void addOpeningComments(List<Comment> openingComments) {
    this.openingComments.addAll(openingComments);
  }

  public void addBeforeOpeningComments(List<Comment> openingComments) {
    this.openingComments.addAll(0, openingComments);
  }

  @NotAstProperty
  public List<Comment> getOrphanComments() {
    return orphanComments;
  }

  public void setOrphanComments(List<Comment> orphanComments) {
    this.orphanComments = orphanComments;
  }

  public void addOrphanComments(List<Comment> orphans) {
    this.orphanComments.addAll(orphanComments);
  }

  public abstract ASTCssNodeType getType();

  public boolean isFaulty() {
    return false;
  }

  public LessSource getSource() {
    return getUnderlyingStructure() == null ? null : getUnderlyingStructure().getSource();
  }

  public int getSourceLine() {
    return getUnderlyingStructure() == null ? -1 : getUnderlyingStructure().getLine();
  }

  public int getSourceColumn() {
    return getUnderlyingStructure() == null ? -1 : getUnderlyingStructure().getCharPositionInLine() + 1;
  }

  @Override
  public ASTCssNode clone() {
    try {
      ASTCssNode clone = (ASTCssNode) super.clone();
      clone.setOpeningComments(new ArrayList<Comment>(getOpeningComments()));
      clone.setOrphanComments(new ArrayList<Comment>(getOrphanComments()));
      clone.setTrailingComments(new ArrayList<Comment>(getTrailingComments()));
      clone.setParent(null);
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException("This is a bug - please submit issue with this stack trace and an input.");
    }
  }

  public void configureParentToAllChilds() {
    List<? extends ASTCssNode> childs = getChilds();
    for (ASTCssNode kid : childs) {
      kid.setParent(this);
    }
  }

  @Override
  public String toString() {
    StringBuilder builder = new StringBuilder();
    builder.append(this.getClass().getSimpleName()).append(" (");
    builder.append(getSourceLine()).append(":").append(getSourceColumn());
    builder.append(")");

    return builder.toString();
  }

}
