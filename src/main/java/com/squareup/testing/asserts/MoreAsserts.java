// Copyright 2013 Square, Inc.
package com.squareup.testing.asserts;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.squareup.common.EquivalentForTesting;
import com.squareup.common.json.DateTimeTypeConverter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Assert;

/**
 * Additional helpful assertions you can use in testing.
 */
public class MoreAsserts {
  private MoreAsserts() {}

  /**
   * Asserts value objects object graphs are equivalent by comparing two serialized json
   * representations.
   *
   * Does not support circular references. (Could use xstream which does).
   *
   * Example object output that is then diffed. Looks elegant in diff-mode in Intellij.
   * <pre>
   * "approvalCode": 123456,
   * "tid": {
   *   "categoryIds": [1,2,3,4],
   *   "transactionId": {
   *     "__CLASS": "class java.lang.String",
   *     "__VALUE": "012345678"
   *   },
   *   "__CLASS": "com.squareup.example.MyClass"
   * }
   * </pre>
   */
  public static void assertDeepObjectEquivalence(Object expected, Object actual) {
    assertDeepObjectEquivalence(null, expected, actual);
  }

  /**
   * Asserts value objects object graphs are equivalent by comparing two serialized json
   * representations.
   *
   * Does not support circular references. (Could use xstream which does).
   *
   * Example object output that is then diffed. Looks elegant in diff-mode in Intellij.
   * <pre>
   * "approvalCode": 123456,
   * "tid": {
   *   "categoryIds": [1,2,3,4],
   *   "transactionId": {
   *     "__CLASS": "class java.lang.String",
   *     "__VALUE": "012345678"
   *   },
   *   "__CLASS": "com.squareup.example.MyClass"
   * }
   * </pre>
   */
  public static void assertDeepObjectEquivalence(String message, Object expected, Object actual) {
    Gson gsonComparer = new GsonBuilder()
        .setPrettyPrinting()
        .serializeNulls()
        .registerTypeAdapterFactory(new ClassInformationSerializer())
        .registerTypeAdapter(DateTime.class, new DateTimeTypeConverter())
        .create();

    Assert.assertEquals(message, gsonComparer.toJson(expected), gsonComparer.toJson(actual));
  }


  // Below is from Google's open source testing package android.test library, thus licensed as such.

  /*
   * Copyright (C) 2007 The Android Open Source Project
   *
   * Licensed under the Apache License, Version 2.0 (the "License");
   * you may not use this file except in compliance with the License.
   * You may obtain a copy of the License at
   *
   *      http://www.apache.org/licenses/LICENSE-2.0
   *
   * Unless required by applicable law or agreed to in writing, software
   * distributed under the License is distributed on an "AS IS" BASIS,
   * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   * See the License for the specific language governing permissions and
   * limitations under the License.
   */

  /**
   * Asserts that the class  {@code expected} is assignable from the object {@code actual}. This
   * verifies {@code expected} is a parent class or a interface that {@code actual} implements.
   */
  public static void assertAssignableFrom(Class<?> expected, Object actual) {
    assertAssignableFrom(expected, actual.getClass());
  }

  /**
   * Asserts that class {@code expected} is assignable from the class {@code actual}. This
   * verifies {@code expected} is a parent class or a interface that {@code actual} implements.
   */
  public static void assertAssignableFrom(Class<?> expected, Class<?> actual) {
    Assert.assertTrue(
        "Expected " + expected.getCanonicalName() +
            " to be assignable from actual class " + actual.getCanonicalName(),
        expected.isAssignableFrom(actual));
  }

  /**
   * Asserts that {@code actual} is not equal {@code unexpected}, according to both {@code ==} and
   * {@link Object#equals}.
   */
  public static void assertNotEqual(
      String message, Object unexpected, Object actual) {
    if (equal(unexpected, actual)) {
      failEqual(message, unexpected);
    }
  }

  /** Variant of {@link #assertNotEqual(String, Object, Object)} using a generic message. */
  public static void assertNotEqual(Object unexpected, Object actual) {
    assertNotEqual(null, unexpected, actual);
  }

