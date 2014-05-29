package com.github.sommeri.less4j;

import java.io.IOException;

import static org.junit.Assert.*;
import org.junit.Test;

import com.github.sommeri.sourcemap.FilePosition;
import com.github.sommeri.sourcemap.SourceMapFormat;
import com.github.sommeri.sourcemap.SourceMapGenerator;
import com.github.sommeri.sourcemap.SourceMapGeneratorFactory;

public class SourceMapBuilderTest {

  @Test
  public void unknownFilenamesSameContent() throws Exception {
    SourceMapGenerator generator = SourceMapGeneratorFactory.getInstance(SourceMapFormat.V3);
    generator.addMapping(null, "content", "ahoj", new FilePosition(1, 1), new FilePosition(1, 1), new FilePosition(1, 5));
    generator.addMapping(null, "content", "nazdar", new FilePosition(2, 1), new FilePosition(2, 1), new FilePosition(2, 5));
    generator.addMapping(null, "content", "hello", new FilePosition(3, 1), new FilePosition(3, 1), new FilePosition(3, 5));
    
    String map = generateMap(generator);
    assertEquals("{\n\"version\":3,\n\"file\":\"name\",\n\"lineCount\":4,\n\"mappings\":\"A;CACCA,I;CACAC,I;CACAC;\",\n\"sources\":[null],\n\"sourcesContent\":[\"content\"],\n\"names\":[\"ahoj\",\"nazdar\",\"hello\"]\n}\n", map);
    System.out.println(map);
  }

  @Test
  public void unknownFilenamesVariousContent() throws Exception {
    SourceMapGenerator generator = SourceMapGeneratorFactory.getInstance(SourceMapFormat.V3);
    generator.addMapping(null, "content", "ahoj", new FilePosition(1, 1), new FilePosition(1, 1), new FilePosition(1, 5));
    generator.addMapping(null, "content", "nazdar", new FilePosition(2, 1), new FilePosition(2, 1), new FilePosition(2, 5));
    generator.addMapping(null, "different", "hello", new FilePosition(3, 1), new FilePosition(3, 1), new FilePosition(3, 5));
    
    String map = generateMap(generator);
    assertEquals("{\n\"version\":3,\n\"file\":\"name\",\n\"lineCount\":4,\n\"mappings\":\"A;CACCA,I;CACAC,I;CCCAC;\",\n\"sources\":[null,null],\n\"sourcesContent\":[\"content\",\"different\"],\n\"names\":[\"ahoj\",\"nazdar\",\"hello\"]\n}\n", map);
    System.out.println(map);
  }

  @Test
  public void differentFilenamesSameContent() throws Exception {
    SourceMapGenerator generator = SourceMapGeneratorFactory.getInstance(SourceMapFormat.V3);
    generator.addMapping("file 1", "ignore", "ahoj", new FilePosition(1, 1), new FilePosition(1, 1), new FilePosition(1, 5));
    generator.addMapping("file 1", "ignore", "nazdar", new FilePosition(2, 1), new FilePosition(2, 1), new FilePosition(2, 5));
    generator.addMapping("file 2", "ignore", "hello", new FilePosition(3, 1), new FilePosition(3, 1), new FilePosition(3, 5));
    
    String map = generateMap(generator);
    assertEquals("{\n\"version\":3,\n\"file\":\"name\",\n\"lineCount\":4,\n\"mappings\":\"A;CACCA,I;CACAC,I;CCCAC;\",\n\"sources\":[\"file 1\",\"file 2\"],\n\"sourcesContent\":[\"ignore\",\"ignore\"],\n\"names\":[\"ahoj\",\"nazdar\",\"hello\"]\n}\n", map);
    System.out.println(map);
  }

  @Test
  public void differentFilenamesUnknownContent() throws Exception {
    SourceMapGenerator generator = SourceMapGeneratorFactory.getInstance(SourceMapFormat.V3);
    generator.addMapping("file 1", null, "ahoj", new FilePosition(1, 1), new FilePosition(1, 1), new FilePosition(1, 5));
    generator.addMapping("file 1", null, "nazdar", new FilePosition(2, 1), new FilePosition(2, 1), new FilePosition(2, 5));
    generator.addMapping("file 2", null, "hello", new FilePosition(3, 1), new FilePosition(3, 1), new FilePosition(3, 5));
    
    String map = generateMap(generator);
    assertEquals("{\n\"version\":3,\n\"file\":\"name\",\n\"lineCount\":4,\n\"mappings\":\"A;CACCA,I;CACAC,I;CCCAC;\",\n\"sources\":[\"file 1\",\"file 2\"],\n\"sourcesContent\":[null,null],\n\"names\":[\"ahoj\",\"nazdar\",\"hello\"]\n}\n", map);
    System.out.println(map);
  }

  private String generateMap(SourceMapGenerator generator) {
    try {
      StringBuilder sb = new StringBuilder();
      generator.appendTo(sb, "name");
      return sb.toString();
    } catch (IOException e) {
      throw new IllegalStateException("Impossible to happen exception.", e);
    }
  }
  
}
