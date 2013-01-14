package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Media;
import com.github.sommeri.less4j.core.ast.StyleSheet;

public class MediaBubblerAndMerger {
  
  public void bubbleAndMergeMedia(ASTCssNode node) {
    bubbleUp(node);
  }

  private void bubbleUp(ASTCssNode node) {
//    switch (node.getType()) {
//    case MEDIA: {
//      bubbleUp((Media) node);
//      break;
//    }
//    }
//
//    List<? extends ASTCssNode> childs = new ArrayList<ASTCssNode>(less.getChilds());
//    for (ASTCssNode kid : childs) {
//      bubbleUp(kid);
//    }
  }

  private void bubbleUp(Media node) {
//    ASTCssNode parent = node.getParent();
//    while (parent!=null) {
//      switch (parent.getType()) {
//      case STYLE_SHEET: {
//        return ;
//      }
//      case MEDIA: {
//        return ;
//      }
//      }
//      // TODO Auto-generated method stub
//      blah blah 
//     
//    }
//    
  }

  private void merge(Media node, Media parent) {
    // TODO Auto-generated method stub
    
  }

}
