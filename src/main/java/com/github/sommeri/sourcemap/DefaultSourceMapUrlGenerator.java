package com.github.sommeri.sourcemap;

import com.github.sommeri.less4j.platform.Constants;
import com.github.sommeri.less4j.utils.URIUtils;

/**
 * An implementation of SourceMapUrlGenerator that just replaces the
 * extension of the CSS file to <em>.css.map</em>
 */
public class DefaultSourceMapUrlGenerator implements SourceMapUrlGenerator {

  @Override
  public String generateUrl(String cssResultLocation) {
    return URIUtils.addSuffix(cssResultLocation, Constants.SOURCE_MAP_SUFFIX);
  }
}
