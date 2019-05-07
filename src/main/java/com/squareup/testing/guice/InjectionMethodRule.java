package com.squareup.testing.guice;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;

import static com.google.common.base.Preconditions.checkNotNull;

public abstract class InjectionMethodRule implements MethodRule {

  private final boolean captureExceptions;
  private Injector injector;
  private AtomicReference<Exception> caughtException;

  /**
   * Initialize <i>without</i> catching injection exceptions.
   */
  public InjectionMethodRule() {
    this(false);
  }

  /**
   * Initialize a new rule, optionally capturing exceptions thrown during instantiation of the
   * {@link Injector}.
   */
  public InjectionMethodRule(boolean captureExceptions) {
    this.captureExceptions = captureExceptions;
  }

  /**
   * Returns the current {@link Injector} in use for this rule.
   */
  public Injector getInjector() {
    return checkNotNull(injector, "injector");
  }

  /**
   * Rethrows any exception caught during the instantiation of the injector.
   */
  public void rethrowCaughtException() throws Exception {
    Exception exception = caughtException.get();
    if (exception != null) {
      throw exception;
    }
  }

  protected abstract List<Module> modules();

  /** Called with the injector before executing a test */
  protected void before(Injector injector) {}

  /** Called with the injector after executing a test */
  protected void after(Injector injector) {}

  @Override public Statement apply(Statement base, FrameworkMethod method, Object target) {
    caughtException = new AtomicReference<>();
    try {
      injector = Guice.createInjector(modules());
    } catch (Exception e) {
      if (!captureExceptions) {
        throw e;
      }

      // capture the exception and continue executing the test method
      caughtException.set(e);
      return new Statement() {
        @Override public void evaluate() throws Throwable {
          base.evaluate();
        }
      };
    }

    return new Statement() {
      @Override public void evaluate() throws Throwable {
        try {
          injector.injectMembers(target);
          before(injector);
          base.evaluate();
        } finally {
          after(injector);
          Uninject.uninject(target);
        }
      }
    };
  }
}
