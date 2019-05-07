// Copyright 2014 by Square, Inc.
package com.squareup.testing.base.scenario;

import com.google.common.base.Objects;
import com.google.common.reflect.TypeToken;
import java.util.Optional;

/**
 * Simple wrapper for the unique identifier for an {@link InstanceBuilder} within a given
 * {@link ScenarioBuilder}.
 */
public class Key {
  private final String name;
  private final TypeToken typeToken;

  private Key(String name, TypeToken typeToken) {
    this.name = name;
    this.typeToken = typeToken;
  }

  static Key of(Optional<String> optionalName, TypeToken type) {
    return new Key(optionalName.orElse(""), type);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Key that = (Key) o;

    return Objects.equal(this.name, that.name)
        && Objects.equal(this.typeToken, that.typeToken);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name, typeToken);
  }

  @Override
  public String toString() {
    return "(" + name + ", " + typeToken + ")";
  }
}
