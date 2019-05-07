package com.squareup.testing.rules;

import java.util.Locale;
import org.junit.rules.TestWatcher;
import org.junit.runner.Description;

/**
 * A rule that encapsulates a test class into a specific locale
 *
 * USAGE:
 *
 *     @Rule public final DefaultLocaleRule defaultLocaleRule = new DefaultLocaleRule(Locale.US);
 */
public class DefaultLocaleRule extends TestWatcher {
  private final Locale localeForTest;
  private Locale defaultLocale;

  public DefaultLocaleRule(Locale localeForTest) {
    this.localeForTest = localeForTest;
  }

  @Override protected void starting(Description description) {
    defaultLocale = Locale.getDefault();
    Locale.setDefault(localeForTest);
  }

  @Override protected void finished(Description description) {
    Locale.setDefault(defaultLocale);
  }
}
