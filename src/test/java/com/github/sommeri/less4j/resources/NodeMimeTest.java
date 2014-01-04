package com.github.sommeri.less4j.resources;

import static org.junit.Assert.*;

import org.junit.BeforeClass;
import org.junit.Test;

import com.github.sommeri.less4j.nodemime.NodeMime;

public class NodeMimeTest {

  @BeforeClass
  public static void setUpBeforeClass() throws Exception {
  }

  @Test
  public void test() {
    NodeMime mime = new NodeMime();
    assertEquals("image/jpeg", mime.lookupMime("aaaa.jpg"));
    assertEquals("application/x-msdownload", mime.lookupMime("c:\\data\\run.exe"));
    assertEquals("text/html", mime.lookupMime("c:\\data\\run.html"));
    assertEquals("text/css", mime.lookupMime("c:/data/run.css"));
    assertEquals("application/octet-stream", mime.lookupMime("aaaa."));
    assertEquals("application/octet-stream", mime.lookupMime("aaaa\\"));
    assertEquals("application/octet-stream", mime.lookupMime("aaaa/"));
  }

}
