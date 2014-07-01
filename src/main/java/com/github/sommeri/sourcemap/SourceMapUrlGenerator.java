package com.github.sommeri.sourcemap;

/**
 * A generator for the value of <em>sourceMappingUrl</em> special comment
 * for CSS source maps.
 */
public interface SourceMapUrlGenerator {

  /**
   * Generates the url location of the source map file
   *
   * @param cssResultLocation The target location of the generated CSS file
   * @return The url to the source map file
   */
  String generateUrl(String cssResultLocation);
}
