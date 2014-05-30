package com.github.sommeri.less4j.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;

import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.LessSource.FileSource;
import com.github.sommeri.less4j.platform.Constants;

public class URIUtils {

  public static final String URI_FILE_SEPARATOR = "/";

  public static URL toParentURL(URL url) {
    URL parentUrl;
    try {
        // compute parent URL
        URL rootParentUrl = new URL(url, "");

        // add query string to parent URL
        String query = url.getQuery();
        if (query == null || query.isEmpty()) {
            parentUrl = new URL(rootParentUrl.toString());
        } else {
            parentUrl = new URL(rootParentUrl.toString() + query);
        }
    } catch (MalformedURLException ex) {
        throw new RuntimeException(ex);
    }

    return parentUrl;
  }

  public static String relativize(File from, File to) {
    URI fromURI = from.toURI();
    URI toURI = to.toURI();

    String relative = getRelativePath(fromURI.toString(), toURI.toString(), URIUtils.URI_FILE_SEPARATOR);
    relative = convertUriToPlatformSeparator(relative);

    return relative;
  }

  public static String relativizeSourceURIs(LessSource from, LessSource to) {
    if (to == null)
      return null;

    URI toURI = to.getURI();
    if (toURI == null)
      return to.getName();

    String toURIAsString = toURI.toString();

    if (from == null)
      return toURIAsString;

    URI fromURI = from.getURI();
    if (fromURI == null)
      return toURIAsString;

    String fromURIAsString = fromURI.toString();

    return getRelativePath(fromURIAsString, toURIAsString, URIUtils.URI_FILE_SEPARATOR);
  }

  public static String convertUriToPlatformSeparator(String relative) {
    return relative.replace(URI_FILE_SEPARATOR, Constants.FILE_SEPARATOR);
  }

  public static String convertPlatformSeparatorToUri(String relative) {
    return relative.replace(Constants.FILE_SEPARATOR, URI_FILE_SEPARATOR);
  }

  /**
   * taken from stackoverflow:
   * http://stackoverflow.com/questions/204784/how-to-construct
   * -a-relative-path-in-java-from-two-absolute-paths-or-urls
   * 
   * Get the relative path from one file to another, specifying the directory
   * separator. If one of the provided resources does not exist, it is assumed
   * to be a file unless it ends with '/' or '\'.
   * 
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
    String normalizedTargetPath = normalizeNoEndSeparator(targetPath, pathSeparator);
    String normalizedBasePath = normalizeNoEndSeparator(basePath, pathSeparator);
    
    if (normalizedTargetPath==null)
      return "";
    
    if (normalizedBasePath==null)
      return targetPath;

    String[] base = normalizedBasePath.split(Pattern.quote(pathSeparator));
    String[] target = normalizedTargetPath.split(Pattern.quote(pathSeparator));

    // First get all the common elements. Store them as a string,
    // and also count how many of them there are.
    StringBuilder common = new StringBuilder();

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

    StringBuilder relative = new StringBuilder();

    if (base.length != commonIndex) {
      int numDirsUp = baseIsFile ? base.length - commonIndex - 1 : base.length - commonIndex;

      for (int i = 0; i < numDirsUp; i++) {
        relative.append(".." + pathSeparator);
      }
    }
    relative.append(safeSubstring(normalizedTargetPath, common.length()));
    return relative.toString();
  }

  public static String normalizeNoEndSeparator(String path, String separator) {
    String result = RelativeFilenameUtils.normalizeNoEndSeparator(path);

    // Undo the changes to the separators made by normalization
    if ("/".equals(separator)) {
      result = FilenameUtils.separatorsToUnix(result);

    } else if ("\\".equals(separator)) {
      result = FilenameUtils.separatorsToWindows(result);
    } else {
      throw new IllegalArgumentException("Unrecognised dir separator '" + separator + "'");
    }

    return result;
  }

  private static String safeSubstring(String string, int beginIndex) {
    if (beginIndex > string.length())
      return "";

    return string.substring(beginIndex);
  }

  public static String addPLatformSlashIfNeeded(String directory) {
    if (directory.endsWith(Constants.FILE_SEPARATOR))
      return directory;

    return directory + Constants.FILE_SEPARATOR;
  }

  public static URI changeSuffix(URI uri, String dottedSuffix) {
    if (uri==null)
      return null;
    
    URI newUri;

    try {

        String uriAsString = uri.toString();
        int lastIndexOfDot = uriAsString.lastIndexOf('.');
        if(lastIndexOfDot > -1) {
          newUri = new URI(uriAsString.substring(0, (lastIndexOfDot + 1)) + dottedSuffix);
      } else {
          newUri = new URI(uriAsString + dottedSuffix);
      }
    } catch (URISyntaxException exception) {
        throw new IllegalStateException(exception);
    }

    return newUri;
  }

  public static String changeSuffix(String filename, String dottedSuffix) {
    if (filename == null)
      return null;

    int lastIndexOf = filename.lastIndexOf('.');
    if (lastIndexOf == -1)
      return filename + dottedSuffix;

    return filename.substring(0, lastIndexOf) + dottedSuffix;
  }

  public static String addSuffix(String filename, String dottedSuffix) {
    if (filename == null)
      return null;

    return filename + dottedSuffix;
  }

  public static File changeSuffix(File file, String dottedSuffix) {
    if (file == null)
      return null;

    String filename = changeSuffix(file.toString(), dottedSuffix);
    return new File(filename);
  }

  public static FileSource changeSuffix(FileSource source, String dottedSuffix) {
    if (source == null)
      return null;

    return new LessSource.FileSource(changeSuffix(source.getInputFile(), dottedSuffix));
  }

  public static boolean isQuotedUrl(String url) {
    int length = url.length();
    if (length<2)
      return false;
    
    char first = url.charAt(0);
    char last = url.charAt(length-1);
    if (first!=last)
      return false;
    
    return first=='\'' || first=='"';
  }
}
