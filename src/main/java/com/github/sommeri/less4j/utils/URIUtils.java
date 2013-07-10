package com.github.sommeri.less4j.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.platform.Constants;

public class URIUtils {

  public static final String URI_FILE_SEPARATOR = "/";

  public static URL toParentURL(URL url) {
    String path = url.getPath();
    int i = path.lastIndexOf(URI_FILE_SEPARATOR);
    if (i != -1) {
      path = path.substring(0, i + 1);
    }
    try {
      return new URL(url, path + url.getQuery());
    } catch (MalformedURLException ex) {
      throw new RuntimeException(ex);
    }
  }

  public static String relativize(File from, File to) {
    URI fromURI = from.toURI();
    URI toURI = to.toURI();

    String relative = getRelativePath(fromURI.toString(), toURI.toString(), URIUtils.URI_FILE_SEPARATOR);
    relative = convertUriToPlatformSeparator(relative);

    return relative;
  }

  public static String relativizeSourceURIs(LessSource from, LessSource to) {
    if (to==null)
      return "";
    
    if (to.getAbsoluteURI()==null)
      return to.getName();
    
    if (from==null || from.getAbsoluteURI()==null)
      return to.getAbsoluteURI().toString();
    
    String fromURI = from.getAbsoluteURI().toString();
    String toURI = to.getAbsoluteURI().toString();

    return getRelativePath(fromURI, toURI, URIUtils.URI_FILE_SEPARATOR);
  }

  private static String convertUriToPlatformSeparator(String relative) {
    return relative.replace(URI_FILE_SEPARATOR, Constants.FILE_SEPARATOR);
  }

  /**
   * taken from stackoverflow: http://stackoverflow.com/questions/204784/how-to-construct-a-relative-path-in-java-from-two-absolute-paths-or-urls
   * 
   * Get the relative path from one file to another, specifying the directory
   * separator. If one of the provided resources does not exist, it is assumed
   * to be a file unless it ends with '/' or '\'.
   * @param basePath
   *          basePath is calculated from this file
   * @param targetPath
   *          targetPath is calculated to this file
   * @param pathSeparator
   *          directory separator. The platform default is not assumed so that
   *          we can test Unix behaviour when running on Windows (for example)
   * 
   * @return
   */
  public static String getRelativePath(String basePath, String targetPath, String pathSeparator) {

    // Normalize the paths
    String normalizedTargetPath = FilenameUtils.normalizeNoEndSeparator(targetPath);
    String normalizedBasePath = FilenameUtils.normalizeNoEndSeparator(basePath);

    // Undo the changes to the separators made by normalization
    if (pathSeparator.equals("/")) {
      normalizedTargetPath = FilenameUtils.separatorsToUnix(normalizedTargetPath);
      normalizedBasePath = FilenameUtils.separatorsToUnix(normalizedBasePath);

    } else if (pathSeparator.equals("\\")) {
      normalizedTargetPath = FilenameUtils.separatorsToWindows(normalizedTargetPath);
      normalizedBasePath = FilenameUtils.separatorsToWindows(normalizedBasePath);

    } else {
      throw new IllegalArgumentException("Unrecognised dir separator '" + pathSeparator + "'");
    }

    String[] base = normalizedBasePath.split(Pattern.quote(pathSeparator));
    String[] target = normalizedTargetPath.split(Pattern.quote(pathSeparator));

    // First get all the common elements. Store them as a string,
    // and also count how many of them there are.
    StringBuffer common = new StringBuffer();

    int commonIndex = 0;
    while (commonIndex < target.length && commonIndex < base.length && target[commonIndex].equals(base[commonIndex])) {
      common.append(target[commonIndex] + pathSeparator);
      commonIndex++;
    }

    if (commonIndex == 0) {
      // No single common path element. This most
      // likely indicates differing drive letters, like C: and D:.
      // These paths cannot be relativized.
      return normalizedTargetPath;
//      throw new PathResolutionException("No common path element found for '" + normalizedTargetPath + "' and '" + normalizedBasePath + "'");
    }

    // The number of directories we have to backtrack depends on whether the base is a file or a dir
    // For example, the relative path from
    //
    // /foo/bar/baz/gg/ff to /foo/bar/baz
    // 
    // ".." if ff is a file
    // "../.." if ff is a directory
    //
    // The following is a heuristic to figure out if the base refers to a file or dir. It's not perfect, because
    // the resource referred to by this path may not actually exist, but it's the best I can do
    boolean baseIsFile = true;

    File baseResource = new File(normalizedBasePath);

    if (baseResource.exists()) {
      baseIsFile = baseResource.isFile();

    } else if (basePath.endsWith(pathSeparator)) {
      baseIsFile = false;
    }

    StringBuffer relative = new StringBuffer();

    if (base.length != commonIndex) {
      int numDirsUp = baseIsFile ? base.length - commonIndex - 1 : base.length - commonIndex;

      for (int i = 0; i < numDirsUp; i++) {
        relative.append(".." + pathSeparator);
      }
    }
    relative.append(safeSubstring(normalizedTargetPath, common.length()));
    return relative.toString();
  }

  private static String safeSubstring(String string, int beginIndex) {
    if (beginIndex>string.length())
      return "";
    
    return string.substring(beginIndex);
  }

}
