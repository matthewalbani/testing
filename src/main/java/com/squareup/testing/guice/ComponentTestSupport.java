package com.squareup.testing.guice;

import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/** Allows a component to add test support hooks */
// TODO(mattmihic): Replace this, either with composable JUnit {@link TestRule}s,
// or a lower level test hook that any be run from any tests
public interface ComponentTestSupport {
  /** Called prior to the start of a test */
  void beforeTest(Statement base, Description description);
}
