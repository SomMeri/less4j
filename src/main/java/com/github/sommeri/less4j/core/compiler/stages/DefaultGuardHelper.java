package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.compiler.expressions.GuardValue;
import com.github.sommeri.less4j.core.problems.BugHappened;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.utils.ArraysUtils;

public class DefaultGuardHelper {

  private final ProblemsHandler problemsHandler;

  public DefaultGuardHelper(ProblemsHandler problemsHandler) {
    this.problemsHandler = problemsHandler;
  }

  public List<BodyCompilationData> chooseMixinsToBeUsed(List<BodyCompilationData> compiledMixins, final MixinReference reference) {
    // count how many mixins of each kind we encountered
    int normalMixinsCnt = ArraysUtils.count(compiledMixins, GuardValue.USE.filter());
    int ifNotCnt = ArraysUtils.count(compiledMixins, GuardValue.USE_IF_NOT_DEFAULT.filter());
    int ifDefaultCnt = ArraysUtils.count(compiledMixins, GuardValue.USE_IF_DEFAULT.filter());
    
    //sanity check - could be removed - keeping only for debugging purposes
    if (normalMixinsCnt+ifNotCnt+ifDefaultCnt!=compiledMixins.size())
      throw new BugHappened("Unexpected mixin type in compiled mixins list.", reference);

    // We know now that default() value is false. We do not care whether there was some potentional ambiguity or not and return anything that is not default. 
    if (normalMixinsCnt > 0) {
      return keepOnly(compiledMixins, GuardValue.USE, GuardValue.USE_IF_NOT_DEFAULT);  
    }
    
    //there are multiple mixins using default() function and nothing else - that is ambiguous (period). 
    if (ifDefaultCnt+ifNotCnt > 1) {
      List<BodyCompilationData> errorSet = keepOnly(compiledMixins, GuardValue.USE_IF_DEFAULT,GuardValue.USE_IF_NOT_DEFAULT);
      problemsHandler.ambiguousDefaultSet(reference, extractOriginalMixins(errorSet));
      //no mixins are going to be used
      return Collections.emptyList();
    }

    //now we know that default function returns true
    return keepOnly(compiledMixins, GuardValue.USE_IF_DEFAULT);
  }

  /**
   * Removes all comiled mixins from compiledMixins list with wrong use of default function. 
   * Warning: Modifies the compiledMixins list.

   * @param compiledMixins - list of compiled mixins - will be modified. 
   * @param kind - types of mixins that are going to stay.
   * @return compiledMixins - for convenience
   */
  private List<BodyCompilationData> keepOnly(List<BodyCompilationData> compiledMixins, GuardValue... kind) {
    Set<GuardValue> expectedUses = ArraysUtils.asSet(kind);
    Iterator<BodyCompilationData> iterator = compiledMixins.iterator();
    while (iterator.hasNext()) {
      BodyCompilationData compiled = iterator.next();
      if (!expectedUses.contains(compiled.getGuardValue())) {
        iterator.remove();
      }
    }
    return compiledMixins;
  }

  private List<ASTCssNode> extractOriginalMixins(List<BodyCompilationData> compiledMixins) {
    List<ASTCssNode> result = new ArrayList<ASTCssNode>();
    for (BodyCompilationData compiled : compiledMixins) {
      result.add((ASTCssNode)compiled.getCompiledBodyOwner());
    }
    return result;
  }


}
