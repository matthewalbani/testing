// Copyright 2013, Square, Inc.

package com.squareup.testing.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import java.util.HashMap;
import java.util.Map;

import static com.squareup.common.reflect.ReflectUtils.newInstance;

public class TestInjectors {
  private static final Map<Class<? extends Module>, Injector> injectorMap =
      new HashMap<Class<? extends Module>, Injector>();

  /**
   * Gets an injector created from the given module.
   *
   * <p>The injector is cached, created only once per module class, during the lifetime of the JVM.
   * This is an optimization that allows multiple test classes that require the same module to only
   * need initialization once.
   *
   * <p>The injector also includes the {@link TestScopeModule} and
   * {@link ComponentTestSupportModule}.
   */
  public static Injector memoized(final Class<? extends Module> testModuleClass) {
    synchronized (injectorMap) {
      if (!injectorMap.containsKey(testModuleClass)) {
        injectorMap.put(testModuleClass, Guice.createInjector(
            newInstance(testModuleClass),
            new TestScopeModule(),
            new ComponentTestSupportModule()));
      }
      return injectorMap.get(testModuleClass);
    }
  }

  static void staticInjectTestClass(final Class<?> testClass, Injector injector) {
    injector.createChildInjector(new StaticInjectTestModule(testClass));
  }

  private static class StaticInjectTestModule extends AbstractModule {
    private final Class<?> testClass;

    private StaticInjectTestModule(Class<?> testClass) {
      this.testClass = testClass;
    }

    @Override protected void configure() {
      requestStaticInjection(testClass);
    }
  }

  private TestInjectors() {
  }
}
