package com.github.sommeri.less4j.core.compiler.expressions;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;

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
import com.github.sommeri.less4j.core.ast.ListExpressionOperator;
import com.github.sommeri.less4j.core.ast.NumberExpression;
import com.github.sommeri.less4j.core.ast.NumberExpression.Dimension;
import com.github.sommeri.less4j.core.parser.ConversionUtils;
import com.github.sommeri.less4j.core.parser.HiddenTokenAwareTree;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;
import com.github.sommeri.less4j.nodemime.NodeMime;
import com.github.sommeri.less4j.utils.InStringCssPrinter;
import com.github.sommeri.less4j.utils.PrintUtils;

public class MiscFunctions extends BuiltInFunctionsPack {

  protected static final String COLOR = "color";
  protected static final String UNIT = "unit";
  protected static final String GET_UNIT = "get-unit";
  protected static final String CONVERT = "convert";
  protected static final String EXTRACT = "extract";
  protected static final String DATA_URI = "data-uri";
  protected static final String IMAGE_SIZE = "image-size";
  protected static final String IMAGE_WIDTH = "image-width";
  protected static final String IMAGE_HEIGHT = "image-height";
  protected static final String SVG_GRADIENT = "svg-gradient";

  private static Map<String, Function> FUNCTIONS = new HashMap<String, Function>();
  static {
    FUNCTIONS.put(COLOR, new Color());
    FUNCTIONS.put(UNIT, new Unit());
    FUNCTIONS.put(GET_UNIT, new GetUnit());
    FUNCTIONS.put(CONVERT, new Convert());
    FUNCTIONS.put(EXTRACT, new Extract());
    FUNCTIONS.put(DATA_URI, new DataUri());
    FUNCTIONS.put(IMAGE_SIZE, new ImageSize());
    FUNCTIONS.put(IMAGE_WIDTH, new ImageWidth());
    FUNCTIONS.put(IMAGE_HEIGHT, new ImageHeight());
    FUNCTIONS.put(SVG_GRADIENT, new SvgGradient());
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

    // this does a bit more then less.js: it is able to parse named colors
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
    return new IdentifierExpression(token, dimension.getSuffix()); // not sure
                                                                   // about the
                                                                   // type
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
    } else {
      CssString mimetypeArg = (CssString) splitParameters.get(0);
      mimetype = mimetypeArg.getValue();

      CssString filenameArg = (CssString) splitParameters.get(1);
      filename = filenameArg.getValue();
    }

    String[] filenameParts = filename.split("#", 2);
    filename = filenameParts[0];
    String fragments = filenameParts.length > 1 ? "#" + filenameParts[1] : "";

    if (mimetype == null)
      mimetype = guessMimetype(filename);

    LessSource source = token.getSource();
    try {
      LessSource dataSource = source.relativeSource(filename);
      byte[] data = dataSource.getBytes();

      String encodedData = encodeDataUri(mimetype, data);
      // **** less.js comment - flag is not implemented yet ****
      // IE8 cannot handle a data-uri larger than 32KB. If this is exceeded
      // and the --ieCompat flag is enabled, return a normal url() instead.
      int encodedSizeInKB = encodedData.length() / 1024;
      if (encodedSizeInKB >= DATA_URI_MAX_KB) {
        problemsHandler.warnIE8UnsafeDataUri(functionCall, filename, encodedSizeInKB, DATA_URI_MAX_KB);
        FunctionExpression result = new FunctionExpression(token, "url", functionCall.getParameter().clone());
        result.configureParentToAllChilds();
        return result;
      }

      return toDataUri(token, mimetype, encodedData, fragments);

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
    String charset = mime.lookupCharset(mimetype);
    if (!textCharset(charset) && !isSvg(mimetype)) {
      mimetype += ";base64";
    }
    return mimetype;
  }

  private boolean isSvg(String mimetype) {
    return "image/svg+xml".equals(mimetype);
  }

  private String encodeDataUri(String mimetype, byte[] data) {
    if (mimetype != null && mimetype.toLowerCase().endsWith("base64")) {
      return PrintUtils.base64Encode(data);
    } else {
      return PrintUtils.toUtf8AsUri(new String(data));
    }
  }

  private boolean textCharset(String charset) {
    return "UTF-8".equals(charset) || "US-ASCII".equals(charset);
  }

