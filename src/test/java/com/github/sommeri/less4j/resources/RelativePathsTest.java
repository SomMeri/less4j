package com.github.sommeri.less4j.resources;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.github.sommeri.less4j.utils.URIUtils;

public class RelativePathsTest {

  @Test
  public void testTargetEqualsBase() {
    String basePath = "https://www.google.com:8080/a/b/c?parameter=value#metoo";
    String targetPath = "https://www.google.com:8080/a/b/c?parameter=value#metoo";
    assertEquals("", URIUtils.getRelativePath(basePath, targetPath, "/"));

    targetPath = "C:\\Windows\\Boot\\Fonts\\chs_boot.ttf";
    basePath = "C:\\Windows\\Boot\\Fonts\\chs_boot.ttf";
    assertEquals("", URIUtils.getRelativePath(basePath, targetPath, "\\"));

  }
  
  @Test
  public void testUrl() {
    String basePath = "https://www.google.com:8080/a/b/c?parameter=value#metoo";
    String targetPath = "https://www.google.com:8080/a/x/../b/d/e/demo.js#middle";
    assertEquals("d/e/demo.js#middle", URIUtils.getRelativePath(basePath, targetPath, "/"));

    basePath = "https://www.google.com:8080/a/b/c?parameter=value#metoo";
    targetPath = "https://www.google.com:8080/a/x/../d/e/demo.js#middle";
    assertEquals("../d/e/demo.js#middle", URIUtils.getRelativePath(basePath, targetPath, "/"));

    basePath = "https://www.google.com:8080/a/b/c?parameter=value#metoo";
    targetPath = "https://www.google.com:8080/a/b/site.html?parameter=value#metoo";
    assertEquals("site.html?parameter=value#metoo", URIUtils.getRelativePath(basePath, targetPath, "/"));
  }

  @Test
  public void testNoCommonPrefix() {
    String basePath = "/usr/data/";
    String targetPath = "/var/data/stuff/xyz.dat";
    assertEquals("../../var/data/stuff/xyz.dat", URIUtils.getRelativePath(basePath, targetPath, "/"));

    basePath = "https://www.google.com:8080/a/b/c";
    targetPath = "www.google.com:8080/a/x/../b/";
    //this kind of normalization *usually* preserve semantic
    assertEquals("www.google.com:8080/a/b", URIUtils.getRelativePath(basePath, targetPath, "/"));
  }

  @Test
  public void testAlreadyThere() {
    String basePath = "https://www.google.com:8080/a/b/c";
    String targetPath = "https://www.google.com:8080/a/b";
    assertEquals("", URIUtils.getRelativePath(basePath, targetPath, "/"));
  }

  @Test
  public void testOneUp() {
    String basePath = "https://www.google.com:8080/a/b/c/";
    String targetPath = "https://www.google.com:8080/a/b/";
    assertEquals("../", URIUtils.getRelativePath(basePath, targetPath, "/"));

    basePath = "https://www.google.com:8080/a/b/c/";
    targetPath = "https://www.google.com:8080/a/b.txt";
    assertEquals("../../b.txt", URIUtils.getRelativePath(basePath, targetPath, "/"));
  }

  @Test
  public void testGetRelativePathDifferentDriveLetters() {
    String target = "D:\\sources\\recovery\\RecEnv.exe";
    String base = "C:\\Java\\workspace\\AcceptanceTests\\Standard test data\\geo\\";
    assertEquals(target, URIUtils.getRelativePath(base, target, "\\"));
  }

  @Test
  public void testGetRelativePathsUnix() {
    assertEquals("stuff/xyz.dat", URIUtils.getRelativePath("/var/data/", "/var/data/stuff/xyz.dat", "/"));
    assertEquals("../../b/c", URIUtils.getRelativePath("/a/x/y/", "/a/b/c", "/"));
    assertEquals("../../b/c", URIUtils.getRelativePath("/m/n/o/a/x/y/", "/m/n/o/a/b/c", "/"));
  }

  @Test
  public void testGetRelativePathFileToFile() {
    String target = "C:\\Windows\\Boot\\Fonts\\chs_boot.ttf";
    String base = "C:\\Windows\\Speech\\Common\\sapisvr.exe";

    String relPath = URIUtils.getRelativePath(base, target, "\\");
    assertEquals("..\\..\\Boot\\Fonts\\chs_boot.ttf", relPath);
  }

  @Test
  public void testGetRelativePathDirectoryToFile() {
    String target = "C:\\Windows\\Boot\\Fonts\\chs_boot.ttf";
    String base = "C:\\Windows\\Speech\\Common\\";

    String relPath = URIUtils.getRelativePath(base, target, "\\");
    assertEquals("..\\..\\Boot\\Fonts\\chs_boot.ttf", relPath);
  }

  @Test
  public void testGetRelativePathFileToDirectory() {
    String target = "C:\\Windows\\Boot\\Fonts";
    String base = "C:\\Windows\\Speech\\Common\\foo.txt";

    String relPath = URIUtils.getRelativePath(base, target, "\\");
    assertEquals("..\\..\\Boot\\Fonts", relPath);
  }

  @Test
  public void testGetRelativePathDirectoryToDirectory() {
    String target = "C:\\Windows\\Boot\\";
    String base = "C:\\Windows\\Speech\\Common\\";
    String expected = "..\\..\\Boot";

    String relPath = URIUtils.getRelativePath(base, target, "\\");
    assertEquals(expected, relPath);
  }

}
