package com.github.sommeri.less4j.core.compiler.stages;

import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.IOUtils;

import com.github.sommeri.less4j.core.URLUtils;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FaultyNode;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.Import;
import com.github.sommeri.less4j.core.ast.Import.ImportKind;
import com.github.sommeri.less4j.core.ast.Media;
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
  
  private Set<URL> importedFiles = new HashSet<URL>();

  public SimpleImportsSolver(ProblemsHandler problemsHandler) {
    this.problemsHandler = problemsHandler;
  }

  public void solveImports(StyleSheet node, URL baseDirectory) {
    doSolveImports(node, baseDirectory);
  }

  private void doSolveImports(StyleSheet node, URL baseDirectory) {
    List<ASTCssNode> childs = new ArrayList<ASTCssNode>(node.getChilds());
    for (ASTCssNode kid : childs) {
      if (kid.getType() == ASTCssNodeType.IMPORT) {
        importEncountered((Import) kid, baseDirectory);
      }
    }

  }

  private void importEncountered(Import node, URL baseDirectory) {
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

    if (baseDirectory==null) {
      // imports are relative to current file and we do not know its location   
      problemsHandler.warnLessImportNoBaseDirectory(node.getUrlExpression());
      return ;
    }
    
    // add .less suffix if needed
    filename = normalizeFileName(filename, urlParams);
    URL importedFile;
	try {
		importedFile = new URL(baseDirectory, filename);
	} catch (MalformedURLException e) {
		throw new RuntimeException(e);
	}

    // import once should not import a file that was already imported  
    if (isImportOnce(node) && alreadyVisited(importedFile)) {
      astManipulator.removeFromBody(node);
      return ;
    }
    importedFiles.add(importedFile);
    
    String importedContent = loadFile(node, importedFile, filename);
    if (importedContent == null)
      return;

    // parse imported file
    StyleSheet importedAst = parseContent(node, importedContent, importedFile);
    solveImports(importedAst, URLUtils.toParentURL(importedFile));
    
    // add media queries if needed 
    if (node.hasMediums()) {
      Media media = new Media(node.getUnderlyingStructure());
      media.setMediums(node.getMediums());
      media.addMembers(importedAst.getMembers());
      media.configureParentToAllChilds();
      astManipulator.replaceInBody(node, media);
    } else {
      astManipulator.replaceInBody(node, importedAst.getChilds());
    }
  }

  private boolean isImportOnce(Import node) {
    return node.getKind()==ImportKind.IMPORT_ONCE;
  }

  private boolean alreadyVisited(URL importedFile) {
    return importedFiles.contains(importedFile);
  }

  private StyleSheet parseContent(Import importNode, String importedContent, URL importedFile) {
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
      return toJavaFileSeparator(conversionUtils.toStringContent(urlExpression));

    //this is the only place in the compiler that can interpret the url function
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

  private String loadFile(Import node, URL inputFile, String filename) {
//    if (!inputFile.exists()) {
//      problemsHandler.errorImportedFileNotFound(node, filename);
//      return null;
//    }
//    if (!inputFile.canRead()) {
//      problemsHandler.errorImportedFileCanNotBeRead(node, filename);
//      return null;
//    }
    String content;
    try {
    	Reader input = new InputStreamReader(inputFile.openStream());
      content = IOUtils.toString(input).replace("\r\n", "\n");
      input.close();
    } catch (Exception ex) {
      throw new RuntimeException(ex);
    }

    return content;
  }

}
