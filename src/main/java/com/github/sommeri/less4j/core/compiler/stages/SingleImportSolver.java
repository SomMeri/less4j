package com.github.sommeri.less4j.core.compiler.stages;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.github.sommeri.less4j.LessCompiler.Configuration;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.LessSource.CannotReadFile;
import com.github.sommeri.less4j.LessSource.FileNotFound;
import com.github.sommeri.less4j.LessSource.StringSourceException;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.ast.FaultyNode;
import com.github.sommeri.less4j.core.ast.GeneralBody;
import com.github.sommeri.less4j.core.ast.Import;
import com.github.sommeri.less4j.core.ast.Import.ImportContent;
import com.github.sommeri.less4j.core.ast.Import.ImportMultiplicity;
import com.github.sommeri.less4j.core.ast.InlineContent;
import com.github.sommeri.less4j.core.ast.Media;
import com.github.sommeri.less4j.core.ast.StyleSheet;
import com.github.sommeri.less4j.core.compiler.expressions.TypesConversionUtils;
import com.github.sommeri.less4j.core.parser.ANTLRParser;
import com.github.sommeri.less4j.core.parser.ASTBuilder;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class SingleImportSolver {

  private final ProblemsHandler problemsHandler;
  private final Configuration configuration;
  private TypesConversionUtils conversionUtils = new TypesConversionUtils();
  private ASTManipulator astManipulator = new ASTManipulator();

  private Map<LessSource, StyleSheet> astCache = new HashMap<LessSource, StyleSheet>();
  
  public SingleImportSolver(ProblemsHandler problemsHandler, Configuration configuration) {
    this.problemsHandler = problemsHandler;
    this.configuration = configuration;
  }

  public ASTCssNode importEncountered(Import node, LessSource source, AlreadyImportedSources alreadyImportedSources) {
    String filename = conversionUtils.extractFilename(node.getUrlExpression(), problemsHandler, configuration);
    if (filename == null) {
      problemsHandler.errorWrongImport(node.getUrlExpression());
      return null;
    }
    String urlParams = "";
    int paramsIndx = filename.lastIndexOf("?");
    if (paramsIndx != -1) {
      urlParams = filename.substring(paramsIndx);
      filename = filename.substring(0, paramsIndx);
    }

    // css file imports should be left as they are
    // FIXME ! they should be relativized
    if (!node.isInline() && treatAsCss(node, filename))
      return null; 

    filename = addLessSuffixIfNeeded(filename, urlParams);
    LessSource importedSource;
    try {
      importedSource = source.relativeSource(filename);
    } catch (FileNotFound ex) {
      return importFileNotFound(node, filename);
    } catch (CannotReadFile e) {
      problemsHandler.errorFileCanNotBeRead(node, filename);
      return null;
    } catch (StringSourceException ex) {
      // imports are relative to current file and we do not know its location
      problemsHandler.warnLessImportNoBaseDirectory(node.getUrlExpression());
      return null;
    }

    // import once should not import a file that was already imported
    if (node.isImportOnce() && alreadyImportedSources.alreadyVisited(importedSource)) {
      System.out.println("removed: " + node + " inside: " + node.getSource().getName());
      astManipulator.removeFromBody(node);
      return null;
    }

    String importedContent;
    try {
      importedContent = importedSource.getContent();
      alreadyImportedSources.add(importedSource);
    } catch (FileNotFound e) {
      return importFileNotFound(node, filename);
    } catch (CannotReadFile e) {
      problemsHandler.errorFileCanNotBeRead(node, filename);
      return null;
    }

    if (node.isInline()) {
      return replaceByInlineValue(node, importedContent);
    }
    
    StyleSheet importedAst = buildImportedAst(node, importedSource, importedContent);
    
    if (node.isReferenceOnly() || node.isSilent()) {
      astManipulator.setTreeSilentness(importedAst, true);
    }
    astManipulator.replaceInBody(node, importedAst.getChilds());
    return importedAst;
  }

  private ASTCssNode replaceByInlineValue(Import node, String importedContent) {
    HiddenTokenAwareTree underlyingStructure = node.getUnderlyingStructure();
    StyleSheet result = new StyleSheet(underlyingStructure);
    InlineContent content = new InlineContent(underlyingStructure, importedContent);
    result.addMember(content);
    result.configureParentToAllChilds();
    
    astManipulator.replaceInBody(node, content);
    return result;
  }

  private ASTCssNode importFileNotFound(Import node, String filename) {
    if (!node.isOptional()) {
      problemsHandler.errorFileNotFound(node, filename);
      return null;
    }
    return replaceByInlineValue(node, "");
  }

  private static final String TEST = "c:/data/meri/less4java/srot/semantic-ui/Semantic-UI-LESS/definitions/views/../../themes/default/globals/site.variables";

  private StyleSheet buildImportedAst(Import node, LessSource source, String content) {
//    String uriStr = source.getURI().toString();
//    if (TEST.equals(uriStr)) {
//      System.out.println(source.hashCode() + " " + uriStr);
//      System.out.println("cache info: " + astCache.size());
//      for (Entry<LessSource, StyleSheet> entry : astCache.entrySet()) {
//        LessSource key = entry.getKey();
//        System.out.println(" ** " + key.equals(source));
//      }
//    }
//    if (astCache.containsKey(source)) {
//      System.out.println("Cached: " + uriStr);
//      return astCache.get(source).clone();
//    }
    //System.out.println("New one: " + uri);
//    System.out.println("\"" + uriStr + "\", ");

    // parse imported file
    StyleSheet importedAst = parseContent(node, content, source);

//    astCache.put(source, importedAst.clone());
    
    // add media queries if needed
    if (node.hasMediums()) {
      HiddenTokenAwareTree underlyingStructure = node.getUnderlyingStructure();
      StyleSheet result = new StyleSheet(underlyingStructure);
      Media media = new Media(underlyingStructure);
      result.addMember(media);
      media.setParent(result);
      media.setMediums(node.getMediums());
      GeneralBody mediaBody = new GeneralBody(underlyingStructure, importedAst.getMembers());
      media.setBody(mediaBody);
      media.configureParentToAllChilds();
      mediaBody.configureParentToAllChilds();
      return result;
    } 

    return importedAst;
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

  private String addLessSuffixIfNeeded(String filename, String urlParams) {
    if ((new File(filename)).getName().contains("."))
      return filename;

    return filename + ".less" + urlParams;
  }

  private boolean treatAsCss(Import node, String filename) {
    ImportContent contentKind = node.getContentKind();
    return contentKind==ImportContent.CSS || (contentKind==ImportContent.SUFFIX_BASED && isCssFile(filename));
  }

  private boolean isCssFile(String filename) {
    String lowerCase = filename.toLowerCase();
    return lowerCase.endsWith(".css") || lowerCase.endsWith("/css");
  }

  public static class AlreadyImportedSources {
    private Set<LessSource> sourcesThatCount = new HashSet<LessSource>();
    private Set<LessSource> allImportedSources;

    public AlreadyImportedSources(Set<LessSource> allImportedSources) {
      this.allImportedSources = allImportedSources;
    }

    public void add(LessSource importedSource) {
      sourcesThatCount.add(importedSource);
      allImportedSources.add(importedSource);
    }
    
    public boolean alreadyVisited(LessSource importedSource) {
      boolean result = sourcesThatCount.contains(importedSource);
      return result;
    }
    
  }
}