  /** Asserts that two sets contain the same elements. */
  public static void assertEquals(
      String message, Set<?> expected, Set<?> actual) {
    Set<Object> onlyInExpected = new HashSet<Object>(expected);
    onlyInExpected.removeAll(actual);
    Set<Object> onlyInActual = new HashSet<Object>(actual);
    onlyInActual.removeAll(expected);
    if (onlyInExpected.size() != 0 || onlyInActual.size() != 0) {
      Set<Object> intersection = new HashSet<Object>(expected);
      intersection.retainAll(actual);
      failWithMessage(
          message,
          "Sets do not match.\nOnly in expected: " + onlyInExpected
              + "\nOnly in actual: " + onlyInActual
              + "\nIntersection: " + intersection);
    }
  }

  /** Asserts that two sets contain the same elements. */
  public static void assertEquals(Set<? extends Object> expected, Set<? extends Object> actual) {
    assertEquals(null, expected, actual);
  }

  /**
   * Asserts that {@code expectedRegex} exactly matches {@code actual} and fails with {@code
   * message} if it does not.  The MatchResult is returned in case the test needs access to any
   * captured groups.  Note that you can also use this for a literal string, by wrapping your
   * expected string in {@link Pattern#quote}.
   */
  public static MatchResult assertMatchesRegex(
      String message, String expectedRegex, String actual) {
    if (actual == null) {
      failNotMatches(message, expectedRegex, actual);
    }
    Matcher matcher = getMatcher(expectedRegex, actual);
    if (!matcher.matches()) {
      failNotMatches(message, expectedRegex, actual);
    }
    return matcher;
  }

  /** Variant of {@link #assertMatchesRegex(String, String, String)} using a generic message. */
  public static MatchResult assertMatchesRegex(
      String expectedRegex, String actual) {
    return assertMatchesRegex(null, expectedRegex, actual);
  }

  /**
   * Asserts that {@code expectedRegex} matches any substring of {@code actual} and fails with
   * {@code message} if it does not.  The Matcher is returned in case the test needs access to any
   * captured groups.  Note that you can also use this for a literal string, by wrapping your
   * expected string in {@link Pattern#quote}.
   */
  public static MatchResult assertContainsRegex(
      String message, String expectedRegex, String actual) {
    if (actual == null) {
      failNotContains(message, expectedRegex, actual);
    }
    Matcher matcher = getMatcher(expectedRegex, actual);
    if (!matcher.find()) {
      failNotContains(message, expectedRegex, actual);
    }
    return matcher;
  }

  /** Variant of {@link #assertContainsRegex(String, String, String)} using a generic message. */
  public static MatchResult assertContainsRegex(
      String expectedRegex, String actual) {
    return assertContainsRegex(null, expectedRegex, actual);
  }

  /**
   * Asserts that {@code expectedRegex} does not exactly match {@code actual}, and fails with
   * {@code message} if it does. Note that you can also use this for a literal string, by wrapping
   * your expected string in {@link Pattern#quote}.
   */
  public static void assertNotMatchesRegex(
      String message, String expectedRegex, String actual) {
    Matcher matcher = getMatcher(expectedRegex, actual);
    if (matcher.matches()) {
      failMatch(message, expectedRegex, actual);
    }
  }

  /** Variant of {@link #assertNotMatchesRegex(String, String, String)} using a generic message. */
  public static void assertNotMatchesRegex(
      String expectedRegex, String actual) {
    assertNotMatchesRegex(null, expectedRegex, actual);
  }

  /**
   * Asserts that {@code expectedRegex} does not match any substring of {@code actual}, and fails
   * with {@code message} if it does.  Note that you can also use this for a literal string, by
   * wrapping your expected string in {@link Pattern#quote}.
   */
  public static void assertNotContainsRegex(
      String message, String expectedRegex, String actual) {
    Matcher matcher = getMatcher(expectedRegex, actual);
    if (matcher.find()) {
      failContains(message, expectedRegex, actual);
    }
  }

