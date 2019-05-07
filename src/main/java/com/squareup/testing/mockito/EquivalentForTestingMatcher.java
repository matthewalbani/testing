package com.squareup.testing.mockito;

import com.squareup.common.EquivalentForTesting;
import com.squareup.common.TestingEquivalents;
import javax.annotation.Nullable;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

/**
 * An {@link ArgumentMatcher} for {@link EquivalentForTesting} instances.
 *
 * @param T The type of the entity to match.
 */
public class EquivalentForTestingMatcher<T extends EquivalentForTesting<T>>
    implements ArgumentMatcher<T> {

  private final T entity;

  /**
   * Wraps the expected in a {@link EquivalentForTesting} matcher.
   */
  public static <O extends EquivalentForTesting<O>> O equivalentForTesting(@Nullable O expected) {
    return Mockito.argThat(new EquivalentForTestingMatcher<O>(expected));
  }

  private EquivalentForTestingMatcher(T entity) {
    this.entity = entity;
  }

  @Override public boolean matches(@Nullable T argument) {
    if (argument == null) {
      return entity == null;
    }
    if (entity == null) {
      return argument == null;
    }
    return TestingEquivalents.equal(entity, argument);
  }
}
