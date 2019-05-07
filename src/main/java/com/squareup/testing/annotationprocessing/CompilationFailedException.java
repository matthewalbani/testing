package com.squareup.testing.annotationprocessing;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.JavaFileObject;

/**
 * An exception thrown if compilation fails. If a test case fails due to a problem in the Java
 * compilation step (vs. an exception or assertion in the test case itself), an instance of this
 * exception is thrown.
 */
public class CompilationFailedException extends RuntimeException {

  private final ImmutableList<Diagnostic<? extends JavaFileObject>> diagnostics;

  /**
   * Creates a new exception with the specified message and set of error diagnostics.
   *
   * @param message an error message
   * @param diagnostics diagnostics emitted by the compiler with a kind of
   *    {@link javax.tools.Diagnostic.Kind#ERROR}
   */
  public CompilationFailedException(String message,
      Collection<Diagnostic<? extends JavaFileObject>> diagnostics) {
    super(message);
    this.diagnostics = ImmutableList.copyOf(diagnostics);
  }

  /**
   * Creates a new exception with the specified message, cause, and set of error diagnostics.
   *
   * @param message an error message
   * @param cause the cause of the failure
   * @param diagnostics diagnostics emitted by the compiler with a kind of
   *    {@link javax.tools.Diagnostic.Kind#ERROR}
   */
  public CompilationFailedException(String message, Throwable cause,
      Collection<Diagnostic<? extends JavaFileObject>> diagnostics) {
    super(message, cause);
    this.diagnostics = ImmutableList.copyOf(diagnostics);
  }

  /**
   * Creates a new exception with the specified cause and set of error diagnostics.
   *
   * @param cause the cause of the failure
   * @param diagnostics diagnostics emitted by the compiler with a kind of
   *    {@link javax.tools.Diagnostic.Kind#ERROR}
   */
  public CompilationFailedException(Throwable cause,
      Iterable<Diagnostic<? extends JavaFileObject>> diagnostics) {
    super(cause);
    this.diagnostics = ImmutableList.copyOf(diagnostics);
  }

  /**
   * Retrieves the error diagnostics associated with this compilation failure.
   *
   * @return the set of error diagnostics emitted from the compiler
   */
  public List<Diagnostic<? extends JavaFileObject>> getDiagnostics() {
    return diagnostics;
  }
}
