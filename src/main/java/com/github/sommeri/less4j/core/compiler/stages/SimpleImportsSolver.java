package com.github.sommeri.less4j.core.compiler.stages;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.LessSource.CannotReadFile;
import com.github.sommeri.less4j.LessSource.FileNotFound;
import com.github.sommeri.less4j.LessSource.StringSourceException;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FaultyNode;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.Import;
import com.github.sommeri.less4j.core.ast.Import.ImportKind;
import com.github.sommeri.less4j.core.ast.Media;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.compiler.expressions.TypesConversionUtils;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionEvaluator;
import com.github.sommeri.less4j.core.parser.ANTLRParser;
import com.github.sommeri.less4j.core.parser.ASTBuilder;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.platform.Constants;

public class SimpleImportsSolver {

  private final ProblemsHandler problemsHandler;
  private TypesConversionUtils conversionUtils = new TypesConversionUtils();
  private ASTManipulator astManipulator = new ASTManipulator();

  private Set<LessSource> importedSources = new HashSet<LessSource>();

  public SimpleImportsSolver(ProblemsHandler problemsHandler) {
    this.problemsHandler = problemsHandler;
  }

  public void solveImports(StyleSheet node, LessSource source) {
    doSolveImports(node, source);
  }

  private void doSolveImports(StyleSheet node, LessSource source) {
    List<ASTCssNode> childs = new ArrayList<ASTCssNode>(node.getChilds());
    for (ASTCssNode kid : childs) {
      if (kid.getType() == ASTCssNodeType.IMPORT) {
        importEncountered((Import) kid, source);
      }
    }
  }

  private void importEncountered(Import node, LessSource source) {
    String filename = evaluateFilename(node.getUrlExpression());
    if (filename == null) {
      problemsHandler.errorWrongImport(node.getUrlExpression());
      return;
    }
    String urlParams = "";
    int paramsIndx = filename.lastIndexOf("?");
    if (paramsIndx != -1) {
      urlParams = filename.substring(paramsIndx);
      filename = filename.substring(0, paramsIndx);
    }

    if (isCssFile(filename))
      // css file imports should be left as they are
      return;

    // add .less suffix if needed
    filename = normalizeFileName(filename, urlParams);
    LessSource importedSource;
    try {
      importedSource = source.relativeSource(filename);
    } catch (FileNotFound ex) {
      problemsHandler.errorImportedFileNotFound(node, filename);
      return;
    } catch (CannotReadFile e) {
      problemsHandler.errorImportedFileCanNotBeRead(node, filename);
      return;
    } catch (StringSourceException ex) {
      // imports are relative to current file and we do not know its location
      problemsHandler.warnLessImportNoBaseDirectory(node.getUrlExpression());
      return;
    }

    // import once should not import a file that was already imported
    if (isImportOnce(node) && alreadyVisited(importedSource)) {
      astManipulator.removeFromBody(node);
      return;
    }
    importedSources.add(importedSource);

    String importedContent;
    try {
      importedContent = importedSource.getContent();
    } catch (FileNotFound e) {
      problemsHandler.errorImportedFileNotFound(node, filename);
      return;
    } catch (CannotReadFile e) {
      problemsHandler.errorImportedFileCanNotBeRead(node, filename);
      return;
    }

    // parse imported file
    StyleSheet importedAst = parseContent(node, importedContent, importedSource);
    solveImports(importedAst, importedSource);

    // add media queries if needed
    if (node.hasMediums()) {
      HiddenTokenAwareTree underlyingStructure = node.getUnderlyingStructure();
      Media media = new Media(underlyingStructure);
      media.setMediums(node.getMediums());
      media.setBody(new GeneralBody(underlyingStructure, importedAst.getMembers()));
      media.configureParentToAllChilds();
      astManipulator.replaceInBody(node, media);
    } else {
      astManipulator.replaceInBody(node, importedAst.getChilds());
    }
  }

  private boolean isImportOnce(Import node) {
    return node.getKind() == ImportKind.IMPORT_ONCE;
  }

  private boolean alreadyVisited(LessSource importedSource) {
    return importedSources.contains(importedSource);
  }

  private StyleSheet parseContent(Import importNode, String importedContent, LessSource source) {
    ANTLRParser parser = new ANTLRParser();
    ANTLRParser.ParseResult parsedSheet = parser.parseStyleSheet(importedContent, source);
    if (parsedSheet.hasErrors()) {
      StyleSheet result = new StyleSheet(importNode.getUnderlyingStructure());
      result.addMember(new FaultyNode(importNode));
      problemsHandler.addErrors(parsedSheet.getErrors());
      return result;
    }
    ASTBuilder astBuilder = new ASTBuilder(problemsHandler);
    StyleSheet lessStyleSheet = astBuilder.parse(parsedSheet.getTree());
    return lessStyleSheet;

  }

  private String normalizeFileName(String filename, String urlParams) {
    if (filename.toLowerCase().contains("."))
      return filename;

    return filename + ".less" + urlParams;
  }

  private boolean isCssFile(String filename) {
    return filename.toLowerCase().endsWith(".css");
  }

  private String evaluateFilename(Expression urlInput) {
    ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(problemsHandler);
    Expression urlExpression = expressionEvaluator.evaluate(urlInput);

    if (urlExpression.getType() != ASTCssNodeType.FUNCTION)
      return toJavaFileSeparator(conversionUtils.contentToString(urlExpression));

    //this is the only place in the compiler that can interpret the url function
    FunctionExpression function = (FunctionExpression) urlExpression;
    if (!"url".equals(function.getName().toLowerCase()))
      return null;

    return toJavaFileSeparator(conversionUtils.contentToString(expressionEvaluator.evaluate(function.getParameter())));
  }

  private String toJavaFileSeparator(String path) {
    if (Constants.FILE_SEPARATOR.equals("/")) {
      return path;
    }

    return path.replace(Constants.FILE_SEPARATOR, "/");
  }

}
