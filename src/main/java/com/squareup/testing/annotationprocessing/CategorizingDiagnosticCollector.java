package com.squareup.testing.annotationprocessing;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.List;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticCollector;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileObject;

/**
 * A collector of diagnostic messages emitted by the compiler. Unlike {@link DiagnosticCollector},
 * this class categorizes the diagnostic messages by kind.
 */
public class CategorizingDiagnosticCollector {

  /** The list of all diagnostics received so far. */
  private final List<Diagnostic<? extends JavaFileObject>> allDiagnostics;

  /** Lists of all diagnostics received so far, categorized by kind. */
  private final ListMultimap<Kind, Diagnostic<? extends JavaFileObject>> categorizedDiagnostics;

  /** Constructs a new collector. Diagnostics will be printed to {@code stderr}. */
  public CategorizingDiagnosticCollector() {
    allDiagnostics = Lists.newArrayList();
    categorizedDiagnostics = ArrayListMultimap.create();
  }

  /**
   * Returns a {@link DiagnosticListener} that prints diagnostic messages and records them to the
   * collector's internal structures. Diagnostics will be printed to standard error.
   *
   * @return the listener
   */
  DiagnosticListener<JavaFileObject> getListener() {
    return getListener(null);
  }

  /**
   * Returns a {@link DiagnosticListener} that prints diagnostic messages and records them to the
   * collector's internal structures. Diagnostics will be printed to the specified writer.
   *
   * @param writer the writer, for printing diagnostics
   * @return the listener
   */
  DiagnosticListener<JavaFileObject> getListener(Writer writer) {
    final Writer output = writer == null ? new OutputStreamWriter(System.err) : writer;
    return new DiagnosticListener<JavaFileObject>() {
      @Override
      public void report(Diagnostic<? extends JavaFileObject> diagnostic) {
        synchronized (CategorizingDiagnosticCollector.this) {
          allDiagnostics.add(diagnostic);
          categorizedDiagnostics.put(diagnostic.getKind(), diagnostic);
        }
        try {
          output.write(diagnostic.getMessage(null) + "\n");
          output.flush();
        } catch (IOException e) {
          // Don't really want to throw here since we've captured
          // the diagnostic. Wish there was something better to do...
          e.printStackTrace();
        }
      }
    };
  }

  /**
   * Gets a view of all diagnostics collected so far.
   *
   * @return a collection of diagnostics
   */
  public synchronized Collection<Diagnostic<? extends JavaFileObject>> getAllDiagnostics() {
    return ImmutableList.copyOf(allDiagnostics);
  }

  /**
   * Gets a view of diagnostics of a given kind collected so far.
   *
   * @param kind the kind of diagnostic to return
   * @return a collection of diagnostics
   */
  public synchronized Collection<Diagnostic<? extends JavaFileObject>> getDiagnostics(
      Diagnostic.Kind kind) {
    return ImmutableList.copyOf(categorizedDiagnostics.get(kind));
  }
}
