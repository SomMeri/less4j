package com.github.sommeri.less4j.core.compiler.remove;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.PureMixin;
import com.github.sommeri.less4j.core.compiler.ExpressionComparator;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionEvaluator;
import com.github.sommeri.less4j.core.compiler.expressions.PatternsComparator;
import com.github.sommeri.less4j.core.compiler.scopes.FullMixinDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.Scope;

public class MixinsReferenceMatcherOld {

  private ExpressionEvaluatorOld evaluator;
  private ExpressionComparator comparator;

  public MixinsReferenceMatcherOld(ActiveScope activeScope) {
    comparator = new PatternsComparator();
    evaluator = new ExpressionEvaluatorOld(activeScope);
  }

  public List<FullMixinDefinitionOld> filter(MixinReference reference, List<FullMixinDefinitionOld> mixins) {
    int requiredNumber = reference.getParameters().size();
    List<FullMixinDefinitionOld> result = new ArrayList<FullMixinDefinitionOld>();
    for (FullMixinDefinitionOld mixinDefinition : mixins) {
      if (hasRightNumberOfParameters(mixinDefinition, requiredNumber) && patternsMatch(reference, mixinDefinition))
        result.add(mixinDefinition);
    }
    return result;
  }

  private boolean hasRightNumberOfParameters(FullMixinDefinitionOld mixinDefinition, int requiredNumber) {
    PureMixin mixin = mixinDefinition.getMixin();
    int allDefined = mixin.getParameters().size();
    int mandatory = mixin.getMandatoryParameters().size();
    boolean hasRightNumberOfParameters = requiredNumber >= mandatory && (requiredNumber <= allDefined || mixin.hasCollectorParameter());
    return hasRightNumberOfParameters;
  }

  private boolean patternsMatch(MixinReference reference, FullMixinDefinitionOld mixin) {
    int i = 0;
    for (ASTCssNode parameter : mixin.getMixin().getParameters()) {
      if (parameter instanceof Expression) {
        if (!reference.hasParameter(i))
          return false;

        Expression pattern = (Expression) parameter;
        if (!comparator.equal(pattern, evaluator.evaluate(reference.getParameter(i))))
          return false;
      }
      i++;
    }
    
    return true;
  }

}
