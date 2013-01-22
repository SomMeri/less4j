package com.github.sommeri.less4j.core;

import java.io.File;
import java.net.URL;

import com.github.sommeri.less4j.LessCompiler.Problem;
import com.github.sommeri.less4j.LessSource;

public abstract class AbstractProblem implements Problem {

  @Override
  public final File getFile() {
    return getSource() instanceof LessSource.FileSource ? ((LessSource.FileSource) getSource()).getInputFile() : null;
  }

  @Override
  public final URL getURL() {
    return getSource() instanceof LessSource.URLSource ? ((LessSource.URLSource) getSource()).getInputURL() : null;
  }

}
