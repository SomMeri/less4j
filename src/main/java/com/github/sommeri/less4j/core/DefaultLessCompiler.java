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
  public CompilationResult compile(File inputFile) throws Less4jException {
    ThreadUnsafeLessCompiler compiler = new ThreadUnsafeLessCompiler();
    return compiler.compile(inputFile);
  }

  @Override
  public CompilationResult compile(URL inputURL) throws Less4jException {
    ThreadUnsafeLessCompiler compiler = new ThreadUnsafeLessCompiler();
    return compiler.compile(inputURL);
  }

  public CompilationResult compile(LessSource source) throws Less4jException {
    ThreadUnsafeLessCompiler compiler = new ThreadUnsafeLessCompiler();
    return compiler.compile(source);
  }

  @Override
  public CompilationResult compile(File inputFile, Configuration sourceMapOptions) throws Less4jException {
    ThreadUnsafeLessCompiler compiler = new ThreadUnsafeLessCompiler();
    return compiler.compile(inputFile, sourceMapOptions);
  }

  @Override
  public CompilationResult compile(LessSource source, Configuration sourceMapOptions) throws Less4jException {
    ThreadUnsafeLessCompiler compiler = new ThreadUnsafeLessCompiler();
    return compiler.compile(source, sourceMapOptions);
  }

}
