// Copyright 2013, Square, Inc.

package com.squareup.testing.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;

import static org.assertj.core.api.Assertions.assertThat;

/** Test for failure handling in {@link AbstractGuiceTestRunner} */
public class AbstractGuiceTestRunnerErrorTest {

  public static class SimpleGuiceTestRunner extends AbstractGuiceTestRunner {

    public SimpleGuiceTestRunner(Class<?> klass) throws InitializationError {
      super(klass);
    }

    @Override protected Injector getInjector() {
      return Guice.createInjector();
    }
  }

  @RunWith(SimpleGuiceTestRunner.class)
  public static class FailingInConstructor {
    public static volatile boolean kaboom = false;
    public FailingInConstructor() {
      if (kaboom) {
        throw new RuntimeException("kaboom");
      }
    }

    @Test public void foo() {
    }
  }

  /** Test that the runner does not mask the exception thrown from the test class constructor */
  @Test public void failingInConstructor() {
    FailingInConstructor.kaboom = true;
    Result result = JUnitCore.runClasses(FailingInConstructor.class);
    assertThat(result.getFailures().get(0).getException()).hasMessageContaining("kaboom");
    FailingInConstructor.kaboom = false;
  }
}
