package com.github.sommeri.less4j.core.compiler.stages;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Body;
import com.github.sommeri.less4j.core.problems.BugHappened;

public class ASTManipulator {

  public void replace(ASTCssNode oldChild, ASTCssNode newChild) {
    if (oldChild==newChild)
      return ;
    
    ASTCssNode parent = oldChild.getParent();
    PropertyDescriptor[] propertyDescriptors = PropertyUtils.getPropertyDescriptors(parent.getClass());
    for (PropertyDescriptor descriptor : propertyDescriptors) {
      Class<?> propertyType = descriptor.getPropertyType();
      if (propertyType!=null && propertyType.isInstance(newChild)) {
        Object value = getPropertyValue(parent, descriptor);
        if (value == oldChild) {
          setPropertyValue(newChild, parent, "parent");
          setPropertyValue(oldChild, null, "parent");
          setPropertyValue(parent, newChild, descriptor);
          //todo maybe set parents to childs?
          return;
        }
      }
    }
  }

  public void removeFromBody(ASTCssNode node) {
    ASTCssNode parent = node.getParent();
    if (!(parent instanceof Body)) {
      throw new BugHappened("Parent is not a body instance. " + parent, parent);
    }

    Body pBody = (Body) parent;
    pBody.removeMember(node);
    node.setParent(null);
  }

  public void replaceInBody(ASTCssNode oldNode, ASTCssNode newNode) {
    ASTCssNode parent = oldNode.getParent();
    if (!(parent instanceof Body)) {
      throw new BugHappened("Parent is not a body instance. " + parent, parent);
    }
    
    Body pBody = (Body) parent;
    pBody.replaceMember(oldNode, newNode);
  }

  public void replaceInBody(ASTCssNode oldNode, List<ASTCssNode> newNodes) {
    ASTCssNode parent = oldNode.getParent();
    if (!(parent instanceof Body)) {
      throw new BugHappened("Parent is not a body instance. " + parent, parent);
    }
    
    Body pBody = (Body) parent;
    pBody.replaceMember(oldNode, newNodes);
  }

  public void addIntoBody(ASTCssNode newNode, ASTCssNode afterNode) {
    ASTCssNode parent = afterNode.getParent();
    if (!(parent instanceof Body)) {
      throw new BugHappened("Parent is not a body instance. " + parent, parent);
    }
    
    Body pBody = (Body) parent;
    pBody.addMemberAfter(newNode, afterNode);
    newNode.setParent(parent);
  }

  public void addIntoBody(List<? extends ASTCssNode> newNodes, ASTCssNode afterNode) {
    ASTCssNode parent = afterNode.getParent();
    if (!(parent instanceof Body)) {
      throw new BugHappened("Parent is not a body instance. " + parent, parent);
    }
    
    Body pBody = (Body) parent;
    pBody.addMembersAfter(newNodes, afterNode);
    for (ASTCssNode newNode : newNodes) {
      newNode.setParent(pBody);
    }
  }

  private void setPropertyValue(ASTCssNode parent, ASTCssNode value, String name) {
    try {
      PropertyUtils.setProperty(parent, name, value);
    } catch (IllegalAccessException e) {
      throw new BugHappened(e, value);
    } catch (InvocationTargetException e) {
      throw new BugHappened(e, value);
    } catch (NoSuchMethodException e) {
      throw new BugHappened(e, value);
    }
  }

  private void setPropertyValue(ASTCssNode parent, ASTCssNode value, PropertyDescriptor descriptor) {
    try {
      PropertyUtils.setProperty(parent, descriptor.getName(), value);
    } catch (IllegalAccessException e) {
      throw new BugHappened(e, value);
    } catch (InvocationTargetException e) {
      throw new BugHappened(e, value);
    } catch (NoSuchMethodException e) {
      throw new BugHappened(e, value);
    }

  }

  private Object getPropertyValue(ASTCssNode object, PropertyDescriptor descriptor) {
    try {
      Object result = PropertyUtils.getProperty(object, descriptor.getName());
      return result;
    } catch (IllegalAccessException e) {
      throw new BugHappened(e, object);
    } catch (InvocationTargetException e) {
      throw new BugHappened(e, object);
    } catch (NoSuchMethodException e) {
      throw new BugHappened(e, object);
    }
  }

  public void removeFromClosestBody(ASTCssNode node) {
    ASTCssNode removeNode = node;
    while (removeNode!=null && !(removeNode.getParent() instanceof Body)) {
      removeNode = removeNode.getParent();
    }
    
    if (removeNode!=null)
      removeFromBody(removeNode);
  }

  public void moveMembersBetweenBodies(Body from, Body to) {
    to.addMembers(from.getMembers());
    from.removeAllMembers();
    to.configureParentToAllChilds();
  }

}
