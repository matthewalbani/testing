package com.squareup.testing.mockito;

import com.google.common.base.Equivalence;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.squareup.common.EquivalentForTesting;
import com.squareup.common.TestingEquivalents;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class EquivalentForTestingCollectionMatcherTest {

  /**
   * A test object that provides a simple getter method.
   */
  private static class TestObject implements EquivalentForTesting<TestObject> {

    private final int value;

    public TestObject(int value) {
      this.value = value;
    }

    public int getValue() {
      return value;
    }

    @Override public Equivalence.Wrapper<TestObject> equivalenceForTesting() {
      return TestingEquivalence.INSTANCE.wrap(this);
    }

    private static class TestingEquivalence extends Equivalence<TestObject> {
      private static final TestingEquivalence INSTANCE = new TestingEquivalence();

      @Override protected boolean doEquivalent(TestObject o0, TestObject o1) {
        return Objects.equal(o0.value, o1.value);
      }

      @Override protected int doHash(TestObject o) {
        return TestingEquivalents.hashCode(o.value);
      }
    }
  }

  /**
   * A test class that uses {@link GetterInternal} to get values of given objects.
   */
  private static class Getter {

    private final GetterInternal getterInternal;

    public Getter(GetterInternal getterInternal) {
      this.getterInternal = getterInternal;
    }

    public Collection<Integer> getValues(Collection<TestObject> os) {
      if (os == null) {
        return getterInternal.getValues(null);
      }

      // Copy objects and call getValuesInternal().
      Collection<TestObject> copied = Sets.newHashSet();
      for (TestObject o : os) {
        copied.add(new TestObject(o.getValue()));
      }
      return getterInternal.getValues(copied);
    }
  }

  /**
   * A class that is accessed from {@link Getter}. This class will be mocked.
   */
  private static class GetterInternal {

    public Collection<Integer> getValues(Collection<TestObject> os) {
      throw new UnsupportedOperationException();
    }

  }

  @Mock GetterInternal getterInternal;

  @Before public void before() {
    MockitoAnnotations.initMocks(this);
  }

  @Test public void match() {
    TestObject o0 = new TestObject(0);
    TestObject o1 = new TestObject(1);

    Getter getter = new Getter(getterInternal);
    when(getterInternal.getValues(
        EquivalentForTestingCollectionMatcher.equivalentForTesting(Sets.newHashSet(o0, o1))))
        .thenReturn(Lists.newArrayList(0, 1));
    when(getterInternal.getValues(
        EquivalentForTestingCollectionMatcher.<TestObject, Collection<TestObject>>equivalentForTesting(null)))
        .thenReturn(null);

    assertThat(getter.getValues(ImmutableList.of(o0, o1))).containsExactly(0, 1);
    assertThat(getter.getValues(null)).isNull();
  }

}
