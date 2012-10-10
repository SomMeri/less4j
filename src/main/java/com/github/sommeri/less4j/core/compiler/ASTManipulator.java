package com.github.sommeri.less4j.core.compiler;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.commons.beanutils.PropertyUtils;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Body;

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
          return;
        }
      }
    }
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void removeFromBody(ASTCssNode node) {
    ASTCssNode parent = node.getParent();
    if (!(parent instanceof Body)) {
      throw new CompileException("Parent is not a body instance. " + parent, parent);
    }

    Body pBody = (Body) parent;
    pBody.removeMember(node);
  }

  @SuppressWarnings({ "rawtypes", "unchecked" })
  public void replaceInBody(ASTCssNode oldNode, List<ASTCssNode> newNodes) {
    ASTCssNode parent = oldNode.getParent();
    if (!(parent instanceof Body)) {
      throw new CompileException("Parent is not a body instance. " + parent, parent);
    }
    
    Body pBody = (Body) parent;
    pBody.replaceMember(oldNode, newNodes);
    oldNode.setParent(null);
    for (ASTCssNode kid : newNodes) {
      kid.setParent(pBody);
    }
  }

  private void setPropertyValue(ASTCssNode parent, ASTCssNode value, String name) {
    try {
      PropertyUtils.setProperty(parent, name, value);
    } catch (IllegalAccessException e) {
      throw new CompileException(e, value);
    } catch (InvocationTargetException e) {
      throw new CompileException(e, value);
    } catch (NoSuchMethodException e) {
      throw new CompileException(e, value);
    }
  }

  private void setPropertyValue(ASTCssNode parent, ASTCssNode value, PropertyDescriptor descriptor) {
    try {
      PropertyUtils.setProperty(parent, descriptor.getName(), value);
    } catch (IllegalAccessException e) {
      throw new CompileException(e, value);
    } catch (InvocationTargetException e) {
      throw new CompileException(e, value);
    } catch (NoSuchMethodException e) {
      throw new CompileException(e, value);
    }

  }

  public Object getPropertyValue(ASTCssNode object, PropertyDescriptor descriptor) {
    try {
      Object result = PropertyUtils.getProperty(object, descriptor.getName());
      return result;
    } catch (IllegalAccessException e) {
      throw new CompileException(e, object);
    } catch (InvocationTargetException e) {
      throw new CompileException(e, object);
    } catch (NoSuchMethodException e) {
      throw new CompileException(e, object);
    }
  }

}
