package com.squareup.testing.logging;

import com.google.common.base.Preconditions;
import com.squareup.logging.LevelAdjustingLogger;
import com.squareup.logging.LevelAdjustmentTesting;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

/**
 * A test rule that clears policies for {@link LevelAdjustingLogger}s. That way log statements in
 * previous test cases do not affect log levels of subsequent test cases.
 */
public class ClearLogLevelAdjustmentsRule implements TestRule {
  private final LevelAdjustingLogger logger;

  /** Constructs a rule that clears all {@link LevelAdjustingLogger}s. */
  public ClearLogLevelAdjustmentsRule() {
    this.logger = null;
  }

  /** Constructs a rule that clears just the specified {@link LevelAdjustingLogger}. */
  public ClearLogLevelAdjustmentsRule(LevelAdjustingLogger logger) {
    this.logger = Preconditions.checkNotNull(logger);
  }

  /** Constructs a rule that clears just the logger with the specified name. */
  public ClearLogLevelAdjustmentsRule(Class<?> whichLogger) {
    this(LevelAdjustingLogger.getLogger(whichLogger));
  }

  /** Constructs a rule that clears just the logger with the specified name. */
  public ClearLogLevelAdjustmentsRule(String whichLogger) {
    this(LevelAdjustingLogger.getLogger(whichLogger));
  }

  @Override public Statement apply(final Statement statement, Description description) {
    return new Statement() {
      @Override public void evaluate() throws Throwable {
        if (logger == null) {
          LevelAdjustmentTesting.resetAll();
        } else {
          LevelAdjustmentTesting.resetLogger(logger);
        }
        statement.evaluate();
      }
    };
  }
}
