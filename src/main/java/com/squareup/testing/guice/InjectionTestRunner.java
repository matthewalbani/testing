package com.squareup.testing.guice;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.squareup.testing.TestModule;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.rules.TestRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static com.squareup.common.reflect.ReflectUtils.getDeclaredAnnotation;

/**
 * Guice test runner that bootstraps itself off a {@link TestModule} annotation on the test class,
 * or a {@link TestInjector} annotation on a field of type {@link InjectorSupplier}. Memoizes the
 * injector to avoid creating it multiple times for the same module or test class.
 */
public class InjectionTestRunner extends AbstractGuiceTestRunner {
  private Injector injector;

  public InjectionTestRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }

  @Override protected void beforeTest(FrameworkMethod testMethod, Class<?> testClass, Object test) {
    MockitoAnnotations.initMocks(test);
    super.beforeTest(testMethod, testClass, test);
  }

  @Override protected void afterTest(FrameworkMethod testMethod, Class<?> testClass, Object test) {
    super.afterTest(testMethod, testClass, test);
    Mockito.validateMockitoUsage();
  }

  @Override protected List<TestRule> getTestRules(Object target) {
    return ImmutableList.<TestRule>builder()
        .add(getInjector().getInstance(ComponentTestSupportRule.class))
        .addAll(super.getTestRules(target))
        .build();
  }

  @Override
  protected void collectInitializationErrors(List<Throwable> errors) {
    super.collectInitializationErrors(errors);
    if (getTestModuleClass() == null && getInjectorSupplier() == null) {
      String gripe =
          String.format("Test class should have @%s annotation or InjectorSupplier field with "
              + "@%s annotation",
              TestModule.class.getSimpleName(), TestInjector.class.getSimpleName());
      errors.add(new Exception(gripe));
    }
  }

  protected Class<?> getClassWithTestModuleAnnotation() {
    return getTestClass().getJavaClass();
  }

  /**
   * Returns the {@link Class} of the test module to be installed. By default, fetches it from the
   * annotation {@link TestModule}.
   */
  @Nullable protected Class<? extends Module> getTestModuleClass() {
    TestModule annotation = getTestModuleClassAnnotation();
    if (annotation != null) {
      return annotation.value();
    } else {
      return null;
    }
  }

  /** @return TestModule annotation on the test class, or null if not present */
  @Nullable private TestModule getTestModuleClassAnnotation() {
    return getDeclaredAnnotation(getClassWithTestModuleAnnotation(), TestModule.class);
  }

  /** @return InjectorSupplier annotated with TestInjector, or null if not present */
  private InjectorSupplier getInjectorSupplier() {
    for (Field field : getTestClass().getJavaClass().getFields()) {
      if (field.isAnnotationPresent(TestInjector.class)) {
        Preconditions.checkState(
            Modifier.isStatic(field.getModifiers()) && Modifier.isPublic(field.getModifiers()),
            "TestInjector field %s should be public static", field.getName());
        Preconditions.checkState(
            InjectorSupplier.class.isAssignableFrom(field.getType()),
            "TestInjector field %s should implement InjectorSupplier", field.getName());

        try {
          return (InjectorSupplier) field.get(null);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        }
      }
    }
    return null;
  }

  @Override protected Injector getInjector() {
    if (injector == null) {
      Class<? extends Module> testModuleClass = getTestModuleClass();
      if (testModuleClass != null) {
        injector = TestInjectors.memoized(testModuleClass);
      } else {
        injector = getInjectorSupplier().getInjector();
      }
    }
    return injector;
  }
}
