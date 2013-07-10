package com.github.sommeri.less4j.resources;

import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

import com.github.sommeri.less4j.LessSource.CannotReadFile;
import com.github.sommeri.less4j.LessSource.FileNotFound;
import com.github.sommeri.less4j.LessSource.FileSource;

public class LessSourceTest {

  private static final String file = "src/test/resources/less-source/level-1/level-2/source.less";

  @Test
  public void lastModifiedPropagation() throws FileNotFound, CannotReadFile {
    FileSource main = new FileSource(new File(file));
    FileSource firstImport = main.relativeSource("imported-1.less");
    FileSource secondImport = firstImport.relativeSource("../imported-2.less");
    
    long mainModified = setLastModified(main, 1364590362375L);
    long firstModified = setLastModified(firstImport, 1364590418644L);
    long secondModified = setLastModified(secondImport, 1364590474913L);
    
    main.getContent();
    assertModified(main, mainModified, mainModified);

    firstImport.getContent();
    assertModified(main, mainModified, firstModified);
    assertModified(firstImport, firstModified, firstModified);
    
    secondImport.getContent();
    assertModified(main, mainModified, secondModified);
    assertModified(firstImport, firstModified, secondModified);
    assertModified(secondImport, secondModified, secondModified);
  }

  private long setLastModified(FileSource fileSource, long modified) {
    fileSource.getInputFile().setLastModified(modified);
    return fileSource.getInputFile().lastModified();
  }

  private void assertModified(FileSource main, long last, long latest) {
    assertEquals(main.getLastModified(), last);
    assertEquals(main.getLatestModified(), latest);
  }

}

class OpenFileSource extends FileSource {

  public OpenFileSource(File inputFile) {
    super(inputFile);
  }

}
