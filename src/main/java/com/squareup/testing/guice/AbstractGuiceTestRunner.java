package com.squareup.testing.guice;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.squareup.common.guice.SimpleScope;
import com.squareup.testing.TestScoped;
import com.squareup.testing.runners.BaseTestRunner;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * Test runner that handles Guice injection.
 *
 * <p>Test members are injected prior to test execution. Static fields can also be injected.
 *
 * <p>Injection happens as early as possible in the life of the test, before running {@link
 * org.junit.Before @Before} methods (and static injection before {@link org.junit.BeforeClass
 *
 * @BeforeClass} methods); before any {@linkplain org.junit.Rule rules}. This means that (both class
 * and method) rules can also be injected.
 *
 * <p>This base class only ensures that at most one injector is used per test method (and one for
 * static injection). Subclasses may cache the injector across test runs.
 *
 * <p>This runner also handles the {@link TestScoped test scope}, if the injector includes the
 * {@link TestScopeModule}.
 * @see InjectionTestRunner
 */
public abstract class AbstractGuiceTestRunner extends BaseTestRunner {
  private final ThreadLocal<Injector> currentTestInjector = new ThreadLocal<Injector>();
  private final ThreadLocal<FrameworkMethod> currentTestMethod = new ThreadLocal<FrameworkMethod>();

  public AbstractGuiceTestRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }

  @Override protected void runChild(FrameworkMethod method, RunNotifier notifier) {
    currentTestMethod.set(method);
    try {
      super.runChild(method, notifier);
    } finally {
      currentTestMethod.remove();
    }
  }

  @Override protected Object createTest() throws Exception {
    Preconditions.checkNotNull(currentTestMethod);
    beforeMemberInjection(currentTestMethod.get());
    currentTestInjector.set(getInjector());
    SimpleScope testScope = getTestScope();
    if (testScope != null) {
      testScope.enter();
    }
    try {
      return instantiateTest();
    } catch (Exception | Error e) {
      // clean up thread-local state on failure
      if (testScope != null) {
        testScope.exit();
      }
      throw e;
    }
  }

  /**
   * Instantiates the test. This is called after the test injector has been created, so the test
   * can be created with the assistance of dependency injection. The default implementation simply
   * asks the test injector to get the test class instance, so the test can have an @Inject
   * constructor.
   */
  protected Object instantiateTest() throws Exception {
    return getCurrentTestInjector().getInstance(getTestClass().getJavaClass());
  }

  /**
   * Instantiates the test without the use of the test injector. This simply delegates to
   * {@code super.createTest()}. This may be used by sub-classes that need an alternate way
   * to instantiate the test (e.g. without DI)
   */
  protected Object instantiateTestWithoutInjector() throws Exception {
    return super.createTest();
  }

  @Override protected void afterTest(FrameworkMethod testMethod, Class<?> testClass, Object test) {
    SimpleScope testScope = getTestScope();
    if (testScope != null) {
      testScope.exit();
    }
    currentTestInjector.remove();
    super.afterTest(testMethod, testClass, test);
  }

  /**
   * Called before {@link #getInjector getting the injector} for injecting the given test instance.
   * This may be used e.g. to initialize resources that are needed to create the injector that
   * depend on the test method.
   */
  protected void beforeMemberInjection(FrameworkMethod testMethod) {
  }

  /**
   * Called very early in the lifecycle of a test class, even before {@link #getInjector() getting
   * the injector} for injecting static members.
   *
   * <p> This is always called, even if {@linkplain #needsStaticInjection() static injection is not
   * needed}, making this a general purpose replacement for {@link #beforeTestClass}.
   *
   * @see BeforeClassInjection
   * @see #beforeMemberInjection(FrameworkMethod)
   */
  protected void beforeClassInjection() {
    invokeAnnotatedBeforeClassInjection();
  }

  private void invokeAnnotatedBeforeClassInjection() {
    List<FrameworkMethod> methods =
        getTestClass().getAnnotatedMethods(BeforeClassInjection.class);
    for (FrameworkMethod method : methods) {
      try {
        method.invokeExplosively(null);
      } catch (Throwable throwable) {
        Throwables.throwIfUnchecked(throwable);
        throw new RuntimeException(throwable);
      }
    }
  }

  /**
   * @return if there are static fields to be injected. If this returns false, no injector will be
   *         created for static injection.
   */
  protected boolean needsStaticInjection() {
    for (Field field : getTestClass().getJavaClass().getFields()) {
      if (needsStaticInjection(field)) return true;
    }
    for (Method method : getTestClass().getJavaClass().getMethods()) {
      if (needsStaticInjection(method)) return true;
    }
    return false;
  }

  private <T extends AccessibleObject & Member> boolean needsStaticInjection(T element) {
    return Modifier.isStatic(element.getModifiers())
        && (element.isAnnotationPresent(Inject.class)
        || element.isAnnotationPresent(javax.inject.Inject.class));
  }

  /** @return the {@link TestScoped} scope, or null if no TestScopeModule was installed */
  private SimpleScope getTestScope() {
    return getCurrentTestInjector() == null ? null // gracefully handle errors injector creation
        : (SimpleScope) getCurrentTestInjector().getScopeBindings().get(TestScoped.class);
  }

  @Override protected Statement classBlock(RunNotifier notifier) {
    beforeClassInjection();

    if (needsStaticInjection()) {
      // injecting here because this just happens to be called before static rules are extracted,
      // so that class rules can get injected too
      TestInjectors.staticInjectTestClass(getTestClass().getJavaClass(), getInjector());
    }

    return super.classBlock(notifier);
  }

  /**
   * @return injector for the current test. This never causes a new injector to be created (as
   *         opposed to {@link #getInjector()})
   */
  protected final Injector getCurrentTestInjector() {
    return currentTestInjector.get();
  }

  /**
   * Returns test method currently being ran (or being initialized), or {@code null} if no
   * particular test is running (e.g. during static injection)
   */
  protected final FrameworkMethod getCurrentTestMethod() {
    return currentTestMethod.get();
  }

  /**
   * Returns the injector to use for the test. Called once per test method; and if needed once for
   * static injection. Implementation may return the same injector for each test, or a fresh one
   * each time.
   */
  protected abstract Injector getInjector();
}
