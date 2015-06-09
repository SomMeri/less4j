package com.github.sommeri.less4j.antlr4.core.parser;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.antlr.runtime.Token;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.TokenSource;
import org.antlr.v4.runtime.misc.Tuple2;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.parser.LessG4Lexer;
import com.github.sommeri.less4j.core.parser.LexerLogic;

/** A Token that tracks the TokenSource name in each token. */
@SuppressWarnings("serial")
public class Antlr4_HiddenTokenAwareTree extends CommonToken {
  
  private LessSource source;
  private List<CommonToken> preceding = new LinkedList<CommonToken>();
  private List<CommonToken> orphans = new LinkedList<CommonToken>();
  private List<CommonToken> following = new LinkedList<CommonToken>();
  private CommonToken tokenAsCommon;
  
  private LexerLogic grammarKnowledge = new LexerLogic();
  protected int generalType = -3;

  public Antlr4_HiddenTokenAwareTree(int type, String text) {
    super(type, text);
  }

  public Antlr4_HiddenTokenAwareTree(Tuple2<? extends TokenSource, CharStream> source, int type, int channel, int start, int stop) {
    super(source, type, channel, start, stop);
  }

  public LessSource getSource() {
    return source;
  }

  public List<CommonToken> getPreceding() {
    return preceding;

  }

  public List<CommonToken> chopPreceedingUpToLastOfType(int type) {
    List<CommonToken> result = new ArrayList<CommonToken>();
    List<CommonToken> preceding = getPreceding();
    
    int index = lastTokenOfType(preceding, type);
    for (int i = 0; i <= index; i++) {
      CommonToken next = preceding.remove(0);
      result.add(next);
    }
    
    return result;
  }

  private int lastTokenOfType(List<CommonToken> list, int type) {
    if (list.isEmpty())
      return -1;

    for (int i = list.size() - 1; i >= 0; i--) {
      Token token = (Token) list.get(i);
      if (token.getType() == type)
        return i;
    }
    return -1;
  }

  public List<CommonToken> getFollowing() {
    return following;
  }

  public List<CommonToken> getOrphans() {
    return orphans;
  }

  public void addPreceding(CommonToken token) {
    preceding.add(token);
  }

  public void addPreceding(List<CommonToken> tokens) {
    preceding.addAll(tokens);
  }

  public void addBeforePreceding(List<CommonToken> tokens) {
    preceding.addAll(0, tokens);
  }

  public void addOrphan(CommonToken token) {
    orphans.add(token);
  }

  public void addOrphans(List<CommonToken> tokens) {
    orphans.addAll(tokens);
  }

  public void addFollowing(CommonToken token) {
    following.add(token);
  }

  public void addBeforeFollowing(List<CommonToken> tokens) {
    following.addAll(0, tokens);
  }

  public void addFollowing(List<CommonToken> tokens) {
    following.addAll(tokens);
  }

  public void giveHidden(Antlr4_HiddenTokenAwareTree previous, Antlr4_HiddenTokenAwareTree next) {
    if (previous != null)
      previous.addFollowing(getPreceding());
    if (next != null)
      next.addBeforePreceding(getFollowing());
  }

  public void moveHidden(Antlr4_HiddenTokenAwareTree previous, Antlr4_HiddenTokenAwareTree next) {
    if (previous != null) {
      previous.addFollowing(getPreceding());
      preceding = new LinkedList<CommonToken>();
    }
    if (next != null) {
      next.addBeforePreceding(getFollowing());
      following = new LinkedList<CommonToken>();
    }
  }

  /**
   * Lines numbering starts with 1.
   */
  public int getLine() {
    return super.getLine();
  }

  /**
   * Columns numbering starts with 1.
   */
  public int getColumn() {
    return super.getCharPositionInLine();
  }

  public int getCharPositionInLine() {
    return super.getCharPositionInLine();
  }
  
  public String toString() {
    return super.toString() + " " + getLine() + ":" + getCharPositionInLine() + 1;
  }

  public void removePreceding() {
    preceding = new ArrayList<CommonToken>();
  }

  public void removeFollowing() {
    following = new ArrayList<CommonToken>();
  }

  public boolean isReal() {
    return tokenAsCommon!=null && tokenAsCommon.getTokenIndex()!=-1;
  }

  public Antlr4_HiddenTokenAwareTree commentsLessClone() {
    try {
      Antlr4_HiddenTokenAwareTree clone = (Antlr4_HiddenTokenAwareTree) super.clone();
      clone.preceding = new LinkedList<CommonToken>();
      clone.orphans = new LinkedList<CommonToken>();
      clone.following = new LinkedList<CommonToken>();
      
      return clone;
    } catch (CloneNotSupportedException e) {
      throw new IllegalStateException();
    }
  }

  @Override
  @Deprecated
  public int getType() {
    return super.getType();
  }

  public int getGeneralType() {
    if ( generalType==-3 ) {
      generalType = generalizeTokenType(super.getType());
    }
    return generalType;
  }

  private int generalizeTokenType(int type) {
    if (grammarKnowledge.isAtName(type))
      return LessG4Lexer.AT_NAME;
    
    if (grammarKnowledge.isIdentifier(type))
      return LessG4Lexer.IDENT;
    
    return type;
  }

  public void setSource(LessSource source) {
    this.source = source;
  }

}