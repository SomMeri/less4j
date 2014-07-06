package com.github.sommeri.less4j.core.compiler.expressions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.LessSource.CannotReadFile;
import com.github.sommeri.less4j.LessSource.FileNotFound;
import com.github.sommeri.less4j.LessSource.StringSourceException;
import com.github.sommeri.less4j.core.ast.ASTCssNodeType;
import com.github.sommeri.less4j.core.ast.ColorExpression;
import com.github.sommeri.less4j.core.ast.CssString;
import com.github.sommeri.less4j.core.ast.Expression;
import com.github.sommeri.less4j.core.ast.FaultyExpression;
import com.github.sommeri.less4j.core.ast.FunctionExpression;
import com.github.sommeri.less4j.core.ast.IdentifierExpression;
import com.github.sommeri.less4j.core.ast.ListExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression.Dimension;
import com.github.sommeri.less4j.core.parser.ConversionUtils;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.nodemime.NodeMime;
import com.github.sommeri.less4j.utils.PrintUtils;

public class MiscFunctions extends BuiltInFunctionsPack {

  protected static final String COLOR = "color";
  protected static final String UNIT = "unit";
  protected static final String GET_UNIT = "get-unit";
  protected static final String CONVERT = "convert";
  protected static final String EXTRACT = "extract";
  protected static final String DATA_URI = "data-uri";

  private static Map<String, Function> FUNCTIONS = new HashMap<String, Function>();
  static {
    FUNCTIONS.put(COLOR, new Color());
    FUNCTIONS.put(UNIT, new Unit());
    FUNCTIONS.put(GET_UNIT, new GetUnit());
    FUNCTIONS.put(CONVERT, new Convert());
    FUNCTIONS.put(EXTRACT, new Extract());
    FUNCTIONS.put(DATA_URI, new DataUri());
  }

  public MiscFunctions(ProblemsHandler problemsHandler) {
    super(problemsHandler);
  }

  @Override
  protected Map<String, Function> getFunctions() {
    return FUNCTIONS;
  }

}

class Color extends CatchAllMultiParameterFunction {

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, FunctionExpression functionCall, HiddenTokenAwareTree token) {
    CssString string = (CssString) splitParameters.get(0);
    String text = string.getValue();

    //this does a bit more then less.js: it is able to parse named colors
    ColorExpression parsedColor = ConversionUtils.parseColor(token, text);
    if (parsedColor == null) {
      FaultyExpression faultyExpression = new FaultyExpression(token);
      problemsHandler.notAColor(faultyExpression, text);
    }

    return parsedColor;
  }

  @Override
  protected int getMinParameters() {
    return 1;
  }

  @Override
  protected int getMaxParameters() {
    return 1;
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.STRING_EXPRESSION);
  }

  @Override
  protected String getName() {
    return MiscFunctions.COLOR;
  }

}

class Unit extends CatchAllMultiParameterFunction {

  private TypesConversionUtils conversionUtils = new TypesConversionUtils();

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, FunctionExpression functionCall, HiddenTokenAwareTree token) {
    NumberExpression dimension = (NumberExpression) splitParameters.get(0);
    String unit = splitParameters.size() > 1 ? conversionUtils.contentToString(splitParameters.get(1)) : null;

    String newSuffix;
    Dimension newDimension;
    if (unit != null) {
      newSuffix = unit;
      newDimension = Dimension.forSuffix(newSuffix);
    } else {
      newSuffix = "";
      newDimension = Dimension.NUMBER;
    }

    return new NumberExpression(token, dimension.getValueAsDouble(), newSuffix, null, newDimension);
  }

  @Override
  protected int getMinParameters() {
    return 1;
  }

  @Override
  protected int getMaxParameters() {
    return 2;
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    switch (position) {
    case 0:
      return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.NUMBER);
    case 1:
      return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.IDENTIFIER_EXPRESSION, ASTCssNodeType.STRING_EXPRESSION, ASTCssNodeType.ESCAPED_VALUE);
    }
    return false;
  }

  @Override
  protected String getName() {
    return MiscFunctions.UNIT;
  }

}

class GetUnit extends CatchAllMultiParameterFunction {

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, FunctionExpression functionCall, HiddenTokenAwareTree token) {
    NumberExpression dimension = (NumberExpression) splitParameters.get(0);
    return new IdentifierExpression(token, dimension.getSuffix()); //not sure about the type
  }

  @Override
  protected int getMinParameters() {
    return 1;
  }

  @Override
  protected int getMaxParameters() {
    return 1;
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    switch (position) {
    case 0:
      return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.NUMBER);
    }
    return false;
  }

  @Override
  protected String getName() {
    return MiscFunctions.GET_UNIT;
  }

}

class Convert extends CatchAllMultiParameterFunction {

