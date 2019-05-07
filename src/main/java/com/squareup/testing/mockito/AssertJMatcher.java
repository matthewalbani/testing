package com.squareup.testing.mockito;

import java.util.function.Consumer;
import org.assertj.core.matcher.AssertionMatcher;
import org.mockito.Mockito;
import org.mockito.hamcrest.MockitoHamcrest;

public final class AssertJMatcher {
  /**
   * Allows for AssertJ assertions in {@link Mockito#argThat}.
   *
   * Example:
   *
   * <pre>{@code
   * verify(aMock).aMethod(argThat(actual -> {
   *   assertThat(actual).containsExactlyInAnyOrder(aValue, anotherValue)));
   *   assertThat(actual.getList()).isEmpty()));
   * }));
   * }</pre>
   *
   * As opposed to {@link Mockito#argThat} which expects a boolean condition.
   */
  public static <T> T argThat(Consumer<T> assertions) {
    return MockitoHamcrest.argThat(new AssertionMatcher<T>() {
      @Override public void assertion(T actual) throws AssertionError {
        assertions.accept(actual);
      }
    });
  }
}
