package org.porting.less4j.core.ast;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.porting.less4j.core.parser.HiddenTokenAwareTree;

//the list of colors have been taken from: http://www.w3schools.com/cssref/css_colornames.asp
//TODO: document: less.js translate color names into codes
public class NamedColorExpression extends ColorExpression {
  
  private static final Map<String, String> ALL_NAMES;
  static {
    Map<String, String> names = new HashMap<String, String>();
    names.put("aliceblue", "#f0f8ff");
    names.put("antiquewhite", "#faebd7");
    names.put("aqua", "#00ffff");
    names.put("aquamarine", "#7fffd4");
    names.put("azure", "#f0ffff");
    names.put("beige", "#f5f5dc");
    names.put("bisque", "#ffe4c4");
    names.put("black", "#000000");
    names.put("blanchedalmond", "#ffebcd");
    names.put("blue", "#0000ff");
    names.put("blueviolet", "#8a2be2");
    names.put("brown", "#a52a2a");
    names.put("burlywood", "#deb887");
    names.put("cadetblue", "#5f9ea0");
    names.put("chartreuse", "#7fff00");
    names.put("chocolate", "#d2691e");
    names.put("coral", "#ff7f50");
    names.put("cornflowerblue", "#6495ed");
    names.put("cornsilk", "#fff8dc");
    names.put("crimson", "#dc143c");
    names.put("cyan", "#00ffff");
    names.put("darkblue", "#00008b");
    names.put("darkcyan", "#008b8b");
    names.put("darkgoldenrod", "#b8860b");
    names.put("darkgray", "#a9a9a9");
    names.put("darkgrey", "#a9a9a9");
    names.put("darkgreen", "#006400");
    names.put("darkkhaki", "#bdb76b");
    names.put("darkmagenta", "#8b008b");
    names.put("darkolivegreen", "#556b2f");
    names.put("darkorange", "#ff8c00");
    names.put("darkorchid", "#9932cc");
    names.put("darkred", "#8b0000");
    names.put("darksalmon", "#e9967a");
    names.put("darkseagreen", "#8fbc8f");
    names.put("darkslateblue", "#483d8b");
    names.put("darkslategray", "#2f4f4f");
    names.put("darkslategrey", "#2f4f4f");
    names.put("darkturquoise", "#00ced1");
    names.put("darkviolet", "#9400d3");
    names.put("deeppink", "#ff1493");
    names.put("deepskyblue", "#00bfff");
    names.put("dimgray", "#696969");
    names.put("dimgrey", "#696969");
    names.put("dodgerblue", "#1e90ff");
    names.put("firebrick", "#b22222");
    names.put("floralwhite", "#fffaf0");
    names.put("forestgreen", "#228b22");
    names.put("fuchsia", "#ff00ff");
    names.put("gainsboro", "#dcdcdc");
    names.put("ghostwhite", "#f8f8ff");
    names.put("gold", "#ffd700");
    names.put("goldenrod", "#daa520");
    names.put("gray", "#808080");
    names.put("grey", "#808080");
    names.put("green", "#008000");
    names.put("greenyellow", "#adff2f");
    names.put("honeydew", "#f0fff0");
    names.put("hotpink", "#ff69b4");
    names.put("indianred ", "#cd5c5c");
    names.put("indigo ", "#4b0082");
    names.put("ivory", "#fffff0");
    names.put("khaki", "#f0e68c");
    names.put("lavender", "#e6e6fa");
    names.put("lavenderblush", "#fff0f5");
    names.put("lawngreen", "#7cfc00");
    names.put("lemonchiffon", "#fffacd");
    names.put("lightblue", "#add8e6");
    names.put("lightcoral", "#f08080");
    names.put("lightcyan", "#e0ffff");
    names.put("lightgoldenrodyellow", "#fafad2");
    names.put("lightgray", "#d3d3d3");
    names.put("lightgrey", "#d3d3d3");
    names.put("lightgreen", "#90ee90");
    names.put("lightpink", "#ffb6c1");
    names.put("lightsalmon", "#ffa07a");
    names.put("lightseagreen", "#20b2aa");
    names.put("lightskyblue", "#87cefa");
    names.put("lightslategray", "#778899");
    names.put("lightslategrey", "#778899");
    names.put("lightsteelblue", "#b0c4de");
    names.put("lightyellow", "#ffffe0");
    names.put("lime", "#00ff00");
    names.put("limegreen", "#32cd32");
    names.put("linen", "#faf0e6");
    names.put("magenta", "#ff00ff");
    names.put("maroon", "#800000");
    names.put("mediumaquamarine", "#66cdaa");
    names.put("mediumblue", "#0000cd");
    names.put("mediumorchid", "#ba55d3");
    names.put("mediumpurple", "#9370d8");
    names.put("mediumseagreen", "#3cb371");
    names.put("mediumslateblue", "#7b68ee");
    names.put("mediumspringgreen", "#00fa9a");
    names.put("mediumturquoise", "#48d1cc");
    names.put("mediumvioletred", "#c71585");
    names.put("midnightblue", "#191970");
    names.put("mintcream", "#f5fffa");
    names.put("mistyrose", "#ffe4e1");
    names.put("moccasin", "#ffe4b5");
    names.put("navajowhite", "#ffdead");
    names.put("navy", "#000080");
    names.put("oldlace", "#fdf5e6");
    names.put("olive", "#808000");
    names.put("olivedrab", "#6b8e23");
    names.put("orange", "#ffa500");
    names.put("orangered", "#ff4500");
    names.put("orchid", "#da70d6");
    names.put("palegoldenrod", "#eee8aa");
    names.put("palegreen", "#98fb98");
    names.put("paleturquoise", "#afeeee");
    names.put("palevioletred", "#d87093");
    names.put("papayawhip", "#ffefd5");
    names.put("peachpuff", "#ffdab9");
    names.put("peru", "#cd853f");
    names.put("pink", "#ffc0cb");
    names.put("plum", "#dda0dd");
    names.put("powderblue", "#b0e0e6");
    names.put("purple", "#800080");
    names.put("red", "#ff0000");
    names.put("rosybrown", "#bc8f8f");
    names.put("royalblue", "#4169e1");
    names.put("saddlebrown", "#8b4513");
    names.put("salmon", "#fa8072");
    names.put("sandybrown", "#f4a460");
    names.put("seagreen", "#2e8b57");
    names.put("seashell", "#fff5ee");
    names.put("sienna", "#a0522d");
    names.put("silver", "#c0c0c0");
    names.put("skyblue", "#87ceeb");
    names.put("slateblue", "#6a5acd");
    names.put("slategray", "#708090");
    names.put("slategrey", "#708090");
    names.put("snow", "#fffafa");
    names.put("springgreen", "#00ff7f");
    names.put("steelblue", "#4682b4");
    names.put("tan", "#d2b48c");
    names.put("teal", "#008080");
    names.put("thistle", "#d8bfd8");
    names.put("tomato", "#ff6347");
    names.put("turquoise", "#40e0d0");
    names.put("violet", "#ee82ee");
    names.put("wheat", "#f5deb3");
    names.put("white", "#ffffff");
    names.put("whitesmoke", "#f5f5f5");
    names.put("yellow", "#ffff00");
    names.put("yellowgreen", "#9acd32");
    
    ALL_NAMES = Collections.unmodifiableMap(names);
  }

  private String colorName;

  public NamedColorExpression(HiddenTokenAwareTree token, String colorName) {
    super(token, ALL_NAMES.get(colorName.toLowerCase()));
    this.colorName = colorName;
  }

  public String getColorName() {
    return colorName;
  }

  public void setColorName(String colorName) {
    this.colorName = colorName;
  }
  
  public static boolean isColorName(String value) {
    return ALL_NAMES.containsKey(value.toLowerCase());
  }

  public static Map<String, String> getAllNames() {
    return ALL_NAMES;
  }

}