  /** Variant of {@link #assertNotContainsRegex(String, String, String)} using a generic message. */
  public static void assertNotContainsRegex(
      String expectedRegex, String actual) {
    assertNotContainsRegex(null, expectedRegex, actual);
  }

  /**
   * Asserts that all the reference fields of a POJO are non-null.
   *
   * @param pojo some Java object with fields
   */
  public static void assertAllFieldsNotNull(Object pojo) {
    assertExactlyFieldsThatAreNull(pojo);
  }

  /**
   * Asserts that all the reference fields of a POJO (plain old Java Object) are non-null, except
   * for the given field names. Useful for catching regressions induced by adding fields to the POJO.
   *
   * Example usage:
   * <pre>
   * Giraffe giraffe = new Giraffe.Builder()
   *     .height(Meters.of(5L)
   *     .netWorth(Money.of(ISOCurrency.USD, 1_000_000L))
   *     .build();
   * assertExactlyFieldsThatAreNull(giraffe, "birthday");
   * </pre>
   * @param pojo some Java object with fields
   */
  public static void assertExactlyFieldsThatAreNull(Object pojo, String... excluded) {
    List<String> excludedFields = Arrays.asList(excluded);
    // Verify that we're not excluding a field that doesn't exist
    for (String fieldName : excludedFields) {
      try {
        pojo.getClass().getDeclaredField(fieldName);
      } catch (NoSuchFieldException e) {
        throw new IllegalArgumentException(String.format("Class %s has no field named %s",
            pojo.getClass().getSimpleName(), fieldName));
      }
    }
    for (Field field : pojo.getClass().getDeclaredFields()) {
      field.setAccessible(true);
      Object ref;
      try {
        ref = field.get(pojo);
      } catch (IllegalAccessException e) {
        throw new RuntimeException(e);
      }
      if (excludedFields.contains(field.getName())) {
        Assert.assertNull(ref);
      } else {
        Assert.assertNotNull(ref);
      }
    }
  }

  /**
   * Asserts that {@code actual} contains precisely the elements {@code expected}, and in the same
   * order.
   */
  public static void assertContentsInOrder(
      String message, Iterable<?> actual, Object... expected) {
    ArrayList actualList = new ArrayList();
    for (Object o : actual) {
      actualList.add(o);
    }
    Assert.assertEquals(message, Arrays.asList(expected), actualList);
  }

  /** Variant of assertContentsInOrder(String, Iterable<?>, Object...) using a generic message. */
  public static void assertContentsInOrder(
      Iterable<?> actual, Object... expected) {
    assertContentsInOrder((String) null, actual, expected);
  }

  /**
   * Asserts that {@code actual} contains precisely the elements {@code expected}, but in any
   * order.
   */
  public static void assertContentsInAnyOrder(String message, Iterable<?> actual,
      Object... expected) {
    HashMap<Object, Object> expectedMap = new HashMap<Object, Object>(expected.length);
    for (Object expectedObj : expected) {
      expectedMap.put(expectedObj, expectedObj);
    }

    for (Object actualObj : actual) {
      if (expectedMap.remove(actualObj) == null) {
        failWithMessage(message, "Extra object in actual: (" + actualObj.toString() + ")");
      }
    }

    if (expectedMap.size() > 0) {
      failWithMessage(message, "Extra objects in expected.");
    }
  }

  /** Variant of assertContentsInAnyOrder(String, Iterable<?>, Object...) using a generic message. */
  public static void assertContentsInAnyOrder(Iterable<?> actual, Object... expected) {
    assertContentsInAnyOrder((String) null, actual, expected);
  }

  /** Asserts that {@code iterable} is empty. */
  public static void assertEmpty(String message, Iterable<?> iterable) {
    if (iterable.iterator().hasNext()) {
      failNotEmpty(message, iterable.toString());
    }
  }

  /** Variant of {@link #assertEmpty(String, Iterable)} using a generic message. */
  public static void assertEmpty(Iterable<?> iterable) {
    assertEmpty(null, iterable);
  }

