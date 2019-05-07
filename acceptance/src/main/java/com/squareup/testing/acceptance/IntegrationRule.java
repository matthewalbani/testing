package com.squareup.testing.acceptance;

import com.google.common.collect.Sets;
import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;

public class IntegrationRule implements TestRule {
  static Multiverse multiverse = new Multiverse();

  static {
    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override public void run() {
        shutDown();
      }
    });
  }

  @Override public Statement apply(final Statement base, final Description description) {
    return new Statement() {
      @Override public void evaluate() throws Throwable {
        IntegrateApps integrateApps = getIntegrateAppsAnnotation(description);
        multiverse.integrate(Sets.newHashSet(integrateApps.value()));
        multiverse.onBeforeEach();

        try {
          base.evaluate();
        } finally {
          multiverse.onAfterEach();
        }
      }
    };
  }

  private static void shutDown() {
    multiverse.shutDown();
  }

  public IntegrationHelper getIntegrationHelper() {
    return multiverse.getIntegrationHelper();
  }

  private IntegrateApps getIntegrateAppsAnnotation(Description description) {
    IntegrateApps annotation = description.getAnnotation(IntegrateApps.class);
    if (annotation == null) {
      annotation = description.getTestClass().getAnnotation(IntegrateApps.class);
    }
    if (annotation == null) {
      throw new RuntimeException("please annotate your test class or method with @IntegrateApps!");
    }
    return annotation;
  }

  public RemoteApp getRemoteApp(String appName) {
    return multiverse.getRemoteApp(appName);
  }
}
