package com.squareup.testing;

import com.google.common.base.Throwables;
import org.junit.Test;
import org.reflections.adapters.MetadataAdapter;
import org.reflections.scanners.AbstractScanner;

import static org.reflections.ReflectionUtils.getAllMethods;
import static org.reflections.ReflectionUtils.withAnnotation;

/**
 * Finds Junit3 style tests and JUnit4 annotated test classes.
 */
class JUnitTestsScanner extends AbstractScanner {
  private final ChunkConfig config;

  public JUnitTestsScanner(ChunkConfig config) {
    this.config = config;
  }

  @SuppressWarnings("unchecked") @Override
  public void scan(Object cls) {
    MetadataAdapter metadataAdapter = getMetadataAdapter();
    String className = metadataAdapter.getClassName(cls);
    if (className.endsWith("Test")) {
      if (!config.runFunctionalTests && className.endsWith("FunctionalTest")) {
        return;
      }

      Class<?> testClass;
      try {
        testClass = Class.forName(className);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException(e);
      }
      boolean containsValidTests =
          !getAllMethods(testClass, withAnnotation(Test.class)).isEmpty();

      if (!containsValidTests) {
        // Some test runners (e.g cucumber) do not require @Test methods.
        if (metadataAdapter.getClassAnnotationNames(cls).contains("org.junit.runner.RunWith")) {
          containsValidTests = true;
        }
      }

      if (containsValidTests) {
        getStore().put("UnitTestClass", className);
      }
    }
  }
}
