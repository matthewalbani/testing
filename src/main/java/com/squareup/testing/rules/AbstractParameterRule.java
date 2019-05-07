package com.squareup.testing.rules;

import com.squareup.testing.mockito.MockitoListenerHelper;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A {@link TestRule} that iterates through the parameters given in the constructor, and runs each
 * test reapeatedly, each time making the current parameter available for use in test code. Optional
 * setup and cleanup callbacks can be provided, which will be called once for each parameterized
 * test run.
 */
public abstract class AbstractParameterRule<T> implements TestRule {
  private final Collection<ParameterGenerator<T>> parameterGenerators;
  private final AtomicReference<T> currentParameter = new AtomicReference<>(null);
  private final List<ParameterCallback<T>> setupCallbacks = new CopyOnWriteArrayList<>();
  private final List<ParameterCallback<T>> cleanupCallbacks = new CopyOnWriteArrayList<>();

  /**
   * Constructor using all the values in the provided {@link Collection}.
   *
   * @param parameterGenerators a {@link Collection} of test parameter values.
   */
  public AbstractParameterRule(Collection<ParameterGenerator<T>> parameterGenerators) {
    this.parameterGenerators = parameterGenerators;
  }

  /**
   * Adds a setup callback for this rule. Multiple setup callbacks can be added, and they will be
   * executed in the order they are added, before each parameterized test run. This method returns
   * {@code this} instance, which allows invocations of it to be chained, builder style.
   *
   * @param setupCallback a {@link ParameterCallback}
   * @return this instance
   */
  public AbstractParameterRule<T> addSetupCallback(ParameterCallback<T> setupCallback) {
    setupCallbacks.add(setupCallback);
    return this;
  }

  /**
   * Adds a cleanup callback for this rule. Multiple cleanup callbacks can be added, and they will
   * be executed in the order they are added, after each parameterized test run. This method returns
   * {@code this} instance, which allows invocations of it to be chained, builder style.
   *
   * @param cleanupCallback a {@link ParameterCallback}
   * @return this instance
   */
  public AbstractParameterRule<T> addCleanupCallback(ParameterCallback<T> cleanupCallback) {
    cleanupCallbacks.add(cleanupCallback);
    return this;
  }

  /**
   * @return the currently active parameter value, accessible from test code
   */
  public T getCurrentParameter() {
    return currentParameter.get();
  }

  @Override public Statement apply(final Statement base, Description description) {
    return new Statement() {
      @Override public void evaluate() throws Throwable {
        for (ParameterGenerator<T> parameterGenerator : parameterGenerators) {
          Optional<T> maybeParameter = parameterGenerator.generate(description);
          if (!maybeParameter.isPresent()) {
            continue;
          }

          T parameter = maybeParameter.get();
          currentParameter.set(parameter);

          for (ParameterCallback<T> setup : setupCallbacks) {
            setup.run(parameter);
          }

          base.evaluate();

          // If we're using the MockitoTestRunner than we need to clear the MismatchReportingTestListener
          // between evaluations to avoid a RedundantListenerException
          MockitoListenerHelper.removeMismatchReportingTestListener();

          for (ParameterCallback<T> cleanup : cleanupCallbacks) {
            cleanup.run(parameter);
          }
        }
      }
    };
  }

  /**
   * A callback used when adding setup and cleanup callbacks to an {@link AbstractParameterRule<T>}.
   */
  public interface ParameterCallback<T> {
    void run(T parameter);
  }

  /**
   * Generates the parameter over which to iterate, if applicable to the given test run (represented
   * by {@param description}).
   */
  public interface ParameterGenerator<T> {
    Optional<T> generate(Description description);
  }
}
