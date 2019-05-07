package com.squareup.testing;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tester for the {@link Comparable#compareTo(Object)} method of a {@link Comparable} class.
 *
 * This tests: <ul> <li>Within a comparable group, for every pair (a, b) of objects a.compareTo(b)
 * == b.compareTo(a) == 0</li> <li>For every object in a comparable group, compareTo with any object
 * in a lesser comparable group returns a negative integer and compareTo with any object in a
 * greater comparable group returns a positive integer.</li> <li>For every object, compareTo throws
 * an exception when given null</li> <li>If checkEquals is true, for every pair (a, b) of objects
 * (a.compareTo(b) == 0) == a.equals(b)</li> </ul>
 */
public class ComparableTester<T extends Comparable<T>> {
  private final boolean checkEquals;
  private final List<List<T>> comparableGroups = Lists.newArrayList();

  /** Creates a ComparableTester that does not check that compareTo and equals are consistent. */
  public ComparableTester() {
    this.checkEquals = false;
  }

  /**
   * Creates a ComparableTester that optionally checks that compareTo and equals are consistent.
   *
   * @param checkEquals if true, checks that compareTo and equals are consistent. That is,
   * (a.compareTo(b) == 0) == a.equals(b)
   */
  public ComparableTester(boolean checkEquals) {
    this.checkEquals = checkEquals;
  }

  /**
   * Add a list of objects where compareTo called for any two within the list should return 0.
   *
   * Given any object o in the given list and any object p in a previously added group,
   * o.compareTo(p) should be > 0 and p.compareTo(o) should be < 0.
   */
  public ComparableTester<T> nextEqualGroup(T... comparableGroup) {
    comparableGroups.add(Lists.newArrayList(comparableGroup));
    return this;
  }

  /**
   * Tests compareTo for all the objects registered by the {@link #nextEqualGroup(Comparable[])}
   * method. Iff compareTo validations fail, an exception is thrown.
   */
  public void testComparable() {
    for (int i = 0; i < comparableGroups.size(); i++) {
      List<T> comparableGroup = comparableGroups.get(i);
      assertAllCompareToZero(comparableGroup);

      if (i < comparableGroups.size() - 1) {
        List<List<T>> greaterObjects = comparableGroups.subList(i + 1, comparableGroups.size());
        assertCompareToForAll(comparableGroup, Iterables.concat(greaterObjects));
      }
    }
  }

  private void assertCompareToForAll(Iterable<T> lesserObjects, Iterable<T> greaterObjects) {
    for (T lesserObject : lesserObjects) {
      for (T greaterObject : greaterObjects) {
        assertCompareTo(lesserObject, greaterObject);
      }
    }
  }

  private void assertCompareTo(T lesserObject, T greaterObject) {
    assertTrue(
        String.format("expected compareTo for %s and %s to be < 0", lesserObject, greaterObject),
        lesserObject.compareTo(greaterObject) < 0);

    assertTrue(
        String.format("expected compareTo for %s and %s to be > 0", greaterObject, lesserObject),
        greaterObject.compareTo(lesserObject) > 0);

    if (checkEquals) {
      assertFalse(
          String.format("expected equals for %s and %s to be false", lesserObject, greaterObject),
          lesserObject.equals(greaterObject));
    }
  }

  private void assertAllCompareToZero(List<T> objects) {
    for (int i = 0; i < objects.size(); i++) {
      T object = objects.get(i);
      assertCannotCompareToNull(object);

      for (int j = i; j < objects.size(); j++) {
        T otherObject = objects.get(j);
        assertTrue(
            String.format("expected compareTo for %s and %s to be 0", object, otherObject),
            object.compareTo(otherObject) == 0);
        assertTrue(
            String.format("expected compareTo for %s and %s to be 0", otherObject, object),
            otherObject.compareTo(object) == 0);

        if (checkEquals) {
          assertTrue(
              String.format("expected %s and %s to be equal", object, otherObject),
              object.equals(otherObject)
          );
          assertTrue(
              String.format("expected %s and %s to be equal", object, otherObject),
              otherObject.equals(object)
          );
        }
      }
    }
  }

  private void assertCannotCompareToNull(T object) {
    try {
      object.compareTo(null);
    } catch (NullPointerException expected) {
      return;
    } catch (Exception unexpected) {
    }

    fail(String.format("expected a NullPointerException when calling compareTo on %s with null",
        object));
  }
}
