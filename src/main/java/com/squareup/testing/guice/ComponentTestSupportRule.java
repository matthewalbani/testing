// Copyright 2013, Square, Inc.

package com.squareup.testing.guice;

import com.google.inject.Inject;
import java.util.Set;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/** JUnit {@link org.junit.Rule} handling {@link ComponentTestSupport} */
public class ComponentTestSupportRule implements TestRule {
  private final Set<ComponentTestSupport> supports;

  @Inject
  public ComponentTestSupportRule(Set<ComponentTestSupport> supports) {
    this.supports = supports;
  }

  @Override public Statement apply(final Statement base, final Description description) {
    return new Statement() {
      @Override public void evaluate() throws Throwable {
        for (ComponentTestSupport support : supports) {
          support.beforeTest(base, description);
        }
        base.evaluate();
      }
    };
  }
}
