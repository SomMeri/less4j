package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionComparator;
import com.github.sommeri.less4j.core.compiler.expressions.PatternsComparator;
import com.github.sommeri.less4j.core.compiler.scopes.FoundMixin;

public class MixinsReferenceMatcher {

  private EvaluatedMixinReferenceCall evaluatedMixinReferenceParams;
  private ExpressionComparator comparator = new PatternsComparator();

  public MixinsReferenceMatcher(EvaluatedMixinReferenceCall evaluatedMixinReferenceParams) {
    this.evaluatedMixinReferenceParams = evaluatedMixinReferenceParams;
  }

  public List<FoundMixin> filterByParametersNumber(List<FoundMixin> mixins) {
    List<FoundMixin> result = new ArrayList<FoundMixin>();
    for (FoundMixin mixin : mixins) {
      if (hasRightNumberOfParameters(mixin.getMixin()))
        result.add(mixin);
    }
    return result;
  }

  public List<FoundMixin> filterByPatterns(List<FoundMixin> mixins) {
    List<FoundMixin> result = new ArrayList<FoundMixin>();
    for (FoundMixin mixin : mixins) {
      if (patternsMatch(mixin.getMixin()))
        result.add(mixin);
    }
    return result;
  }

  private boolean hasRightNumberOfParameters(ReusableStructure mixin) {
    int requiredNumber = evaluatedMixinReferenceParams.getNumberOfDeclaredParameters();
    int allDefined = mixin.getParameters().size();
    int mandatory = mixin.getMandatoryParameters().size();
    boolean hasRightNumberOfParameters = requiredNumber >= mandatory && (requiredNumber <= allDefined || mixin.hasCollectorParameter());
    return hasRightNumberOfParameters;
  }

  //FIXME: how does pattern matching and named arguments mix? This is most likely faulty
  private boolean patternsMatch(ReusableStructure mixin) {
    int i = 0;
    for (ASTCssNode parameter : mixin.getParameters()) {
      if (parameter instanceof Expression) {
        if (!evaluatedMixinReferenceParams.hasPositionalParameter(i))
          return false;

        Expression pattern = (Expression) parameter;
        if (!comparator.equal(pattern, evaluatedMixinReferenceParams.getPositionalParameter(i)))
          return false;
      }
      i++;
    }
    
    return true;
  }

}
