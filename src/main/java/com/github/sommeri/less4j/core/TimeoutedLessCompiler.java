package com.github.sommeri.less4j.core;

import java.io.File;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.problems.BugHappened;
import com.github.sommeri.less4j.core.problems.ProblemsHandler;

public class TimeoutedLessCompiler implements LessCompiler {

  private final long timeout;
  private final TimeUnit unit;

  public TimeoutedLessCompiler(long timeout, TimeUnit unit) {
    super();
    this.timeout = timeout;
    this.unit = unit;
  }

  @Override
  public CompilationResult compile(String lessContent) throws Less4jException {
    return compile(new LessSource.StringSource(lessContent), null);
  }

  @Override
  public CompilationResult compile(String lessContent, Configuration options) throws Less4jException {
    return compile(new LessSource.StringSource(lessContent), options);
  }

  @Override
  public CompilationResult compile(File lessFile) throws Less4jException {
    LessSource.FileSource lessSource = new LessSource.FileSource(lessFile);
    return compile(lessSource, null);
  }

  @Override
  public CompilationResult compile(File lessFile, Configuration options) throws Less4jException {
    return compile(new LessSource.FileSource(lessFile, "utf-8"), options);
  }

  @Override
  public CompilationResult compile(URL lessURL) throws Less4jException {
    return compile(new LessSource.URLSource(lessURL));
  }

  @Override
  public CompilationResult compile(URL lessURL, Configuration options) throws Less4jException {
    return compile(new LessSource.URLSource(lessURL), options);
  }

  @Override
  public CompilationResult compile(LessSource source) throws Less4jException {
    return compile(source, new Configuration());
  }

  @Override
  public CompilationResult compile(LessSource source, Configuration options) throws Less4jException {
    final LessSource iSource = source;
    final Configuration iOption = options;

    Callable<CompilationResult> task = new Callable<CompilationResult>() {
      public CompilationResult call() {

        ThreadUnsafeLessCompiler compiler = new ThreadUnsafeLessCompiler();
        try {
          return compiler.compile(iSource, iOption);
        } catch (Less4jException ex) {
          throw new Less4jRuntimeException(ex);
        }
      }
    };

    ExecutorService executor = Executors.newSingleThreadExecutor();
    Future<CompilationResult> future = executor.submit(task);
    try {
      boolean terminatedNormally = executor.awaitTermination(timeout, unit);
      if (terminatedNormally) {
        try {
          CompilationResult result = future.get();
          return result;
        } catch (ExecutionException e) {
          throw (Less4jException) e.getCause().getCause();
        }
      } else {
        executor.shutdownNow();
        executor.awaitTermination(80, unit);
        return future.get();
      }
    } catch (InterruptedException e) {
      throw new BugHappened("Unexpected thread interrupt.", (ASTCssNode) null);
    } catch (ExecutionException e) {
      Throwable cause = e.getCause();
      
      if (cause instanceof Less4jRuntimeException) {
        Less4jRuntimeException wrapper = (Less4jRuntimeException) cause;
        throw (Less4jException) wrapper.getCause();
      }

      if (cause instanceof RuntimeException) {
        RuntimeException runtimeException = (RuntimeException) cause;
        throw runtimeException;
      }

      if (cause instanceof Error) {
        Error error = (Error) cause;
        throw error;
      }

      throw new RuntimeException("Unexpected state, this should not be possible", cause);
    }
  }

  //  @Override
  //  public CompilationResult compile(LessSource source, Configuration options) throws Less4jException {
  //    final LessSource iSource = source;
  //    final Configuration iOption = options;
  //
  //    Callable<CompilationResult> task = new Callable<CompilationResult>() {
  //      public CompilationResult call() {
  //
  //        ThreadUnsafeLessCompiler compiler = new ThreadUnsafeLessCompiler();
  //        try {
  //          return compiler.compile(iSource, iOption);
  //        } catch (Less4jException ex) {
  //          throw new RuntimeException(ex);
  //        }
  //      }
  //    };
  //
  //    ExecutorService executor = Executors.newSingleThreadExecutor();
  //    Future<CompilationResult> future = executor.submit(task);
  //    try {
  //      CompilationResult result = future.get(timeout, unit);
  //      return result;
  //    } catch (InterruptedException e) {
  //      throw new BugHappened("Unexpected thread interrupt.", (ASTCssNode) null);
  //    } catch (ExecutionException e) {
  //      throw (Less4jException) e.getCause().getCause();
  //    } catch (TimeoutException e) {
  //      if (future.cancel(true)) {
  ////        ProblemsHandler problemsHandler = new ProblemsHandler();
  ////        problemsHandler.unableToFinish(lessStyleSheet, ex);
  ////        return createEmptyCompilationResult();
  //        return getResult(future);
  //      }
  //      return getResult(future);
  //    }
  //
  //  }
//
//  private CompilationResult getResult(Future<CompilationResult> future) throws Less4jException {
//    try {
//      return future.get();
//    } catch (CancellationException ex) {
//      System.out.println(ex);
//      return null;
//    } catch (InterruptedException e1) {
//      throw new BugHappened("Unexpected thread interrupt.", (ASTCssNode) null);
//    } catch (ExecutionException e1) {
//      throw (Less4jException) e1.getCause().getCause();
//    }
//  }

  @SuppressWarnings("serial")
  private class Less4jRuntimeException extends RuntimeException {

    public Less4jRuntimeException(Less4jException ex) {
      super(ex);
    }
    
  }
}
