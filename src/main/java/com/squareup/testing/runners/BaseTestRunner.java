package com.squareup.testing.runners;

import com.google.common.base.Throwables;
import java.lang.reflect.InvocationTargetException;
import org.junit.internal.runners.statements.Fail;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

/**
 * Base class for test runners.  Provides easier hooks for performing actions before and after
 * tests and test classes
 */
public class BaseTestRunner extends BlockJUnit4ClassRunner {
  private static final ThreadLocal<Object> CURRENT_TEST = new ThreadLocal<Object>();

  public BaseTestRunner(Class<?> klass) throws InitializationError {
    super(klass);
  }

  /** Called before the given test class is executed */
  protected void beforeTestClass(Class<?> testClass, RunNotifier notifier) {
  }

  /** Called after the given test class is executed */
  protected void afterTestClass(Class<?> testClass, RunNotifier notifier) {
  }

  /** Called before a test is run */
  protected void beforeTest(FrameworkMethod testMethod, Class<?> testClass, Object test) {
  }

  /** Called after a test is run */
  protected void afterTest(FrameworkMethod testMethod, Class<?> testClass, Object test) {
  }

  @Override
  protected Statement classBlock(final RunNotifier notifier) {
    final Statement parentClassBlock = super.classBlock(notifier);
    return new Statement() {
      @Override public void evaluate() throws Throwable {
        beforeTestClass(getTestClass().getJavaClass(), notifier);
        try {
          parentClassBlock.evaluate();
        } finally {
          afterTestClass(getTestClass().getJavaClass(), notifier);
        }
      }
    };
  }

  @Override protected Statement methodBlock(final FrameworkMethod method) {
    final Statement parentBlock = super.methodBlock(method);
    if (parentBlock instanceof Fail) {
      // This is heavily dependent on the current impl of BlockJUnit4ClassRunner#methodBlock
      // which returns a Fail object if the test class could not be constructed reflectively.
      // Bail early with a useful stacktrace instead of failing obscurely later when a null
      // test case object is passed around.
      try {
        // This is only way to access the underlying exception in the Fail object.
        parentBlock.evaluate();
      } catch (Throwable error) {
        Throwables.throwIfUnchecked(error);
        throw new RuntimeException(error);
      }
    }
    return new Statement() {
      @Override public void evaluate() throws Throwable {
        beforeTest(method, getTestClass().getJavaClass(), CURRENT_TEST.get());
        try {
          parentBlock.evaluate();
        } catch (InvocationTargetException e) {
          throw e.getTargetException();
        } finally {
          afterTest(method, getTestClass().getJavaClass(), CURRENT_TEST.get());
          CURRENT_TEST.remove();
        }
      }
    };
  }

  @Override
  protected Statement methodInvoker(final FrameworkMethod method, final Object test) {
    CURRENT_TEST.set(test);

    final Statement parentInvoke = super.methodInvoker(method, test);
    return new Statement() {
      @Override
      public void evaluate() throws Throwable {
        try {
          parentInvoke.evaluate();
        } finally {
          CURRENT_TEST.remove();
        }
      }
    };
  }

}
