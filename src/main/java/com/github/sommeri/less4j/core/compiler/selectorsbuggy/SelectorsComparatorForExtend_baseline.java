package com.github.sommeri.less4j.core.compiler.selectorsbuggy;

import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Selector;
import com.github.sommeri.less4j.core.ast.SelectorPart;
import com.github.sommeri.less4j.core.compiler.selectors.ElementSubsequentComparator;
import com.github.sommeri.less4j.core.compiler.selectors.GeneralComparatorForExtend;
import com.github.sommeri.less4j.core.compiler.selectors.SelectorPartComparator;
import com.github.sommeri.less4j.core.compiler.selectors.SelectorsComparatorUtils;
import com.github.sommeri.less4j.core.compiler.selectors.SimpleSelectorComparator;
import com.github.sommeri.less4j.utils.ListsComparator;

public class SelectorsComparatorForExtend_baseline {

  private SelectorsComparatorUtils utils = new SelectorsComparatorUtils();
  private ListsComparator listsComparator = new ListsComparator();

  private ElementSubsequentComparator elementSubsequentComparator;
  private SimpleSelectorComparator simpleSelectorComparator;
  private SelectorPartComparator selectorPartsComparator;

  public SelectorsComparatorForExtend_baseline(GeneralComparatorForExtend generalComparator) {
    this.elementSubsequentComparator = new ElementSubsequentComparator(generalComparator, utils);
    this.simpleSelectorComparator = new SimpleSelectorComparator(elementSubsequentComparator, utils);
    this.selectorPartsComparator = new SelectorPartComparator(simpleSelectorComparator);
  }

  public boolean equals(Selector first, Selector second) {
    List<SelectorPart> firstParts = first.getParts();
    List<SelectorPart> secondParts = second.getParts();
    return listsComparator.equals(firstParts, secondParts, selectorPartsComparator);
  }

  public boolean contains(Selector lookFor, Selector inSelector) {
    List<SelectorPart> lookForParts = lookFor.getParts();
    List<SelectorPart> inSelectorParts = inSelector.getParts();
    
    return containsInList(lookForParts, inSelectorParts, null) || containsEmbedded(lookForParts, inSelectorParts);
  }

  public boolean contains(Selector lookFor, Selector inSelector, Selector replaceBy) {
    List<SelectorPart> lookForParts = lookFor.getParts();
    List<SelectorPart> inSelectorParts = inSelector.getParts();
    
    return containsInList(lookForParts, inSelectorParts, replaceBy.getParts()) || containsEmbedded(lookForParts, inSelectorParts);
  }

  private boolean containsInList(List<SelectorPart> lookForParts, List<SelectorPart> inSelectorParts, List<SelectorPart> replaceBy) {
    boolean contains = listsComparator.contains(lookForParts, inSelectorParts, selectorPartsComparator);
//    if (contains)  {
//      List<MatchMarker<SelectorPart>> matches = listsComparator.findMatches(lookForParts, inSelectorParts, selectorPartsComparator);
//      System.out.println("Number of matches: " + matches.size());
//      
//      SelectorPart first = matches.get(0).getFirst();
//      SelectorPart last = matches.get(0).getLast();
//      
//      //TODO remove everything between first and last part - included
//      //TODO 
//      //TODO 
//      //TODO 
//      //TODO 
//      selectorPartsComparator.cutSuffix(replaceBy.get(0), first);
//      //FIXME: predpokladame ze
//      if ()
//      
//      System.out.println(first + " ----- " + last);
//    }
      
    return contains;
  }

  private boolean containsEmbedded(List<SelectorPart> lookFor, List<SelectorPart> inSelectors) {
    for (SelectorPart inside : inSelectors) {
      if (containsEmbedded(lookFor, inside))
        return true;
    }
    return false;
  }

  private boolean containsEmbedded(List<SelectorPart> lookFor, ASTCssNode inside) {
    for (ASTCssNode kid : inside.getChilds()) {
      switch (kid.getType()) {
      case SELECTOR:
        Selector kidSelector = (Selector) kid;
        if (containsInList(lookFor, kidSelector.getParts(), null))
          return true;
        break;
      default:
        if (containsEmbedded(lookFor, kid))
          return true;
      }
    }
    return false;
  }

}