  private Expression toDataUri(HiddenTokenAwareTree token, String mimetype, String data, String fragments) {
    StringBuilder value = new StringBuilder("data:");
    value.append(mimetype).append(",").append(data).append(fragments);

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

class ImageSize extends CatchAllMultiParameterFunction {

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, FunctionExpression functionCall, HiddenTokenAwareTree token) {
    CssString filenameArg = (CssString) splitParameters.get(0);
    String filename = filenameArg.getValue();

    LessSource source = token.getSource();
    try {
      LessSource dataSource = source.relativeSource(filename);
      byte[] data = dataSource.getBytes();

      BufferedImage image = ImageIO.read(new ByteArrayInputStream(data));
      if (image==null) {
        problemsHandler.errorUnknownImageFileFormat(functionCall, filename);
        return new FaultyExpression(functionCall.getUnderlyingStructure());
      }
      
      int width = image.getWidth();
      int height = image.getHeight();
      return toSizeNumber(functionCall.getUnderlyingStructure(), width, height);
      
    } catch (FileNotFound ex) {
      problemsHandler.errorFileNotFound(functionCall, filename);
      return new FaultyExpression(functionCall.getUnderlyingStructure());
    } catch (CannotReadFile e) {
      problemsHandler.errorFileCanNotBeRead(functionCall, filename);
      return new FaultyExpression(functionCall.getUnderlyingStructure());
    } catch (IOException e) {
      problemsHandler.errorFileCanNotBeRead(functionCall, filename);
      return new FaultyExpression(functionCall.getUnderlyingStructure());
    } catch (StringSourceException ex) {
      // imports are relative to current file and we do not know its location
      problemsHandler.errorFileReferenceNoBaseDirectory(functionCall, filename);
      return new FaultyExpression(functionCall.getUnderlyingStructure());
    }

  }

  protected Expression toSizeNumber(HiddenTokenAwareTree token, int width, int height) {
    Expression widthExp = toPixels(token, width);
    Expression heightExp = toPixels(token, height);
    
    return new ListExpression(token, Arrays.asList(widthExp, heightExp), new ListExpressionOperator(token, ListExpressionOperator.Operator.EMPTY_OPERATOR));
  }

  protected Expression toPixels(HiddenTokenAwareTree token, int width) {
    return new NumberExpression(token, (double ) width, "px", null, Dimension.LENGTH);
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
    return MiscFunctions.IMAGE_SIZE;
  }

}

class ImageWidth extends ImageSize {

  protected Expression toSizeNumber(HiddenTokenAwareTree token, int width, int height) {
    Expression widthExp = toPixels(token, width);
    
    return widthExp;
  }

  @Override
  protected String getName() {
    return MiscFunctions.IMAGE_WIDTH;
  }

}

class ImageHeight extends ImageSize {

  protected Expression toSizeNumber(HiddenTokenAwareTree token, int width, int height) {
    Expression heightExp = toPixels(token, height);
    
    return heightExp;
  }

  @Override
  protected String getName() {
    return MiscFunctions.IMAGE_HEIGHT;
  }

}

class SvgGradient extends CatchAllMultiParameterFunction {

  private final TypesConversionUtils conversions = new TypesConversionUtils();

  @Override
  protected Expression evaluate(List<Expression> splitParameters, ProblemsHandler problemsHandler, FunctionExpression functionCall, HiddenTokenAwareTree token) {
    String direction = toDirection(splitParameters.get(0)), gradientDirectionSvg = "";
    List<Expression> stops = extractStops(splitParameters);
    if (stops == null || stops.size() < 2) {
      problemsHandler.errorSvgGradientArgument(functionCall);
      return new FaultyExpression(functionCall.getUnderlyingStructure());
    }

    String gradientType = "linear", rectangleDimension = "x=\"0\" y=\"0\" width=\"1\" height=\"1\"";
    boolean useBase64 = true;
    if ("to bottom".equals(direction)) {
      gradientDirectionSvg = "x1=\"0%\" y1=\"0%\" x2=\"0%\" y2=\"100%\"";
    } else if ("to right".equals(direction)) {
      gradientDirectionSvg = "x1=\"0%\" y1=\"0%\" x2=\"100%\" y2=\"0%\"";
    } else if ("to bottom right".equals(direction)) {
      gradientDirectionSvg = "x1=\"0%\" y1=\"0%\" x2=\"100%\" y2=\"100%\"";
    } else if ("to top right".equals(direction)) {
      gradientDirectionSvg = "x1=\"0%\" y1=\"100%\" x2=\"100%\" y2=\"0%\"";
    } else if (direction != null && direction.startsWith("ellipse")) {
      gradientType = "radial";
      gradientDirectionSvg = "cx=\"50%\" cy=\"50%\" r=\"75%\"";
      rectangleDimension = "x=\"-50\" y=\"-50\" width=\"101\" height=\"101\"";
    } else {
      problemsHandler.wrongEnumeratedArgument(functionCall, "direction", "to bottom", "to right", "to bottom right", "to top right", "ellipse", "ellipse at center");
      return new FaultyExpression(functionCall);
    }

    StringBuilder returner = new StringBuilder("<?xml version=\"1.0\" ?>");
    returner.append("<svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\" width=\"100%\" height=\"100%\" viewBox=\"0 0 1 1\" preserveAspectRatio=\"none\">");
    returner.append("<");
    returner.append(gradientType);
    returner.append("Gradient id=\"gradient\" gradientUnits=\"userSpaceOnUse\" ");
    returner.append(gradientDirectionSvg);
    returner.append(">");

    Iterator<Expression> iterator = stops.iterator();
    boolean isFirstStop = true;
    while (iterator.hasNext()) {
      Expression stop = iterator.next();
      if (!addColorStop(returner, stop, isFirstStop, !iterator.hasNext(), functionCall, problemsHandler)) {
        problemsHandler.errorSvgGradientArgument(functionCall);
        return new FaultyExpression(functionCall);
      }
      isFirstStop = false;
    }

    returner.append("</").append(gradientType).append("Gradient>");
    returner.append("<rect ").append(rectangleDimension).append(" fill=\"url(#gradient)\" /></svg>");

    String result = useBase64 ? PrintUtils.base64Encode(returner.toString().getBytes()) : returner.toString();
    return toDataUri(functionCall.getUnderlyingStructure(), result, useBase64);
  }

