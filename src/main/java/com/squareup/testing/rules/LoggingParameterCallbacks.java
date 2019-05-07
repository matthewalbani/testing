package com.squareup.testing.rules;

import com.squareup.logging.Logger;
import com.squareup.testing.rules.AbstractParameterRule.ParameterCallback;

/**
 * Defines {@link ParameterCallback} implementations for logging the start and end of a
 * parameterized iteration, within the execution of a {@link ParameterRule}.
 *
 * The callbacks will use the standard {@link com.squareup.logging.Logger} if Log4J is properly
 * configured. Otherwise, it will fallback to {@link java.util.logging.Logger}, which will generally
 * log at least to the console, but with somewhat limited formatting and collation with test output.
 *
 * This class is declared package private, and is used by {@link ParameterRule}.
 */
final class LoggingParameterCallbacks {
  private static final Logger logger = Logger.getLogger(LoggingParameterCallbacks.class);

  private static boolean log4jChecked;
  private static boolean log4jStatus;

  static <T> ParameterCallback<T> parameterStart() {
    return new ParameterCallback<T> () {
      @Override public void run(T parameter) {
        if (checkRootLog4jLoggerAvailable()) {
          logger.info("**********************************************");
          logger.info("Starting test with parameter %s", parameter);
          logger.info("**********************************************");
        } else {
          java.util.logging.Logger.getLogger(ParameterRule.class.getCanonicalName())
              .log(java.util.logging.Level.INFO, "Starting test with parameter {0}", parameter);
        }
      }};
  }

  static <T> ParameterCallback<T> parameterEnd() {
    return new ParameterCallback<T> () {
      @Override public void run(T parameter) {
        if (checkRootLog4jLoggerAvailable()) {
          logger.info("**********************************************");
          logger.info("Finishing test with parameter %s", parameter);
          logger.info("**********************************************");
        } else {
          java.util.logging.Logger.getLogger(ParameterRule.class.getCanonicalName())
              .log(java.util.logging.Level.INFO, "Finishing test with parameter {0}", parameter);
        }
      }};
  }

  private static boolean checkRootLog4jLoggerAvailable() {
    // this doesn't really need to be thread-safe, since any races will result in same result
    if (log4jChecked) {
      return log4jStatus;
    }

    Logger rootLogger = Logger.getRootLogger();
    log4jStatus = (rootLogger != null && rootLogger.getAppenderNames().size() > 0);
    log4jChecked = true;

    return log4jStatus;
  }
}
