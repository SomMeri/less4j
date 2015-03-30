package com.github.sommeri.less4j.core.compiler.scopes;

import com.github.sommeri.less4j.utils.debugonly.DebugUtils;

public class InScopeSnapshotRunner {
  private final IScope scope;

  public InScopeSnapshotRunner(IScope scope) {
    this.scope = scope;
  }

  /**
   * Convenience method. See {@link #runInLocalDataSnapshot(ITask)}
   * 
   */
  public static void runInLocalDataSnapshot(IteratedScope scope, ITask task) {
    runInLocalDataSnapshot(scope.getScope(), task);
  }
  
  /**
   * Convenience method. See {@link #runInLocalDataSnapshot(ITask)}
   * 
   */
  public static void runInLocalDataSnapshot(IScope scope, ITask task) {
    InScopeSnapshotRunner runner = new InScopeSnapshotRunner(scope);
    runner.runInLocalDataSnapshot(task);
  }

  /**
   * Convenience method. See {@link #runInLocalDataSnapshot(IFunction)}
   * 
   */
  public static <T> T runInLocalDataSnapshot(IScope scope, IFunction<T> task) {
    InScopeSnapshotRunner runner = new InScopeSnapshotRunner(scope);
    return runner.runInLocalDataSnapshot(task);
  }

  public static int id = 0;
  /**
   * Create local data snapshot on the scope and runs the task. Use this method to make sure
   * that each local data snapshot is closed on proper place and regardless of thrown exceptions.
   * 
   */
  public void runInLocalDataSnapshot(ITask task) {
//    DebugUtils debugUtils = new DebugUtils();
//    debugUtils.scopeTest(scope, "before snapshot " + (id++));
    scope.createCurrentDataSnapshot();
//    debugUtils.scopeTest(scope, "after snapshot " + (id++));
    try {
      task.run();
    } finally {
//      debugUtils.scopeTest(scope, "before discard " + (id++));
      scope.discardLastDataSnapshot();
//      debugUtils.scopeTest(scope, "after discard " + (id++));
    }
  }

  /**
   * Create local data snapshot on the scope and runs the task. Use this method to make sure
   * that each local data snapshot is closed on proper place and regardless of thrown exceptions.
   * 
   */
  public <T> T runInLocalDataSnapshot(IFunction<T> task) {
    
    scope.createCurrentDataSnapshot();
    try {
      return task.run();
    } finally {
      scope.discardLastDataSnapshot();
    }
  }

  /**
   * Convenience method. See {@link #runInLocalDataSnapshot(ITask)}
   * 
   */
  public static void runInOriginalDataSnapshot(IScope scope, ITask task) {
    InScopeSnapshotRunner runner = new InScopeSnapshotRunner(scope);
    runner.runInOriginalDataSnapshot(task);
  }

  /**
   * Convenience method. See {@link #runInOriginalDataSnapshot(IFunction)}
   * 
   */                 
  public static <T> T runInOriginalDataSnapshot(IScope scope, IFunction<T> task) {
    InScopeSnapshotRunner runner = new InScopeSnapshotRunner(scope);
    return runner.runInOriginalDataSnapshot(task);
  }

  /**
   * Create local data snapshot on the scope and runs the task. Use this method to make sure
   * that each local data snapshot is closed on proper place and regardless of thrown exceptions.
   * 
   */
  public void runInOriginalDataSnapshot(ITask task) {
    scope.createOriginalDataSnapshot();
    try {
      task.run();
    } finally {
      scope.discardLastDataSnapshot();
    }
  }

  /**
   * Create local data snapshot on the scope and runs the task. Use this method to make sure
   * that each local data snapshot is closed on proper place and regardless of thrown exceptions.
   * 
   */
  public <T> T runInOriginalDataSnapshot(IFunction<T> task) {
    scope.createOriginalDataSnapshot();
    try {
      return task.run();
    } finally {
      scope.discardLastDataSnapshot();
    }
  }

  public interface ITask {
    public void run();
  }

  public interface IFunction <T> {
    public T run();
  }
}
