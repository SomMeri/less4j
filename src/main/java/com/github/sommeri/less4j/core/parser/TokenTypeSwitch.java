package com.github.sommeri.less4j.core.parser;

import com.github.sommeri.less4j.core.parser.LessLexer;
import com.github.sommeri.less4j.core.problems.BugHappened;

public abstract class TokenTypeSwitch<T> {

  public T switchOn(HiddenTokenAwareTree token) {
    int type = token.getType();

    if (type == LessLexer.ELEMENT_SUBSEQUENT) {
      return handleElementSubsequent(token);
    }

    if (type == LessLexer.RULESET) {
      return handleRuleSet(token);
    }

    if (type == LessLexer.CSS_CLASS) {
      return handleCssClass(token);
    }

    if (type == LessLexer.PSEUDO) {
      return handlePseudo(token);
    }

    if (type == LessLexer.EOF) {
      return handleEOF(token);
    }

    if (type == LessLexer.SELECTOR) {
      return handleSelector(token);
    }

    if (type == LessLexer.STYLE_SHEET) {
      return handleStyleSheet(token);
    }

    if (type == LessLexer.ATTRIBUTE) {
      return handleSelectorAttribute(token);
    }

    if (type == LessLexer.ID_SELECTOR) {
      return handleIdSelector(token);
    }

    if (type == LessLexer.CHARSET_DECLARATION) {
      return handleCharsetDeclaration(token);
    }

    if (type == LessLexer.FONT_FACE_SYM) {
      return handleFontFace(token);
    }

    if (type == LessLexer.DECLARATION) {
      return handleDeclaration(token);
    }

    if (type == LessLexer.BODY) {
      return handleRuleSetsBody(token);
    }

    if (type == LessLexer.EXPRESSION) {
      return handleExpression(token);
    }
    if (type == LessLexer.NTH) {
      return handleNth(token);
    }

    if (type == LessLexer.TERM)
      return handleTerm(token);

    if (type == LessLexer.MEDIA_SYM)
      return handleMedia(token);

    if (type == LessLexer.MEDIA_QUERY)
      return handleMediaQuery(token);

    if (type == LessLexer.MEDIUM_TYPE) {
      return handleMedium(token);
    }

    if (type == LessLexer.MEDIA_EXPRESSION)
      return handleMediaExpression(token);

    if (type == LessLexer.VARIABLE_DECLARATION)
      return handleVariableDeclaration(token);

    if (type == LessLexer.ARGUMENT_DECLARATION)
      return handleArgumentDeclaration(token);

    if (type == LessLexer.VARIABLE)
      return handleVariable(token);

    if (type == LessLexer.VARIABLE)
      return handleIndirectVariable(token);

    if (type == LessLexer.REUSABLE_STRUCTURE)
      return handleReusableStructureDeclaration(token);

    if (type == LessLexer.MIXIN_REFERENCE)
      return handleMixinReference(token);

    if (type == LessLexer.NAMESPACE_REFERENCE)
      return handleNamespaceReference(token);

    if (type == LessLexer.MIXIN_PATTERN)
      return handleMixinPattern(token);

    if (type == LessLexer.GUARD)
      return handleGuard(token);

    if (type == LessLexer.GUARD_CONDITION)
      return handleGuardCondition(token);

    if (type == LessLexer.NESTED_APPENDER)
      return handleNestedAppender(token);

    if (type == LessLexer.SIMPLE_SELECTOR)
      return handleSimpleSelector(token);

    throw new BugHappened("Unexpected token type: " + type + " for " + token.getText(), token);
  }

  public abstract T handleSimpleSelector(HiddenTokenAwareTree token);

  public abstract T handleNestedAppender(HiddenTokenAwareTree token);

  public abstract T handleElementSubsequent(HiddenTokenAwareTree token);

  public abstract T handleGuardCondition(HiddenTokenAwareTree token);

  public abstract T handleGuard(HiddenTokenAwareTree token);

  public abstract T handleMixinPattern(HiddenTokenAwareTree token);

  public abstract T handleReusableStructureDeclaration(HiddenTokenAwareTree token);

  public abstract T handleMixinReference(HiddenTokenAwareTree token);

  public abstract T handleNamespaceReference(HiddenTokenAwareTree token);

  public abstract T handleVariableDeclaration(HiddenTokenAwareTree token);

  public abstract T handleArgumentDeclaration(HiddenTokenAwareTree token);

  public abstract T handleVariable(HiddenTokenAwareTree token);

  public abstract T handleIndirectVariable(HiddenTokenAwareTree token);

  public abstract T handleMediaExpression(HiddenTokenAwareTree token);

  public abstract T handleMedium(HiddenTokenAwareTree token);

  public abstract T handleNth(HiddenTokenAwareTree token);

  public abstract T handleRuleSetsBody(HiddenTokenAwareTree token);

  public abstract T handleMediaQuery(HiddenTokenAwareTree token);

  public abstract T handleMedia(HiddenTokenAwareTree token);

  public abstract T handleTerm(HiddenTokenAwareTree token);

  public abstract T handleExpression(HiddenTokenAwareTree token);

  public abstract T handleDeclaration(HiddenTokenAwareTree token);

  public abstract T handleFontFace(HiddenTokenAwareTree token);

  public abstract T handleCharsetDeclaration(HiddenTokenAwareTree token);

  public abstract T handleIdSelector(HiddenTokenAwareTree token);

  public abstract T handleSelectorAttribute(HiddenTokenAwareTree token);

  public abstract T handleSelectorOperator(HiddenTokenAwareTree token);

  public abstract T handlePseudo(HiddenTokenAwareTree token);

  public abstract T handleCssClass(HiddenTokenAwareTree token);

  public abstract T handleStyleSheet(HiddenTokenAwareTree token);

  public abstract T handleSelector(HiddenTokenAwareTree token);

  public abstract T handleRuleSet(HiddenTokenAwareTree token);

  public T handleEOF(HiddenTokenAwareTree token) {
    return null;
  }

}
