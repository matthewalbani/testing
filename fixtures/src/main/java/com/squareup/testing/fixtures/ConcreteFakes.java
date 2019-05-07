// Copyright 2017 Square, Inc.
package com.squareup.testing.fixtures;

public class ConcreteFakes {
  public static ConcreteFakeRule rule() {
    return new ConcreteFakeRule();
  }

  // Disallow construction
  private ConcreteFakes() {}
}
