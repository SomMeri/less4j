package com.github.sommeri.less4j;

import com.github.sommeri.less4j.core.ast.Expression;

public interface EmbeddedScripting {

  String toScriptExpression(Expression value, LessProblems problemsHandler);

}