  /** Asserts that {@code map} is empty. */
  public static void assertEmpty(String message, Map<?, ?> map) {
    if (!map.isEmpty()) {
      failNotEmpty(message, map.toString());
    }
  }

  /** Variant of {@link #assertEmpty(String, Map)} using a generic message. */
  public static void assertEmpty(Map<?, ?> map) {
    assertEmpty(null, map);
  }

  /** Asserts that {@code iterable} is not empty. */
  public static void assertNotEmpty(String message, Iterable<?> iterable) {
    if (!iterable.iterator().hasNext()) {
      failEmpty(message);
    }
  }

  /** Variant of assertNotEmpty(String, Iterable<?>) using a generic message. */
  public static void assertNotEmpty(Iterable<?> iterable) {
    assertNotEmpty(null, iterable);
  }

  /** Asserts that {@code map} is not empty. */
  public static void assertNotEmpty(String message, Map<?, ?> map) {
    if (map.isEmpty()) {
      failEmpty(message);
    }
  }

  /** Variant of {@link #assertNotEmpty(String, Map)} using a generic message. */
  public static void assertNotEmpty(Map<?, ?> map) {
    assertNotEmpty(null, map);
  }

  /**
   * Utility for testing equals() and hashCode() results at once. Tests that lhs.equals(rhs)
   * matches expectedResult, as well as rhs.equals(lhs).  Also tests that hashCode() return values
   * are equal if expectedResult is true.  (hashCode() is not tested if expectedResult is false,
   * as unequal objects can have equal hashCodes.)
   *
   * @param lhs An Object for which equals() and hashCode() are to be tested.
   * @param rhs As lhs.
   * @param expectedResult True if the objects should compare equal, false if not.
   */
  public static void checkEqualsAndHashCodeMethods(
      String message, Object lhs, Object rhs, boolean expectedResult) {

    if ((lhs == null) && (rhs == null)) {
      Assert.assertTrue(
          "Your check is dubious...why would you expect null != null?",
          expectedResult);
      return;
    }

    if ((lhs == null) || (rhs == null)) {
      Assert.assertFalse(
          "Your check is dubious...why would you expect an object "
              + "to be equal to null?", expectedResult);
    }

    if (lhs != null) {
      Assert.assertEquals(message, expectedResult, lhs.equals(rhs));
    }
    if (rhs != null) {
      Assert.assertEquals(message, expectedResult, rhs.equals(lhs));
    }

    if (expectedResult) {
      String hashMessage =
          "hashCode() values for equal objects should be the same";
      if (message != null) {
        hashMessage += ": " + message;
      }
      Assert.assertTrue(hashMessage, lhs.hashCode() == rhs.hashCode());
    }
  }

  /**
   * Variant of checkEqualsAndHashCodeMethods(String,Object,Object,boolean...)} using a generic
   * message.
   */
  public static void checkEqualsAndHashCodeMethods(Object lhs, Object rhs,
      boolean expectedResult) {
    checkEqualsAndHashCodeMethods((String) null, lhs, rhs, expectedResult);
  }

  public static void assertWithin(DateTime expected, DateTime actual, Duration delta) {
    Preconditions.checkNotNull(expected);
    Preconditions.checkNotNull(actual);
    Preconditions.checkNotNull(delta);
    long actualDelta = actual.getMillis() - expected.getMillis();
    if (Math.abs(actualDelta) <= delta.getMillis()) return;

    Assert.fail("Expected: " + expected + ". Actual: " + actual + ". Should be within "
        + delta.getMillis()
        + " ms but was ahead by "
        + actualDelta
        + " millis");
  }

  /**
   * Assert that a collection contains an equivalent entity.
   */
  public static <T extends EquivalentForTesting<T>> void assertContainsEquivalent(
      Collection<T> actual,
      T expected) {
    for (T actualEntry : actual) {
      if (actualEntry.equivalenceForTesting().equals(expected.equivalenceForTesting())) {
        return;
      }
    }
    Assert.fail(errorMsg("actual did not contain expected", actual, expected));
  }

