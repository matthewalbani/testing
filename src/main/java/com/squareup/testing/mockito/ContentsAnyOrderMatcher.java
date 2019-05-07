// Copyright 2014, Square, Inc.
package com.squareup.testing.mockito;

import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Set;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Matches two collections by contents in any order.
 *
 * <p>Note: The matching is done by Set semantics so the objects in the collection
 * must behave properly in a Set (i.e proper hashCode() implementation) </p>
 *
 * @param <C> The type of the collection.
 */
public class ContentsAnyOrderMatcher<C extends Collection<?>> implements ArgumentMatcher<C> {

  private final Set<?> expected;

  /**
   * Wraps the expected collection in a {@link ContentsAnyOrderMatcher} matcher.
   */
  public static <O extends Collection<?>> O contentsAnyOrder(O expected) {
    return Mockito.argThat(new ContentsAnyOrderMatcher<O>(expected));
  }

  private ContentsAnyOrderMatcher(C expected) {
    checkNotNull(expected);
    this.expected = Sets.newHashSet(expected);
  }

  @Override public boolean matches(C argument) {
    if (argument == null) {
      return false;
    }

    return expected.equals(Sets.newHashSet((Collection) argument));
  }
}
