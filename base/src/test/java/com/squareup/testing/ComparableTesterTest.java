package com.squareup.testing;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ComparableTesterTest {
  @Test
  public void noComparableGroups() {
    new ComparableTester<Integer>()
        .testComparable();
  }

  @Test
  public void oneComparableGroup() {
    new ComparableTester<Integer>()
        .nextEqualGroup()
        .testComparable();

    new ComparableTester<Integer>()
        .nextEqualGroup(1)
        .testComparable();

    new ComparableTester<Integer>()
        .nextEqualGroup(1, 1)
        .testComparable();

    new ComparableTester<Integer>()
        .nextEqualGroup(2, 2, 2)
        .testComparable();
  }

  @Test
  public void oneComparableGroupWithNonComparableData() {
    assertTesterFailsWithMessage(
        new ComparableTester<Integer>()
            .nextEqualGroup(1, 2),
        "expected compareTo for 1 and 2 to be 0"
    );
  }

  @Test
  public void multipleComparableGroups() {
    new ComparableTester<Integer>()
        .nextEqualGroup(1)
        .nextEqualGroup()
        .nextEqualGroup(2, 2)
        .nextEqualGroup(3, 3, 3, 3)
        .testComparable();
  }

  @Test
  public void multipleComparableGroupsOutOfOrder_1() {
    assertTesterFailsWithMessage(
        new ComparableTester<Integer>()
            .nextEqualGroup(2, 2)
            .nextEqualGroup(1),
        "expected compareTo for 2 and 1 to be < 0");
  }

  @Test
  public void multipleComparableGroupsOutOfOrder_2() {
    assertTesterFailsWithMessage(
        new ComparableTester<Integer>()
            .nextEqualGroup(1, 1)
            .nextEqualGroup(2, 2, 2)
            .nextEqualGroup(-456),
        "expected compareTo for 1 and -456 to be < 0");
  }

  static abstract class BaseComparable<T extends BaseComparable> implements Comparable<T> {
    protected final Integer value;

    BaseComparable(int value) {
      this.value = value;
    }

    @Override public int compareTo(T other) {
      if (other == null) throw new NullPointerException();
      return value.compareTo(other.value);
    }

    @Override public String toString() {
      return String.format("%s(%s)", getClass().getSimpleName(), value.toString());
    }
  }

  static class IntComparable extends BaseComparable<IntComparable> {
    IntComparable(int value) {
      super(value);
    }
  }

  @Test
  public void userClass() {
    new ComparableTester<IntComparable>()
        .nextEqualGroup(new IntComparable(1))
        .nextEqualGroup(new IntComparable(2), new IntComparable(2))
        .testComparable();
  }

  /** compareTo is correct but equals always returns false */
  @SuppressWarnings("EqualsHashCode")
  static class NeverEqual extends BaseComparable<NeverEqual> {
    NeverEqual(int value) {
      super(value);
    }

    @Override public boolean equals(Object other) {
      return false;
    }
  }

  @Test
  public void withoutEqualsCheck() {
    new ComparableTester<NeverEqual>()
        .nextEqualGroup(new NeverEqual(1), new NeverEqual(1))
        .nextEqualGroup(new NeverEqual(2))
        .testComparable();
  }

  @Test
  public void withEqualsCheckPassing() {
    new ComparableTester<Integer>(true)
        .nextEqualGroup(1)
        .nextEqualGroup(2, 2)
        .testComparable();
  }

  @Test
  public void withEqualsCheckFailing_1() {
    assertTesterFailsWithMessage(
        new ComparableTester<NeverEqual>(true)
            .nextEqualGroup(new NeverEqual(1), new NeverEqual(1))
            .nextEqualGroup(new NeverEqual(2)),
        "expected NeverEqual(1) and NeverEqual(1) to be equal");
  }

  @SuppressWarnings("EqualsHashCode")
  static class AlwaysEqual extends BaseComparable<AlwaysEqual> {
    AlwaysEqual(int value) {
      super(value);
    }

    @Override public boolean equals(Object other) {
      return true;
    }
  }

  @Test
  public void withEqualsCheckFailing_2() {
    assertTesterFailsWithMessage(
        new ComparableTester<AlwaysEqual>(true)
            .nextEqualGroup(new AlwaysEqual(1), new AlwaysEqual(1))
            .nextEqualGroup(new AlwaysEqual(2)),
        "expected equals for AlwaysEqual(1) and AlwaysEqual(2) to be false");
  }

  static class DoesNotCompareToSelf extends BaseComparable<DoesNotCompareToSelf> {
    DoesNotCompareToSelf(int value) {
      super(value);
    }

    @Override public int compareTo(DoesNotCompareToSelf other) {
      return this == other
          ? -1
          : super.compareTo(other);
    }
  }

  @Test
  public void testsCompareToSelf() {
    assertTesterFailsWithMessage(
        new ComparableTester<DoesNotCompareToSelf>()
            .nextEqualGroup(new DoesNotCompareToSelf(1))
            .nextEqualGroup(new DoesNotCompareToSelf(2), new DoesNotCompareToSelf(2)),
        "expected compareTo for DoesNotCompareToSelf(1) and DoesNotCompareToSelf(1) to be 0");
  }

  static class CanCompareToNull extends BaseComparable<CanCompareToNull> {
    CanCompareToNull(int value) {
      super(value);
    }

    @Override public int compareTo(CanCompareToNull other) {
      return other == null
          ? 0
          : super.compareTo(other);
    }
  }

  @Test
  public void testsCompareToNull() {
    assertTesterFailsWithMessage(
        new ComparableTester<CanCompareToNull>()
            .nextEqualGroup(new CanCompareToNull(1)),
        "expected a NullPointerException when calling compareTo on CanCompareToNull(1) with null");
  }

  static class CompareToNullThrowsWrongExceptions extends BaseComparable<CompareToNullThrowsWrongExceptions> {
    CompareToNullThrowsWrongExceptions(int value) {
      super(value);
    }

    @Override public int compareTo(CompareToNullThrowsWrongExceptions other) {
      if (other == null) {
        throw new RuntimeException();
      }
      return 0;
    }
  }

  @Test
  public void compareToNullThrowsWrongExceptions() {
    assertTesterFailsWithMessage(
        new ComparableTester<CompareToNullThrowsWrongExceptions>()
            .nextEqualGroup(new CompareToNullThrowsWrongExceptions(1)),
        "expected a NullPointerException when calling compareTo on CompareToNullThrowsWrongExceptions(1) with null");
  }

  static class ConstantCompareTo extends BaseComparable<ConstantCompareTo> {
    private static int compareToValue;

    ConstantCompareTo(int compareToValue) {
      super(compareToValue);
      ConstantCompareTo.compareToValue = compareToValue;
    }

    @Override public int compareTo(ConstantCompareTo constantCompareTo) {
      super.compareTo(constantCompareTo);
      return compareToValue;
    }
  }

  @Test
  public void testsCommutativeCompareToWhenEqual() {
    new ComparableTester<ConstantCompareTo>()
        .nextEqualGroup(new ConstantCompareTo(0), new ConstantCompareTo(0))
        .testComparable();
  }

  @Test
  public void testsCommutativeCompareToWhenNotEqual() {
    assertTesterFailsWithMessage(
        new ComparableTester<ConstantCompareTo>()
            .nextEqualGroup(new ConstantCompareTo(-1)),
        "expected compareTo for ConstantCompareTo(-1) and ConstantCompareTo(-1) to be 0");
  }

  @SuppressWarnings("AssertionFailureIgnored")
  private <T extends Comparable<T>> void assertTesterFailsWithMessage(
      ComparableTester<T> tester, String message) {
    try {
      tester.testComparable();
      fail(String.format("Expected AssertionError with message: %s", message));
    } catch (AssertionError expected) {
      assertEquals(expected.getMessage(), message);
    }
  }
}
