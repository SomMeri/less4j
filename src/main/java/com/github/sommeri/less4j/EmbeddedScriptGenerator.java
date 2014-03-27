package com.github.sommeri.less4j;

import com.github.sommeri.less4j.core.ast.Expression;

public interface EmbeddedScriptGenerator {

  String toScriptExpression(Expression value, LessProblems problemsHandler);

}
