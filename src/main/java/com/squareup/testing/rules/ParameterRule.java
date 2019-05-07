package com.squareup.testing.rules;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * An implementation of {@code AbstractParameterRule} which adds logging callbacks to delimit the
 * start and finish of each parameterized test run.
 *
 * @param <T> The type of the parameter.
 */
public class ParameterRule<T> extends AbstractParameterRule<T> {
  /**
   * Create a {@link ParameterRule} using all the values in the provided {@link Collection}.
   *
   * @param parameters a {@link Collection} of test parameter values.
   */
  public ParameterRule(Collection<T> parameters) {
    super(parameters.stream()
        .map(ParameterRule::generator)
        .collect(Collectors.toList()));
    addSetupCallback(LoggingParameterCallbacks.parameterStart());
    addCleanupCallback(LoggingParameterCallbacks.parameterEnd());
  }

  /**
   * Create a {@link ParameterRule} using all the passed in as vararg values.
   *
   * @param parameters a vararg list of test parameter values.
   */
  @SafeVarargs
  public ParameterRule(T... parameters) {
    this(Arrays.asList(parameters));
  }

  private static <T> ParameterGenerator<T> generator(T param) {
    return description -> Optional.ofNullable(param);
  }
}
