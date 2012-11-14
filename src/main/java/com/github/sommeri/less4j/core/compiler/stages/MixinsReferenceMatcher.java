package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.PureMixin;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionComparator;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionEvaluator;
import com.github.sommeri.less4j.core.compiler.expressions.PatternsComparator;
import com.github.sommeri.less4j.core.compiler.scopes.FullMixinDefinition;
import com.github.sommeri.less4j.core.compiler.scopes.Scope;

public class MixinsReferenceMatcher {

  private ExpressionEvaluator evaluator;
  private ExpressionComparator comparator = new PatternsComparator();

  public MixinsReferenceMatcher(Scope scope) {
    evaluator = new ExpressionEvaluator(scope);
  }

  public List<FullMixinDefinition> filter(MixinReference reference, List<FullMixinDefinition> mixins) {
    int requiredNumber = reference.getNumberOfDeclaredParameters();
    List<FullMixinDefinition> result = new ArrayList<FullMixinDefinition>();
    for (FullMixinDefinition mixin : mixins) {
      if (hasRightNumberOfParameters(mixin.getMixin(), requiredNumber) && patternsMatch(reference, mixin.getMixin()))
        result.add(mixin);
    }
    return result;
  }

  private boolean hasRightNumberOfParameters(PureMixin mixin, int requiredNumber) {
    int allDefined = mixin.getParameters().size();
    int mandatory = mixin.getMandatoryParameters().size();
    boolean hasRightNumberOfParameters = requiredNumber >= mandatory && (requiredNumber <= allDefined || mixin.hasCollectorParameter());
    return hasRightNumberOfParameters;
  }

  //FIXME: how does pattern matching and named arguments mix? This is most likely faulty
  private boolean patternsMatch(MixinReference reference, PureMixin mixin) {
    int i = 0;
    for (ASTCssNode parameter : mixin.getParameters()) {
      if (parameter instanceof Expression) {
        if (!reference.hasPositionalParameter(i))
          return false;

        Expression pattern = (Expression) parameter;
        if (!comparator.equal(pattern, evaluator.evaluate(reference.getPositionalParameter(i))))
          return false;
      }
      i++;
    }
    
    return true;
  }

}
