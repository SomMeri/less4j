package com.github.sommeri.less4j.antlr4.core.parser;

import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.antlr.v4.runtime.tree.Tree;

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
    
    from.moveHidden(previousHolder, followingHolder);
  }

}
