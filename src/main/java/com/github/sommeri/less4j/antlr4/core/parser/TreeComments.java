package com.github.sommeri.less4j.antlr4.core.parser;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Tree;

import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.parser.LessG4Parser.DeclarationContext;
import com.github.sommeri.less4j.core.parser.LessG4Parser.SimpleSelectorContext;
import com.github.sommeri.less4j.core.problems.BugHappened;

/**
 * Associate a property with a parse tree node. Useful with parse tree listeners
 * that need to associate values with particular tree nodes, kind of like
 * specifying a return value for the listener event method that visited a
 * particular node. Example:
 *
 * <pre>
 * ParseTreeProperty&lt;Integer&gt; values = new ParseTreeProperty&lt;Integer&gt;();
 * values.put(tree, 36);
 * int x = values.get(tree);
 * values.removeFrom(tree);
 * </pre>
 *
 * You would make one decl (values here) in the listener and use lots of times
 * in your event methods.
 */
public class TreeComments implements Iterable<Entry<Tree, NodeCommentsHolder>> {
  protected Map<Tree, NodeCommentsHolder> comments = new IdentityHashMap<Tree, NodeCommentsHolder>();

  public NodeCommentsHolder getOrCreate(Tree node) {
    NodeCommentsHolder result = comments.get(node);
    if (result == null) {
      result = new NodeCommentsHolder();
      comments.put(node, result);
    }

    return result;
  }

  public NodeCommentsHolder get(Tree node) {
    return comments.get(node);
  }

  public void put(Tree node, NodeCommentsHolder value) {
    comments.put(node, value);
  }

  public NodeCommentsHolder removeFrom(Tree node) {
    return comments.remove(node);
  }

  public Set<Entry<Tree, NodeCommentsHolder>> all() {
    return comments.entrySet();
  }

  @Override
  public Iterator<Entry<Tree, NodeCommentsHolder>> iterator() {
    return all().iterator();
  }

  public void moveHidden(ParseTree previous, ParseTree fromSource, ParseTree following) {
    if (!comments.containsKey(fromSource))
      return;

    NodeCommentsHolder from = comments.get(fromSource);
    NodeCommentsHolder previousHolder = previous == null ? null : getOrCreate(previous);
    NodeCommentsHolder followingHolder = following == null ? null : getOrCreate(following);

    if (previousHolder!=followingHolder) {
      from.moveHidden(previousHolder, followingHolder);
    } else {
      from.moveAsIs(followingHolder);
    }
  }

  public void moveToPrevious(ParseTree previous, ParseTree fromSource, int upToType) {
    if (!comments.containsKey(fromSource))
      return;

    NodeCommentsHolder from = comments.get(fromSource);
    List<CommonToken> toBeMoved = from.chopPreceedingUpToLastOfType(upToType);

    getOrCreate(previous).addFollowing(toBeMoved);
  }

  public void extractOrphans(ParseTree toOrphans, ParseTree preceedingFrom, int upToType) {
    if (!comments.containsKey(preceedingFrom))
      return ;
    
    NodeCommentsHolder commentsHolder = comments.get(preceedingFrom);
    List<CommonToken> toBeOrphans = commentsHolder.chopPreceedingUpToLastOfType(upToType);
    
    getOrCreate(toOrphans).addOrphans(toBeOrphans);
  }

  public void pushHiddenToKids(ParseTree ctx) {
    if (!comments.containsKey(ctx))
      return ;
    
    int childCount = ctx.getChildCount();
    if (childCount == 0)
      return;
    
    ParseTree first = ctx.getChild(0);
    ParseTree last = ctx.getChild(childCount-1);
    
    moveHidden(first, ctx, last);
  }

  public void moveHidden(HiddenTokenAwareTree underlyingStructure, ParseTree rbraceToken, ParseTree following) {
    if (underlyingStructure instanceof HiddenTokenAwareTreeAdapter) {
      HiddenTokenAwareTreeAdapter adapter = (HiddenTokenAwareTreeAdapter) underlyingStructure;
      moveHidden(adapter.getUnderlyingNode(), rbraceToken, following);
      return ;
    }
    
    throw new BugHappened("this method should not exist at all", underlyingStructure);
   
  }

  public void shiftFollowing(ParseTree from, ParseTree to) {
    if (!comments.containsKey(from))
      return ;
    
    NodeCommentsHolder fromHolder = comments.get(from);
    NodeCommentsHolder toHolder = getOrCreate(to);
    fromHolder.moveHidden(null, toHolder);
  }

}
