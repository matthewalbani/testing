// Copyright 2017 Square, Inc.
package com.squareup.testing.fixtures;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

public class ConcreteFakeRule implements MethodRule {
  @Override public Statement apply(Statement base, FrameworkMethod method, Object target) {
    return new Statement() {
      @Override public void evaluate() throws Throwable {
        ConcreteFakeAnnotations.initConcreteFakes(target);
        base.evaluate();
      }
    };
  }
}
