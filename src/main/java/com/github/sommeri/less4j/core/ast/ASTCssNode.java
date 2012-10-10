package com.github.sommeri.less4j.core.ast;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;

public abstract class ASTCssNode implements Cloneable {
  
  private ASTCssNode parent;
  private HiddenTokenAwareTree underlyingStructure;
  private List<Comment> openingComments = new ArrayList<Comment>();
  private List<Comment> orphanComments = new ArrayList<Comment>();
  private List<Comment> trailingComments = new ArrayList<Comment>();

  public ASTCssNode(HiddenTokenAwareTree underlyingStructure) {
    this.underlyingStructure = underlyingStructure;
    if (underlyingStructure==null)
      throw new IllegalArgumentException("Underlying can not be null. It is used for error reporting, so place there the closest token possible.");
  }
  
  /**
   * WARNING: it is up to the programmer to keep parent and childs getters 
   * and setters consistent. Members of this hierarchy are not responsible for that.  
   */
  public abstract List<? extends ASTCssNode> getChilds();
  
  /**
   * WARNING: it is up to the programmer to keep parent and childs getters 
   * and setters consistent. Members of this hierarchy are not responsible for that.  
   */
  public ASTCssNode getParent() {
    return parent;
  }

  /**
   * WARNING: it is up to the programmer to keep parent and childs getters 
   * and setters consistent. Members of this hierarchy are not responsible for that.  
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

  public List<Comment> getTrailingComments() {
    return trailingComments;
  }

  public void setTrailingComments(List<Comment> trailingComments) {
    this.trailingComments = trailingComments;
  }
  
  public void addTrailingComments(List<Comment> comments) {
    this.trailingComments.addAll(comments);
  }
  
  public List<Comment> getOpeningComments() {
    return openingComments;
  }

  public void setOpeningComments(List<Comment> openingComments) {
    this.openingComments = openingComments;
  }

  public void addOpeningComments(List<Comment> openingComments) {
    this.openingComments.addAll(openingComments);
  }
  
  public List<Comment> getOrphanComments() {
    return orphanComments;
  }

  public void setOrphanComments(List<Comment> orphanComments) {
    this.orphanComments = orphanComments;
  }

  public abstract ASTCssNodeType getType();

  public int getSourceLine() {
    return getUnderlyingStructure()==null? -1 : getUnderlyingStructure().getLine();
  }

  public int getCharPositionInSourceLine() {
    return getUnderlyingStructure()==null? -1 : getUnderlyingStructure().getCharPositionInLine() + 1;
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

}
