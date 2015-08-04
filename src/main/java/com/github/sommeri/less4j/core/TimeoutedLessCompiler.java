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

import com.github.sommeri.less4j.Less4jException;
import com.github.sommeri.less4j.LessCompiler;
import com.github.sommeri.less4j.LessSource;
import com.github.sommeri.less4j.core.ast.ASTCssNode;
import com.github.sommeri.less4j.core.problems.BugHappened;

public class TimeoutedLessCompiler implements LessCompiler {

  private final long timeout;
  private final TimeUnit unit;
  private final long afterInterruptTimeout;
  private final TimeUnit afterInterruptUnit;

  public TimeoutedLessCompiler(long timeout, TimeUnit unit) {
    this(timeout, unit, 80, TimeUnit.MILLISECONDS);
  }

  public TimeoutedLessCompiler(long timeout, TimeUnit unit, long afterInterruptTimeout, TimeUnit afterInterruptunit) {
    super();
    this.timeout = timeout;
    this.unit = unit;
    this.afterInterruptTimeout = afterInterruptTimeout;
    this.afterInterruptUnit = afterInterruptunit;
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
        CompilationResult result = future.get();
        return result;
      } else {
        executor.shutdownNow();
        executor.awaitTermination(afterInterruptTimeout, afterInterruptUnit);
        return future.get();
      }
    } catch (CancellationException e) {
      throw new BugHappened("Unexpected future cancellation.", (ASTCssNode) null);
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

  @SuppressWarnings("serial")
  private class Less4jRuntimeException extends RuntimeException {

    public Less4jRuntimeException(Less4jException ex) {
      super(ex);
    }

  }
}
