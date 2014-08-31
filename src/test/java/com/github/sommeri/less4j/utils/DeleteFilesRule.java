package com.github.sommeri.less4j.utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class DeleteFilesRule implements TestRule  {
  
  private List<File> files;
  
  public void ensureRemoval(String... filenames) {
    for (String filename : filenames) {
      ensureRemoval(new File(filename));
    }
  }
  
  public void ensureRemoval(File file) {
    remove(file);
    files.add(file);
  }
  
  private void remove(File file) {
    if (file.exists())
      file.delete();
  }

  public Statement apply(final Statement base, final Description description) {
    return new Statement() {
      
      @Override
      public void evaluate() throws Throwable {
        files = new ArrayList<File>();
        try {
          base.evaluate();
        } finally {
          for (File file : files) {
            remove(file);
          }
        }
      }

    };
  }

}
