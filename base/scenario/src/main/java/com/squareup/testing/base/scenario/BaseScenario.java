/*
 * Copyright 2015, Square, Inc.
 */

package com.squareup.testing.base.scenario;

import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import java.util.Map;
import java.util.Optional;

/**
 * A basic {@link Scenario} that holds a mapping of instances in memory.
 */
public class BaseScenario implements Scenario {
  // Mapping of types and names to named instances
  private final ImmutableMap<Key, Object> namedEntities;

  BaseScenario(Map<Key, Object> namedEntities) {
    this.namedEntities = ImmutableMap.copyOf(namedEntities);
  }

  /**
   * The suppression is safe because the namedEntities map is constructed via a process that only
   * performs insertions of the form
   * {@code put(Key.of("name", TypeToken<T>), t)}
   * where {@code t} is of type {@code T}.
   */
  @SuppressWarnings({"SuspiciousMethodCalls", "unchecked"})
  @Override public <T> T get(String name, TypeToken<T> type) {
    T instance = (T)namedEntities.get(Key.of(Optional.ofNullable(name), type));
    if (instance == null) {
      if (name == null) {
        throw new IllegalArgumentException(String.format("Scenario does not contain unnamed %s.",
            type));
      }
      throw new IllegalArgumentException(String.format("Scenario does not contain %s named %s",
          type, name));
    }
    return instance;
  }
}
