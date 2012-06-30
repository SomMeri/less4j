package org.porting.less4j.core.parser;

import org.antlr.runtime.Token;

public class SelectiveGrammarCallback implements ILessGrammarCallback {

  @Override
  public void entering(int masterTokenType) {
    log("entering " + masterTokenType);
    // TODO Auto-generated method stub

  }

  @Override
  public void leaving(int masterTokenType, Object object) {
    log("leaving " + masterTokenType);
  }

  @Override
  public void skippingOffChannelToken(Token nextToken) {
    // TODO Auto-generated method stub
    log("skippingOffChannelToken " + nextToken.getText());
  }

  private void log(Object message) {
    System.out.println(message);
  }

}
