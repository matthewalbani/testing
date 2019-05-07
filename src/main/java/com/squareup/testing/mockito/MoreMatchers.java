package com.squareup.testing.mockito;

import java.util.function.Predicate;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/** Additional Mockito {@link Matcher}s */
public final class MoreMatchers {
  /** @return A {@link Matcher} based on a {@link Predicate} block */
  public static <T> Matcher<T> matches(Predicate<T> p) {
    return new BaseMatcher<T>() {
      @Override public void describeTo(Description description) {
        description.appendText(getClass().getSimpleName());
      }

      @SuppressWarnings("unchecked")
      @Override public boolean matches(Object argument) {
        return argument != null && p.test((T) argument);
      }
    };
  }

  private MoreMatchers() {}
}
