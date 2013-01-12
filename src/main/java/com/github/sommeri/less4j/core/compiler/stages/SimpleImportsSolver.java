package com.github.sommeri.less4j.core.compiler.stages;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FaultyNode;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.Import;
import com.github.sommeri.less4j.core.ast.Import.ImportKind;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.compiler.expressions.ConversionUtils;
import com.github.sommeri.less4j.core.compiler.expressions.ExpressionEvaluator;
import com.github.sommeri.less4j.core.parser.ANTLRParser;
import com.github.sommeri.less4j.core.parser.ASTBuilder;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.platform.Constants;

public class SimpleImportsSolver {

  private final ProblemsHandler problemsHandler;
  private ConversionUtils conversionUtils = new ConversionUtils();
  private ASTManipulator astManipulator = new ASTManipulator();
  
  private Set<File> importedFiles = new HashSet<File>();

  public SimpleImportsSolver(ProblemsHandler problemsHandler) {
    this.problemsHandler = problemsHandler;
  }

  public void solveImports(StyleSheet node, File baseDirectory) {
    doSolveImports(node, baseDirectory);
  }

  private void doSolveImports(StyleSheet node, File baseDirectory) {
    List<ASTCssNode> childs = new ArrayList<ASTCssNode>(node.getChilds());
    for (ASTCssNode kid : childs) {
      if (kid.getType() == ASTCssNodeType.IMPORT) {
        //FIXME: add test case on what happen in less.js in case of two imports if the first one is rewriting variable containing url for the second one 
        importEncountered((Import) kid, baseDirectory);
      }
    }

  }

  private void importEncountered(Import node, File baseDirectory) {
    String filename = evaluateFilename(node.getUrlExpression());
    if (filename == null) { //FIXME: document limitations
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

    if (baseDirectory==null) {
      // imports are relative to current file and we do not know its location   
      problemsHandler.warnLessImportNoBaseDirectory(node.getUrlExpression());
      return ;
    }
    
    // add .less suffix if needed
    filename = normalizeFileName(filename, urlParams);
    File importedFile = new File(baseDirectory, filename);
    if (isImportOnce(node) && alreadyVisited(importedFile)) {
      astManipulator.removeFromBody(node);
      return ;
    }
    
    String importedContent = loadFile(node, importedFile, filename);
    if (importedContent == null)
      return;

    importedFiles.add(importedFile.getAbsoluteFile());
    StyleSheet importedAst = parseContent(node, importedContent, importedFile);
    solveImports(importedAst, importedFile.getParentFile());
    astManipulator.replaceInBody(node, importedAst.getChilds());
  }

  private boolean isImportOnce(Import node) {
    return node.getKind()==ImportKind.IMPORT_ONCE;
  }

  private boolean alreadyVisited(File importedFile) {
    return importedFiles.contains(importedFile.getAbsoluteFile());
  }

  private StyleSheet parseContent(Import importNode, String importedContent, File importedFile) {
    ANTLRParser parser = new ANTLRParser();
    ANTLRParser.ParseResult parsedSheet = parser.parseStyleSheet(importedContent, importedFile);
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
    if (filename.toLowerCase().endsWith(".less"))
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
      return toJavaFileSeparator(conversionUtils.toStringContent(urlExpression));

    //this is the only place in the compiler that can interpret the url function
    //TODO: ! may there should be a special AST construct for uri. I will see when I will start to play with variables as URI parameters.
    FunctionExpression function = (FunctionExpression) urlExpression;
    if (!"url".equals(function.getName().toLowerCase()))
      return null;

    return toJavaFileSeparator(conversionUtils.toStringContent(expressionEvaluator.evaluate(function.getParameter())));
  }

  private String toJavaFileSeparator(String path) {
    if (Constants.FILE_SEPARATOR.equals("/")) {
      return path;
    }
    
    return path.replace(Constants.FILE_SEPARATOR, "/");
  }

  private String loadFile(Import node, File inputFile, String filename) {
    if (!inputFile.exists()) {
      problemsHandler.errorImportedFileNotFound(node, filename);
      return null;
    }
    if (!inputFile.canRead()) {
      problemsHandler.errorImportedFileCanNotBeRead(node, filename);
      return null;
    }
    String content;
    try {
      FileReader input = new FileReader(inputFile);
      content = IOUtils.toString(input).replace("\r\n", "\n");
      input.close();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }

    return content;
  }

}
