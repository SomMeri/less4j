package com.github.sommeri.less4j.utils;

import java.util.Iterator;
import java.util.List;

import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.ReusableStructureName;
import com.github.sommeri.less4j.core.ast.Variable;

public class LessPrinter {
  
  public String toString(MixinReference reference) {
    StringBuilder result = new StringBuilder();
    Iterator<ReusableStructureName> iterator = reference.getNameChain().iterator();
    if (iterator.hasNext())
      result.append(iterator.next().asString());

    while (iterator.hasNext()) {
      result.append(" > ");
      result.append(iterator.next().asString());
    }

    result.append(" > ");
    result.append(reference.getFinalNameAsString());
    result.append("(...)");

    return result.toString();
  }

  
  public String toVariablesString(List<Variable> cycle) {
    String result = "";
    Iterator<Variable> iCycle = cycle.iterator();
    while (iCycle.hasNext()) {
      Variable variable = iCycle.next();
      result += variable.getName() + " ("+ variable.getSourceLine()+":"+variable.getCharPositionInSourceLine()+") ";
      if (iCycle.hasNext())
        result +="-> ";
    }
    
    return result;
  }

  public String toMixinReferencesString(List<MixinReference> cycle) {
    String result = "";
    Iterator<MixinReference> iReference = cycle.iterator();
    while (iReference.hasNext()) {
      MixinReference reference = iReference.next();
      result += reference.getFinalName() + " ("+ reference.getSourceLine()+":"+reference.getCharPositionInSourceLine()+") ";
      if (iReference.hasNext())
        result +="-> ";
    }
    
    return result;
  }

}