  private TypesConversionUtils conversionUtils = new TypesConversionUtils();

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, FunctionExpression functionCall, HiddenTokenAwareTree token) {
    NumberExpression value = (NumberExpression) splitParameters.get(0);
    return value.convertTo(conversionUtils.contentToString(splitParameters.get(1)));
  }

  @Override
  protected int getMinParameters() {
    return 2;
  }

  @Override
  protected int getMaxParameters() {
    return 2;
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    switch (position) {
    case 0:
      return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.NUMBER);
    case 1:
      return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.IDENTIFIER_EXPRESSION, ASTCssNodeType.STRING_EXPRESSION, ASTCssNodeType.ESCAPED_VALUE);
    }
    return false;
  }

  @Override
  protected String getName() {
    return MiscFunctions.CONVERT;
  }

}

class Extract extends CatchAllMultiParameterFunction {

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, FunctionExpression functionCall, HiddenTokenAwareTree token) {
    List<Expression> values = collect((ListExpression) splitParameters.get(0));
    NumberExpression index = (NumberExpression) splitParameters.get(1);
    return values.get(index.getValueAsDouble().intValue() - 1);
  }

  private List<Expression> collect(ListExpression values) {
    return values.getExpressions();
  }

  @Override
  protected int getMinParameters() {
    return 2;
  }

  @Override
  protected int getMaxParameters() {
    return 2;
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    switch (position) {
    case 0:
      return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.LIST_EXPRESSION);
    case 1:
      return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.NUMBER);
    }
    return false;
  }

  @Override
  protected String getName() {
    return MiscFunctions.EXTRACT;
  }

}

class DataUri extends CatchAllMultiParameterFunction {

  private NodeMime mime = new NodeMime();
  private static final int DATA_URI_MAX_KB = 32;

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, FunctionExpression functionCall, HiddenTokenAwareTree token) {
    String mimetype = null;
    String filename = null;
    if (splitParameters.size() == 1) {
      CssString filenameArg = (CssString) splitParameters.get(0);
      filename = filenameArg.getValue();
      
      mimetype = guessMimetype(filename);
    } else {
      CssString mimetypeArg = (CssString) splitParameters.get(0);
      mimetype = mimetypeArg.getValue();

      CssString filenameArg = (CssString) splitParameters.get(1);
      filename = filenameArg.getValue();
    }

    LessSource source = token.getSource();
    try {

      LessSource dataSource = source.relativeSource(filename);
      byte[] data = dataSource.getBytes();

      // **** less.js comment - flag is not implemented yet ****
      // IE8 cannot handle a data-uri larger than 32KB. If this is exceeded
      // and the --ieCompat flag is enabled, return a normal url() instead.
      int fileSizeInKB = data.length/1024;
      if (fileSizeInKB >=DATA_URI_MAX_KB) {
        problemsHandler.warnIE8UnsafeDataUri(functionCall, filename, fileSizeInKB, DATA_URI_MAX_KB);
        FunctionExpression result = new FunctionExpression(token, "url", functionCall.getParameter().clone());
        result.configureParentToAllChilds();
        return result;
      }
      
      return toDataUri(token, mimetype, data);

    } catch (FileNotFound ex) {
      problemsHandler.errorFileNotFound(functionCall, filename);
      return new FaultyExpression(functionCall.getUnderlyingStructure());
    } catch (CannotReadFile e) {
      problemsHandler.errorFileCanNotBeRead(functionCall, filename);
      return new FaultyExpression(functionCall.getUnderlyingStructure());
    } catch (StringSourceException ex) {
      // imports are relative to current file and we do not know its location
      problemsHandler.errorFileReferenceNoBaseDirectory(functionCall, filename);
      return new FaultyExpression(functionCall.getUnderlyingStructure());
    }

  }

  private String guessMimetype(String filename) {
    String mimetype;
    mimetype = mime.lookupMime(filename);
    if (!mime.isText(mimetype)) {
      mimetype += ";base64";
    }
    return mimetype;
  }

  private Expression toDataUri(HiddenTokenAwareTree token, String mimetype, byte[] data) {
    if (mimetype != null && mimetype.toLowerCase().endsWith("base64"))
      return toDataUri(token, mimetype, PrintUtils.base64Encode(data));
    else
      return toDataUri(token, mimetype, PrintUtils.toUtf8AsUri(new String(data)));
  }

  private Expression toDataUri(HiddenTokenAwareTree token, String mimetype, String data) {
    StringBuilder value = new StringBuilder("data:");
    value.append(mimetype).append(",").append(data);

    CssString parameter = new CssString(token, value.toString(), "\""); 
    return new FunctionExpression(token, "url", parameter);
  }

  @Override
  protected int getMinParameters() {
    return 1;
  }

  @Override
  protected int getMaxParameters() {
    return 2;
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    return validateParameterTypeReportError(parameter, problemsHandler, ASTCssNodeType.STRING_EXPRESSION);
  }

  @Override
  protected String getName() {
    return MiscFunctions.DATA_URI;
  }

}
