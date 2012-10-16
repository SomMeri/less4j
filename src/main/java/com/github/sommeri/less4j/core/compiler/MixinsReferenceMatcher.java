package com.github.sommeri.less4j.core.compiler;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.PureMixin;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionEvaluator;

public class MixinsReferenceMatcher {

  private ExpressionEvaluator evaluator;
  private ExpressionComparator comparator;

  public MixinsReferenceMatcher(ActiveScope activeScope) {
    comparator = new PatternsComparator();
    evaluator = new ExpressionEvaluator(activeScope);
  }

  public List<FullMixinDefinition> filter(MixinReference reference, List<FullMixinDefinition> mixins) {
    return filterByParametersNumber(reference, mixins);
  }

  private List<FullMixinDefinition> filterByParametersNumber(MixinReference reference, List<FullMixinDefinition> mixins) {
    int requiredNumber = reference.getParameters().size();
    List<FullMixinDefinition> result = new ArrayList<FullMixinDefinition>();
    for (FullMixinDefinition mixinDefinition : mixins) {
      if (hasRightNumberOfParameters(mixinDefinition, requiredNumber))
        result.add(mixinDefinition);
    }
    return result;
  }

  private boolean hasRightNumberOfParameters(FullMixinDefinition mixinDefinition, int requiredNumber) {
    PureMixin mixin = mixinDefinition.getMixin();
    int allDefined = mixin.getParameters().size();
    int mandatory = mixin.getMandatoryParameters().size();
    boolean hasRightNumberOfParameters = requiredNumber >= mandatory && (requiredNumber <= allDefined || mixin.hasCollectorParameter());
    return hasRightNumberOfParameters;
  }

  public boolean patternsMatch(MixinReference reference, FullMixinDefinition mixin) {
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
