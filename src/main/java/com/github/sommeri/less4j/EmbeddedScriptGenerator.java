package com.github.sommeri.less4j;

import com.github.sommeri.less4j.core.ast.Expression;

public interface EmbeddedScriptGenerator {

  String toScript(Expression value, LessProblems problemsHandler);

}
