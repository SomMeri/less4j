package com.github.sommeri.less4j.core.parser;

import java.util.Iterator;
import java.util.List;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ArgumentDeclaration;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.MixinReference;
import com.github.sommeri.less4j.core.ast.ReusableStructure;
import com.github.sommeri.less4j.core.ast.Variable;
import com.github.sommeri.less4j.core.ast.VariableDeclaration;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class MixinsParametersBuilder {

  private final ASTBuilderSwitch parentBuilder;
  @SuppressWarnings("unused")
  private ProblemsHandler problemsHandler = new ProblemsHandler();

  public MixinsParametersBuilder(ASTBuilderSwitch astBuilderSwitch, ProblemsHandler problemsHandler) {
    this.parentBuilder = astBuilderSwitch;
    this.problemsHandler = problemsHandler;
  }

  public void handleMixinReferenceArguments(HiddenTokenAwareTree token, MixinReference reference) {
    List<HiddenTokenAwareTree> children = token.getChildren();
    if (containsType(children, LessLexer.SEMI)) {
      //easy case
      handleSemicolonSplitMixinReferenceArguments(token, reference);
    } else {
      //hard case
      handleCommaSplitMixinReferenceArguments(token, reference);
    }
  }

  private void handleSemicolonSplitMixinReferenceArguments(HiddenTokenAwareTree token, MixinReference reference) {
    List<HiddenTokenAwareTree> children = token.getChildren();
    for (HiddenTokenAwareTree kid : children) {
      if (kid.getType() != LessLexer.SEMI) {
        ASTCssNode parameter = parentBuilder.switchOn(kid);
        if (parameter.getType() == ASTCssNodeType.VARIABLE_DECLARATION)
          reference.addNamedParameter((VariableDeclaration) parameter);
        else
          reference.addPositionalParameter((Expression) parameter);
      }
    }
  }

  private void handleCommaSplitMixinReferenceArguments(HiddenTokenAwareTree token, MixinReference reference) {
    List<HiddenTokenAwareTree> children = token.getChildren();
    for (HiddenTokenAwareTree kid : children) {
      ASTCssNode parameter = parentBuilder.switchOn(kid);
      if (parameter.getType() == ASTCssNodeType.VARIABLE_DECLARATION) {
        VariableDeclaration variableDeclaration = (VariableDeclaration) parameter;
        Iterator<Expression> expressions = variableDeclaration.getValue().splitByComma().iterator();
        
        variableDeclaration.setValue(expressions.next());
        reference.addNamedParameter(variableDeclaration);
        
        addAsPositional(reference, expressions);
      } else {
        Iterator<Expression> expressions = ((Expression) parameter).splitByComma().iterator();
        addAsPositional(reference, expressions);
      }
    }
  }

  private void addAsPositional(MixinReference reference, Iterator<Expression> expressions) {
    while (expressions.hasNext()) {
      reference.addPositionalParameter(expressions.next());
    }
  }

  private boolean containsType(List<HiddenTokenAwareTree> list, int type) {
    for (HiddenTokenAwareTree element : list) {
      if (element.getType() == type)
        return true;
    }
    return false;
  }

  public void handleMixinDeclarationArguments(HiddenTokenAwareTree token, ReusableStructure declaration) {
    List<HiddenTokenAwareTree> children = token.getChildren();
    if (containsType(children, LessLexer.SEMI)) {
      //easy case
      handleSemicolonSplitMixinDeclarationArguments(token, declaration);
    } else {
      //hard case
      handleCommaSplitMixinDeclarationArguments(token, declaration);
    }
  }

  private void handleCommaSplitMixinDeclarationArguments(HiddenTokenAwareTree token, ReusableStructure declaration) {
    List<HiddenTokenAwareTree> children = token.getChildren();
    for (HiddenTokenAwareTree kid : children) {
      if (kid.getType() != LessLexer.SEMI) {
        ASTCssNode argument = parentBuilder.switchOn(kid);
        
        if (argument.getType() == ASTCssNodeType.ARGUMENT_DECLARATION) {
          ArgumentDeclaration argumentDeclaration = (ArgumentDeclaration) argument;
          declaration.addParameter(argumentDeclaration);

          if (argumentDeclaration.getValue()!=null) {
            Iterator<Expression> expressions = argumentDeclaration.getValue().splitByComma().iterator();
            argumentDeclaration.setValue(expressions.next());
            addParameters(declaration, expressions);
          }
        } else {
          Iterator<Expression> expressions = ((Expression) argument).splitByComma().iterator();
          addParameters(declaration, expressions);
        }
      }
    }
  }

  private void addParameters(ReusableStructure declaration, Iterator<Expression> expressions) {
    while (expressions.hasNext()) {
      Expression next = expressions.next();
      
      if (next.getType()==ASTCssNodeType.VARIABLE) {
        declaration.addParameter(new ArgumentDeclaration((Variable)next, null));
      } else {
        declaration.addParameter(next);
      }
    }
  }

  private void handleSemicolonSplitMixinDeclarationArguments(HiddenTokenAwareTree token, ReusableStructure declaration) {
    List<HiddenTokenAwareTree> children = token.getChildren();
    for (HiddenTokenAwareTree kid : children) {
      if (kid.getType() != LessLexer.SEMI) {
        declaration.addParameter(parentBuilder.switchOn(kid));
      }
    }
  }

}
