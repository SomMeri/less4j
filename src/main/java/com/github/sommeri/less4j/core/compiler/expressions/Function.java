package com.github.sommeri.less4j.core.compiler.expressions;

import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

interface Function {
  Expression evaluate(Expression parameters, ProblemsHandler problemsHandler);
}