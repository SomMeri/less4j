package com.github.sommeri.less4j.core;

import java.io.File;
import java.net.URL;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessSource;

public class DefaultLessCompiler implements LessCompiler {

  @Override
  public CompilationResult compile(String lessContent) throws Less4jException {
    ThreadUnsafeLessCompiler compiler = new ThreadUnsafeLessCompiler();
    return compiler.compile(lessContent);
  }

  @Override
  public CompilationResult compile(String lessContent, Configuration options) throws Less4jException {
    ThreadUnsafeLessCompiler compiler = new ThreadUnsafeLessCompiler();
    return compiler.compile(lessContent, options);
  }

  @Override
  public CompilationResult compile(File lessFile) throws Less4jException {
    ThreadUnsafeLessCompiler compiler = new ThreadUnsafeLessCompiler();
    return compiler.compile(lessFile);
  }

  @Override
  public CompilationResult compile(URL lessURL) throws Less4jException {
    ThreadUnsafeLessCompiler compiler = new ThreadUnsafeLessCompiler();
    return compiler.compile(lessURL);
  }

  @Override
  public CompilationResult compile(URL lessURL, Configuration options) throws Less4jException {
    ThreadUnsafeLessCompiler compiler = new ThreadUnsafeLessCompiler();
    return compiler.compile(lessURL, options);
  }

  public CompilationResult compile(LessSource source) throws Less4jException {
    ThreadUnsafeLessCompiler compiler = new ThreadUnsafeLessCompiler();
    return compiler.compile(source);
  }

  @Override
  public CompilationResult compile(File lessFile, Configuration options) throws Less4jException {
    ThreadUnsafeLessCompiler compiler = new ThreadUnsafeLessCompiler();
    return compiler.compile(lessFile, options);
  }

  @Override
  public CompilationResult compile(LessSource source, Configuration options) throws Less4jException {
    ThreadUnsafeLessCompiler compiler = new ThreadUnsafeLessCompiler();
    return compiler.compile(source, options);
  }

}
