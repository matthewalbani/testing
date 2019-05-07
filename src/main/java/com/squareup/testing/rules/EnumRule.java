package com.squareup.testing.rules;

import java.util.EnumSet;

/**
 * An extension of {@link ParameterRule} which allows using enum classes as parameter values.
 *
 * @param <T> the enum type.
 */
public class EnumRule<T extends Enum<T>> extends ParameterRule<T> {

  /**
   * Constructs an {@link EnumRule} using all enum values from an {@link Enum} class.
   *
   * @param enumClazz an {@link Enum} class
   */
  public EnumRule(Class<T> enumClazz) {
    super(EnumSet.allOf(enumClazz));
  }
}
