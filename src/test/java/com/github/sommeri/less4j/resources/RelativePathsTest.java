package com.github.sommeri.less4j.resources;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Test;

import com.github.sommeri.less4j.utils.URIUtils;

public class RelativePathsTest {

  private static final String LINUX = "/";
  private static final String WINDOWS = "\\";

  @Test
  public void testPathsNormalization() {
    assertEquals("..", URIUtils.normalizeNoEndSeparator("../", LINUX));
    assertEquals("../foo", URIUtils.normalizeNoEndSeparator("../foo", LINUX));
    assertEquals("../bar", URIUtils.normalizeNoEndSeparator("foo/../../bar", LINUX));
    assertEquals("~/../bar", URIUtils.normalizeNoEndSeparator("~/../bar", LINUX));
    assertEquals("../../bar", URIUtils.normalizeNoEndSeparator("../foo/../../bar", LINUX));

    // these two are technically incorrect and used to return null. It could return null or anything else and still be "technically" correct 
    assertEquals("//server/../bar", URIUtils.normalizeNoEndSeparator("//server/../bar", LINUX));
    assertEquals("C:\\..\\bar", URIUtils.normalizeNoEndSeparator("C:\\..\\bar", WINDOWS));

    assertEquals("/foo", URIUtils.normalizeNoEndSeparator("/foo//", LINUX));
    assertEquals("/foo", URIUtils.normalizeNoEndSeparator("/foo/./", LINUX));
    assertEquals("/bar", URIUtils.normalizeNoEndSeparator("/foo/../bar", LINUX));
    assertEquals("/bar", URIUtils.normalizeNoEndSeparator("/foo/../bar/", LINUX));
    assertEquals("/baz", URIUtils.normalizeNoEndSeparator("/foo/../bar/../baz", LINUX));

    assertEquals("foo", URIUtils.normalizeNoEndSeparator("foo/bar/..", LINUX));

    assertEquals("bar", URIUtils.normalizeNoEndSeparator("foo/../bar", LINUX));
    assertEquals("//server/bar", URIUtils.normalizeNoEndSeparator("//server/foo/../bar", LINUX));
    assertEquals("C:\\bar", URIUtils.normalizeNoEndSeparator("C:\\foo\\..\\bar", WINDOWS));

    assertEquals("~/bar", URIUtils.normalizeNoEndSeparator("~/foo/../bar/", LINUX));

    // do not work as advertized, but irrelevant
    //assertEquals("/foo/bar", URIUtils.normalizeNoEndSeparator("//foo//./bar", LINUX));
  }

  @Test
  public void testOnNullCase() {
    String nullPath = ":/normalize/to/null";
    String nonNullPath = "normalize/normally/";
    assertNull(URIUtils.normalizeNoEndSeparator(nullPath, LINUX));
    assertNotNull(URIUtils.normalizeNoEndSeparator(nonNullPath, LINUX));
    
    assertEquals("", URIUtils.getRelativePath(nullPath, nullPath, LINUX));
    assertEquals("", URIUtils.getRelativePath(nonNullPath, nullPath, LINUX));
    assertEquals(nonNullPath, URIUtils.getRelativePath(nullPath, nonNullPath, LINUX));
  }

  @Test
  public void testLeadingClimbUp() {
    assertEquals("stuff/xyz.dat", URIUtils.getRelativePath("../var/data/", "../var/data/stuff/xyz.dat", LINUX));
    assertEquals("../../b/c", URIUtils.getRelativePath("../a/x/y/", "../a/b/c", LINUX));
    assertEquals("../../b/c", URIUtils.getRelativePath("../m/n/o/a/x/y/", "../m/n/o/a/b/c", LINUX));

    assertEquals("a/b/c", URIUtils.getRelativePath("../a/x/y/", "a/b/c", LINUX));
    assertEquals("/a/b/c", URIUtils.getRelativePath("../a/x/y/", "/a/b/c", LINUX));

    assertEquals("../a/b/c", URIUtils.getRelativePath("a/x/y/", "../a/b/c", LINUX));
    assertEquals("../a/b/c", URIUtils.getRelativePath("/a/x/y/", "../a/b/c", LINUX));
  }

  @Test
  public void testTargetEqualsBase() {
    String basePath = "https://www.google.com:8080/a/b/c?parameter=value#metoo";
    String targetPath = "https://www.google.com:8080/a/b/c?parameter=value#metoo";
    assertEquals("", URIUtils.getRelativePath(basePath, targetPath, LINUX));

    targetPath = "C:\\Windows\\Boot\\Fonts\\chs_boot.ttf";
    basePath = "C:\\Windows\\Boot\\Fonts\\chs_boot.ttf";
    assertEquals("", URIUtils.getRelativePath(basePath, targetPath, WINDOWS));

  }

