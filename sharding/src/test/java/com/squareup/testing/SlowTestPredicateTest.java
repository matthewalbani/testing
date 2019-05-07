// Copyright 2015 Square, Inc.
package com.squareup.testing;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;
import org.junit.runner.Runner;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class SlowTestPredicateTest {

  private SlowTestPredicate predicate = new SlowTestPredicate();

  private static class NotSlowTest {}

  @Category(SlowTests.class)
  private static class SlowTest {}

  @Category(SlowTests.class)
  private static abstract class SlowRunner extends Runner {}

  private static abstract class NotSlowRunner extends Runner {}

  @RunWith(SlowRunner.class)
  private static class TestWithSlowRunner {}

  @RunWith(NotSlowRunner.class)
  private static class TestWithNotSlowRunner {}

  @RunWith(NotSlowRunner.class)
  @Category(SlowTests.class)
  private static class SlowTestWithNotSlowRunner {}

  @RunWith(SlowRunner.class)
  @Category(SlowTests.class)
  private static class SlowTestWithSlowRunner {}

  @Test public void test() {
    assertFalse(predicate.test(NotSlowTest.class));
    assertFalse(predicate.test(TestWithNotSlowRunner.class));

    assertTrue(predicate.test(SlowTest.class));
    assertTrue(predicate.test(TestWithSlowRunner.class));
    assertTrue(predicate.test(SlowTestWithNotSlowRunner.class));
    assertTrue(predicate.test(SlowTestWithSlowRunner.class));
  }
}