  /**
   * Assert the two values are equivalent.
   */
  public static <T extends EquivalentForTesting<T>> void assertEquivalent(T actual, T expected) {
    Assert.assertEquals(expected.equivalenceForTesting(), actual.equivalenceForTesting());
  }

  /**
   * Assert the two iterables are equivalent.
   */
  public static <T extends EquivalentForTesting<T>> void assertEquivalent(
      Iterable<T> actual,
      T... expected) {
    assertEquivalent(actual, Lists.newArrayList(expected));
  }

  /**
   * Assert the two iterables are equivalent.  First applies the Ordering before comparing
   * one by one.
   */
  public static <T extends EquivalentForTesting<T>> void assertEquivalent(
      Iterable<T> actual,
      Iterable<T> expected,
      Ordering<? super T> ordering) {
    assertEquivalent(ordering.immutableSortedCopy(actual), ordering.immutableSortedCopy(expected));
  }

  /**
   * Assert the two iterables are equivalent by comparing them one by one.
   */
  public static <T extends EquivalentForTesting<T>> void assertEquivalent(
      Iterable<T> actual,
      Iterable<T> expected) {
    Iterator<T> expectedItr = expected.iterator();
    for (T actualEntry : actual) {
      Assert.assertTrue(
          errorMsg("expected has no more results", actual, expected),
          expectedItr.hasNext());
      assertEquivalent(actualEntry, expectedItr.next());
    }
    Assert.assertFalse(
        errorMsg("expected has more results", actual, expected),
        expectedItr.hasNext());
  }

  private static String errorMsg(String errorMsg, Object actual, Object expected) {
    return String.format("%s: actual=[%s] expected=[%s]", errorMsg, actual, expected);
  }

  private static Matcher getMatcher(String expectedRegex, String actual) {
    Pattern pattern = Pattern.compile(expectedRegex);
    return pattern.matcher(actual);
  }

  private static void failEqual(String message, Object unexpected) {
    failWithMessage(message, "expected not to be:<" + unexpected + ">");
  }

  private static void failWrongLength(
      String message, int expected, int actual) {
    failWithMessage(message, "expected array length:<" + expected
        + "> but was:<" + actual + '>');
  }

  private static void failWrongElement(
      String message, int index, Object expected, Object actual) {
    failWithMessage(message, "expected array element[" + index + "]:<"
        + expected + "> but was:<" + actual + '>');
  }

  private static void failNotMatches(
      String message, String expectedRegex, String actual) {
    String actualDesc = (actual == null) ? "null" : ('<' + actual + '>');
    failWithMessage(message, "expected to match regex:<" + expectedRegex
        + "> but was:" + actualDesc);
  }

  private static void failNotContains(
      String message, String expectedRegex, String actual) {
    String actualDesc = (actual == null) ? "null" : ('<' + actual + '>');
    failWithMessage(message, "expected to contain regex:<" + expectedRegex
        + "> but was:" + actualDesc);
  }

  private static void failMatch(
      String message, String expectedRegex, String actual) {
    failWithMessage(message, "expected not to match regex:<" + expectedRegex
        + "> but was:<" + actual + '>');
  }

  private static void failContains(
      String message, String expectedRegex, String actual) {
    failWithMessage(message, "expected not to contain regex:<" + expectedRegex
        + "> but was:<" + actual + '>');
  }

  private static void failNotEmpty(
      String message, String actual) {
    failWithMessage(message, "expected to be empty, but contained: <"
        + actual + ">");
  }

  private static void failEmpty(String message) {
    failWithMessage(message, "expected not to be empty, but was");
  }

  private static void failWithMessage(String userMessage, String ourMessage) {
    Assert.fail((userMessage == null)
        ? ourMessage
        : userMessage + ' ' + ourMessage);
  }

  private static boolean equal(Object a, Object b) {
    return a == b || (a != null && a.equals(b));
  }
}
