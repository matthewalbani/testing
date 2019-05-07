package com.squareup.testing.rules;

/**
 * Wrapper around {@link ParameterRule} for booleans.
 */
public class BinaryRule extends ParameterRule<Boolean> {

  public BinaryRule() {
    super(Boolean.TRUE, Boolean.FALSE);
  }
}
