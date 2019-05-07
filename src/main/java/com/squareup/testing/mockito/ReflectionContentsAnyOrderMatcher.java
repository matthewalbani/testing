package com.squareup.testing.mockito;

import java.util.Collection;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Matches two collections by contents in any order using reflection, optionally ignoring some
 * fields.
 */
public class ReflectionContentsAnyOrderMatcher<C extends Collection<?>>
    implements ArgumentMatcher<C> {
  private final C expected;
  private final String[] excludeFields;

  public static <O extends Collection<?>> O contentsAnyOrderIgnoring(O expected,
      String... excludeFields) {
    return Mockito.argThat(new ReflectionContentsAnyOrderMatcher<>(expected, excludeFields));
  }
  public static <O extends Collection<?>> O contentsAnyOrder(O expected) {
    return Mockito.argThat(new ReflectionContentsAnyOrderMatcher<>(expected));
  }

  private ReflectionContentsAnyOrderMatcher(C expected, String... excludeFields) {
    this.expected = checkNotNull(expected);
    this.excludeFields = excludeFields;
  }

  @Override public boolean matches(C argument) {
    if (argument == null) {
      return false;
    }

    for (Object e : expected ) {
      boolean anyMatch = false;
      for (Object a : argument) {
        boolean match = EqualsBuilder.reflectionEquals(e, a, excludeFields);
        if (match) {
          anyMatch = true;
          break;
        }
      }
      if (!anyMatch) {
        return false;
      }
    }
    return true;
  }
}
