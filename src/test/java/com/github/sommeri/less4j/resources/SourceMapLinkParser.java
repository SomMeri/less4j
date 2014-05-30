package com.github.sommeri.less4j.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.DatatypeConverter;

import com.github.sommeri.less4j.LessCompiler.CompilationResult;

public class SourceMapLinkParser {

  public static final Pattern EXTRACT_MAP_LINK = Pattern.compile("/\\*# sourceMappingURL=.*", Pattern.DOTALL);

  public static String extractAttachedSourceMap(String css) {
    Matcher matcher = EXTRACT_MAP_LINK.matcher(css);
    if (!matcher.find())
      fail("Source map is missing.");

    String sourceMapComment = matcher.group();
    String encodedSourceMap = sourceMapComment.substring(sourceMapComment.lastIndexOf(",") + 1, sourceMapComment.lastIndexOf(" */") + 1);
    String sourceMap = new String(DatatypeConverter.parseBase64Binary(encodedSourceMap));
    return sourceMap;
  }

  public static void assertInlineSourceMap(CompilationResult compilationResult) {
    String attachedSourceMap = extractAttachedSourceMap(compilationResult.getCss());
    assertEquals("Attached source map should be the same as generated one.", compilationResult.getSourceMap(), attachedSourceMap);
  }

  public static void assertDoesNotLinkSourceMap(String css) {
    boolean hasLink = hasSourceMapLink(css);
    assertFalse("Generated css should NOT contain source map link.", hasLink);
}

  public static void assertLinksSourceMap(String css, String mapName) {
    assertTrue("Generated css should contain source map link.", hasSourceMapLink(css, ""));
    assertTrue("Generated css links wrong source map. Should be " + mapName + "", hasSourceMapLink(css, mapName));
  }

  public static boolean hasSourceMapLink(String css) {
    return hasSourceMapLink(css, "");
  }
  
  public static boolean hasSourceMapLink(String css, String mapName) {
    Pattern pattern = Pattern.compile("/\\*#\\s+sourceMappingURL="+mapName);
    Matcher matcher = pattern.matcher(css);
    return matcher.find();
  }

}
