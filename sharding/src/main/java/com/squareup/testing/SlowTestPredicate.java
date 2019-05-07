// Copyright 2015 Square, Inc.
package com.squareup.testing;

import java.util.Arrays;
import java.util.function.Predicate;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * A {@link Predicate} to determine if a test class should be considered slow.
 */
public class SlowTestPredicate implements Predicate<Class<?>> {

  /**
   * Returns true if the provided test class should be considered slow.
   *
   * <p>A class is considered slow if it is annotated with {@code Category(SlowTests.class)} or is
   * annotated with a test runner that is annotated with {@code Category(SlowTests.class)}.
   * </p>
   */
  @Override public boolean test(Class<?> clazz) {
    if (hasSlowTestAnnotation(clazz)) {
      return true;
    }
    return clazz.isAnnotationPresent(RunWith.class)
        && hasSlowTestAnnotation(clazz.getAnnotation(RunWith.class).value());
  }

  private boolean hasSlowTestAnnotation(Class<?> clazz) {
    return clazz.isAnnotationPresent(Category.class) && Arrays.asList(
        clazz.getAnnotation(Category.class).value()).contains(SlowTests.class);
  }
}