  private List<Expression> extractStops(List<Expression> splitParameters) {
    if (splitParameters.size() == 2) {
      Expression expression = splitParameters.get(1);
      if (ASTCssNodeType.LIST_EXPRESSION == expression.getType()) {
        ListExpression list = (ListExpression) expression;
        return list.getExpressions();
      } else {
        return null;
      }
    }
    return splitParameters.subList(1, splitParameters.size());
  }

  private Expression toDataUri(HiddenTokenAwareTree token, String data, boolean useBase64) {
    StringBuilder value = new StringBuilder("data:image/svg+xml");
    if (useBase64)
      value.append(";base64");
    value.append(",").append(data);

    CssString parameter = new CssString(token, value.toString(), "\'");
    return new FunctionExpression(token, "url", parameter);
  }

  private boolean addColorStop(StringBuilder returner, Expression colorStop, boolean isFirst, boolean isLast, FunctionExpression errorNode, ProblemsHandler problemsHandler) {
    if (colorStop.getType() == ASTCssNodeType.LIST_EXPRESSION) {
      ListExpression list = (ListExpression) colorStop;
      List<Expression> expressions = list.getExpressions();
      if (expressions.isEmpty() || expressions.size() > 2) {
        return false;
      }

      Expression color = expressions.get(0);
      Expression position = expressions.size() > 1 ? expressions.get(1) : null;
      if (!addColorStop(returner, color, position, isFirst, isLast, errorNode, problemsHandler))
        return false;
    } else {
      if (!addColorStop(returner, colorStop, null, isFirst, isLast, errorNode, problemsHandler))
        return false;
    }
    return true;
  }

  private boolean addColorStop(StringBuilder returner, Expression colorE, Expression position, boolean isFirst, boolean isLast, FunctionExpression errorNode, ProblemsHandler problemsHandler) {
    if (colorE.getType() != ASTCssNodeType.COLOR_EXPRESSION) {
      problemsHandler.errorSvgGradientArgument(errorNode);
      return false;
    }
    if (!isLast && !isFirst && position == null) {
      problemsHandler.errorSvgGradientArgument(errorNode);
      return false;
    }

    ColorExpression color = (ColorExpression) colorE;
    String positionValue = position != null ? toCss(position) : isFirst ? "0%" : "100%";
    returner.append("<stop offset=\"").append(positionValue);
    returner.append("\" stop-color=\"").append(color.getValueInHexadecimal());
    returner.append("\"");
    if (color.hasAlpha()) {
      returner.append(" stop-opacity=\"").append(PrintUtils.formatNumber(color.getAlpha())).append("\"");
    }
    returner.append("/>");
    return true;
  }

  private String toDirection(Expression direction) {
    String result = conversions.contentToString(direction);
    if (result != null)
      return result;

    return toCss(direction);
  }

  private String toCss(Expression direction) {
    InStringCssPrinter printer = new InStringCssPrinter();
    printer.append(direction);
    return printer.toString();
  }

  @Override
  protected boolean validateParameter(Expression parameter, int position, ProblemsHandler problemsHandler) {
    // TODO Auto-generated method stub
    return true;
  }

  @Override
  protected int getMinParameters() {
    return 2;
  }

  @Override
  protected int getMaxParameters() {
    return Integer.MAX_VALUE;
  }

  @Override
  protected String getName() {
    return MiscFunctions.SVG_GRADIENT;
  }

}
