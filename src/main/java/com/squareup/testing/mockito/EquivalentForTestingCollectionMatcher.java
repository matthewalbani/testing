package com.squareup.testing.mockito;

import com.squareup.common.EquivalentForTesting;
import com.squareup.common.TestingEquivalents;
import java.util.Collection;
import javax.annotation.Nullable;
import org.mockito.ArgumentMatcher;
import org.mockito.Mockito;

/**
 * {@link ContentsAnyOrderMatcher} for objects that implements {@link EquivalentForTesting}.
 */
public class EquivalentForTestingCollectionMatcher<
    T extends EquivalentForTesting<T>,
    C extends Collection<T>>
    implements ArgumentMatcher<C> {

  private final C expected;

  /**
   * Wraps the expected collection in a {@link EquivalentForTestingCollectionMatcher} matcher.
   */
  public static <D extends EquivalentForTesting<D>, O extends Collection<D>> O equivalentForTesting(
      @Nullable O expected) {
    return Mockito.argThat(new EquivalentForTestingCollectionMatcher<D, O>(expected));
  }

  private EquivalentForTestingCollectionMatcher(C expected) {
    this.expected = expected;
  }

  @Override public boolean matches(@Nullable C argument) {
    if (argument == null) {
      return expected == null;
    }
    if (expected == null) {
      return argument == null;
    }
    return TestingEquivalents.equal(expected, argument);
  }
}
