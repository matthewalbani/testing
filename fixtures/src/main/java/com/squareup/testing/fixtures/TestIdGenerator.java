package com.squareup.testing.fixtures;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.squareup.integration.persistence.Id;
import com.squareup.integration.persistence.PersistentEntity;
import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.lang3.RandomUtils;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * Generates unique ids for use in testing fixture data.
 */
public class TestIdGenerator {

  private TestIdGenerator() {}

  // N.B - Start at 1M to avoid id conflict with auto generated database ids.
  /**
   * Start at a number that is
   * - large, to avoid id conflict with auto generated database ids
   * - random, to avoid reliance on incidental ordering of unordered containers
   * (e.g. HashMap.keySet().containsExactly())
   */
  private static final AtomicInteger currentId =
      new AtomicInteger(1_000_000 + RandomUtils.nextInt(0, 10));

  /**
   * Get the next integer.
   */
  public static int nextInt() {
    return currentId.getAndIncrement();
  }

  /**
   * Get the next long.
   */
  public static long nextLong() {
    return (long) nextInt();
  }

  /**
   * Get the next short.
   */
  public static short nextShort() {
    return (short) (nextInt() % Short.MAX_VALUE);
  }

  public static byte nextByte() {
    return (byte) (nextInt() % Byte.MAX_VALUE);
  }

  /**
   * Get the {@link #nextInt()} as a string.
   */
  public static String nextString() {
    return String.valueOf(nextInt());
  }

  /**
   * Get the {@link #nextInt()} as a string, with a provided prefix.
   */
  public static String prefixed(String prefix) {
    return prefix + nextString();
  }

  /**
   * Get the {@link #nextString()} with a specified length.
   */
  public static String nextString(int length) {
    String atLeastLength = Strings.padStart(nextString(), length, '0');
    // Use the least significant digits to increase randomness.
    return atLeastLength.substring(atLeastLength.length() - length, atLeastLength.length());
  }

  /**
   * Use the {@link #nextInt()} to generate an arbitrary {@link Enum value}.
   */
  public static <E extends Enum<E>> E nextEnum(Class<E> enumClass) {
    int i = Math.abs(nextInt());
    E[] values = enumClass.getEnumConstants();
    return values[i % values.length];
  }

  /**
   * Returns a random {@link Enum value} different from the specified value.
   */
  public static <E extends Enum<E>> E nextEnumDifferentFrom(E e) {
    E[] enums = e.getDeclaringClass().getEnumConstants();
    int enumCount = enums.length;
    checkArgument(enumCount >= 2, "Not enough enum values to generate a different value");

    // Opt to use RandomUtils#nextInt instead of the TestIdGenerator#nextInt for better randomness
    // guarantees. TestIdGenerator#nextInt has "memory" of previous invocations via currentId and
    // is liable to return an enum only 1 away.
    int i = RandomUtils.nextInt(0, enumCount);
    return enums[i] == e ? enums[(i + 1) % enumCount] : enums[i];
  }

  /**
   * Returns a random {@link Enum value} different from the specified value(s).
   */
  public static <E extends Enum<E>> E nextEnumDifferentFrom(E first, E ... rest) {
    return nextEnumDifferentFrom(EnumSet.of(first, rest));
  }

  /**
   * Returns a random {@link Enum value} different from the specified value(s).
   */
  public static <E extends Enum<E>> E nextEnumDifferentFrom(Collection<E> c) {
    return nextEnumDifferentFrom(EnumSet.copyOf(c));
  }

  /**
   * Returns a random {@link Enum value} different from the specified value(s).
   */
  public static <E extends Enum<E>> E nextEnumDifferentFrom(EnumSet<E> es) {
    EnumSet<E> enums = EnumSet.complementOf(es);
    checkArgument(enums.size() >= 1, "Not enough enum values to generate a different value");

    // Opt to use RandomUtils#nextInt instead of the TestIdGenerator#nextInt for better randomness
    // guarantees. TestIdGenerator#nextInt has "memory" of previous invocations via currentId and
    // is liable to return an enum only 1 away.
    int i = RandomUtils.nextInt(0, enums.size());
    return ImmutableList.copyOf(enums).get(i);
  }

  /**
   * Use the {@link #nextInt()} to generate an arbitrary value from a given list of values
   */
  public static <T> T oneOf(T... values) {
    return values[nextInt() % values.length];
  }

  /**
   * Use the {@link #nextInt()} to generate an arbitrary value from a given collection of values
   */
  public static <T> T oneOf(Collection<T> values) {
    return Iterables.get(values, nextInt() % values.size());
  }

  /**
   * Get the {@link #nextInt()} as a type safe {@link Id}.
   */
  public static <T extends PersistentEntity> Id<T> nextId() {
    return Id.of(nextInt());
  }

  /**
   * Use the {@link #nextInt()} to generate a fractional double value.
   */
  public static double nextDouble() {
    return ((double) nextInt()) / 100.;
  }

  /**
   * Use the {@link #nextInt()} to generate a single hexadecimal value.
   */
  public static char nextHex() {
    char c = Character.forDigit(nextInt() % 16, 16);
    Preconditions.checkState(c != '\0', "somehow got the NUL character");
    return c;
  }

  /**
   * Use the {@link #nextByte()} to generate a hexadecimal string of length bytes.
   *
   * Note that the String length will be twice the given length since each byte is
   * two hexadecimal digits.
   */
  public static String nextHexString(int length) {
    StringBuffer sb = new StringBuffer();
    for (int i = 0; i < length; ++i) {
      sb.append(String.format("%02x", nextByte()));
    }
    return sb.toString();
  }

  /**
   * Use the {@link #nextInt()} to generate a single alphanumeric value.
   */
  public static char nextAlphaNumeric() {
    char c = Character.forDigit(nextInt() % 36, 36);
    Preconditions.checkState(c != '\0', "somehow got the NUL character");
    return c;
  }

  /**
   * Allow the formation of any arbitrary string using a supplier function and a length.
   */
  public static String nextStringFromFunction(Supplier<Character> function, int length) {
    return IntStream.rangeClosed(1, length).boxed()
        .map(i -> function.get())
        .map(String::valueOf)
        .collect(Collectors.joining());
  }
}
