package org.porting.less4j.core.parser;

import org.antlr.runtime.Token;

public interface ILessGrammarCallback {

  void entering(int masterTokenType);

  void leaving(int masterTokenType, Object object);

  void skippingOffChannelToken(Token nextToken);

  ILessGrammarCallback NULL_CALLBACK = new ILessGrammarCallback() {

    @Override
    public void entering(int masterTokenType) {
    }

    @Override
    public void leaving(int masterTokenType, Object object) {
    }

    @Override
    public void skippingOffChannelToken(Token nextToken) {
      // TODO Auto-generated method stub
      
    }
  };
}