  @Test
  public void testUrl() {
    String basePath = "https://www.google.com:8080/a/b/c?parameter=value#metoo";
    String targetPath = "https://www.google.com:8080/a/x/../b/d/e/demo.js#middle";
    assertEquals("d/e/demo.js#middle", URIUtils.getRelativePath(basePath, targetPath, LINUX));

    basePath = "https://www.google.com:8080/a/b/c?parameter=value#metoo";
    targetPath = "https://www.google.com:8080/a/x/../d/e/demo.js#middle";
    assertEquals("../d/e/demo.js#middle", URIUtils.getRelativePath(basePath, targetPath, LINUX));

    basePath = "https://www.google.com:8080/a/b/c?parameter=value#metoo";
    targetPath = "https://www.google.com:8080/a/b/site.html?parameter=value#metoo";
    assertEquals("site.html?parameter=value#metoo", URIUtils.getRelativePath(basePath, targetPath, LINUX));
  }

  @Test
  public void testNoCommonPrefix() {
    String basePath = "/usr/data/";
    String targetPath = "/var/data/stuff/xyz.dat";
    assertEquals("../../var/data/stuff/xyz.dat", URIUtils.getRelativePath(basePath, targetPath, LINUX));

    basePath = "https://www.google.com:8080/a/b/c";
    targetPath = "www.google.com:8080/a/x/../b/";
    //this kind of normalization *usually* preserve semantic
    assertEquals("www.google.com:8080/a/b", URIUtils.getRelativePath(basePath, targetPath, LINUX));
  }

  @Test
  public void testAlreadyThere() {
    String basePath = "https://www.google.com:8080/a/b/c";
    String targetPath = "https://www.google.com:8080/a/b";
    assertEquals("", URIUtils.getRelativePath(basePath, targetPath, LINUX));
  }

  @Test
  public void testOneUp() {
    String basePath = "https://www.google.com:8080/a/b/c/";
    String targetPath = "https://www.google.com:8080/a/b/";
    assertEquals("../", URIUtils.getRelativePath(basePath, targetPath, LINUX));

    basePath = "https://www.google.com:8080/a/b/c/";
    targetPath = "https://www.google.com:8080/a/b.txt";
    assertEquals("../../b.txt", URIUtils.getRelativePath(basePath, targetPath, LINUX));
  }

  @Test
  public void testGetRelativePathDifferentDriveLetters() {
    String target = "D:\\sources\\recovery\\RecEnv.exe";
    String base = "C:\\Java\\workspace\\AcceptanceTests\\Standard test data\\geo\\";
    assertEquals(target, URIUtils.getRelativePath(base, target, WINDOWS));
  }

  @Test
  public void testGetRelativePathsUnix() {
    assertEquals("stuff/xyz.dat", URIUtils.getRelativePath("/var/data/", "/var/data/stuff/xyz.dat", LINUX));
    assertEquals("../../b/c", URIUtils.getRelativePath("/a/x/y/", "/a/b/c", LINUX));
    assertEquals("../../b/c", URIUtils.getRelativePath("/m/n/o/a/x/y/", "/m/n/o/a/b/c", LINUX));
  }

  @Test
  public void testGetRelativePathFileToFile() {
    String target = "C:\\Windows\\Boot\\Fonts\\chs_boot.ttf";
    String base = "C:\\Windows\\Speech\\Common\\sapisvr.exe";

    String relPath = URIUtils.getRelativePath(base, target, WINDOWS);
    assertEquals("..\\..\\Boot\\Fonts\\chs_boot.ttf", relPath);
  }

  @Test
  public void testGetRelativePathDirectoryToFile() {
    String target = "C:\\Windows\\Boot\\Fonts\\chs_boot.ttf";
    String base = "C:\\Windows\\Speech\\Common\\";

    String relPath = URIUtils.getRelativePath(base, target, WINDOWS);
    assertEquals("..\\..\\Boot\\Fonts\\chs_boot.ttf", relPath);
  }

  @Test
  public void testGetRelativePathFileToDirectory() {
    String target = "C:\\Windows\\Boot\\Fonts";
    String base = "C:\\Windows\\Speech\\Common\\foo.txt";

    String relPath = URIUtils.getRelativePath(base, target, WINDOWS);
    assertEquals("..\\..\\Boot\\Fonts", relPath);
  }

  @Test
  public void testGetRelativePathDirectoryToDirectory() {
    String target = "C:\\Windows\\Boot\\";
    String base = "C:\\Windows\\Speech\\Common\\";
    String expected = "..\\..\\Boot";

    String relPath = URIUtils.getRelativePath(base, target, WINDOWS);
    assertEquals(expected, relPath);
  }

}
