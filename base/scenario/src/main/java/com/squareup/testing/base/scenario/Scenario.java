// Copyright 2014 by Square, Inc.
package com.squareup.testing.base.scenario;

import com.google.common.reflect.TypeToken;

/**
 * A Scenario is just a bag of instances of classes. Instances can be anonymous, in which case
 * they are accessible by class, or they can be named, in which case they are accessible by
 * (name, class).
 * A Scenario can only have one anonymous instance of each class. It can have multiple named
 * instances of each class, as long as the names are unique.
 */
public interface Scenario {
  /**
   * Get an anonymous instance from the scenario.
   */
  default <T> T get(Class<T> klass) {
    return get(null, TypeToken.of(klass));
  }

  /**
   * Get a named instance from the scenario.
   */
  default <T> T get(String name, Class<T> klass) {
    return get(name, TypeToken.of(klass));
  }

  /**
   * Get an anonymous instance from the scenario.
   */
  default <T> T get(TypeToken<T> type) {
    return get(null, type);
  }

  /**
   * Get a named instance from the scenario.
   *
   * @param name the name of the desired instance
   * @param type the type of the desired instance
   * @param <T> generic type parameter of the desired instance
   * @return an instance of the desired name and type
   */
  <T> T get(String name, TypeToken<T> type);
}
