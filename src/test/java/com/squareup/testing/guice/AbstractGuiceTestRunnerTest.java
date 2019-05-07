package com.squareup.testing.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.matcher.Matchers;
import com.google.inject.name.Names;
import com.squareup.testing.TestScoped;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import javax.inject.Inject;
import javax.inject.Named;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@RunWith(AbstractGuiceTestRunnerTest.SampleGuiceTestRunner.class)
public class AbstractGuiceTestRunnerTest {

  public static class SampleGuiceTestRunner extends AbstractGuiceTestRunner {
    private boolean beforeInjectionCalled;

    public SampleGuiceTestRunner(Class<?> klass) throws InitializationError {
      super(klass);
    }

    @Override protected void beforeClassInjection() {
      super.beforeClassInjection();
      assertFalse(beforeInjectionCalled);
      beforeInjectionCalled = true;
    }

    @Override protected void beforeMemberInjection(FrameworkMethod testMethod) {
      super.beforeMemberInjection(testMethod);
      assertNotNull(testMethod);
      assertFalse(beforeInjectionCalled);
      beforeInjectionCalled = true;
    }

    @Override protected Injector getInjector() {
      assertTrue("getInjector should only be called once per test, after beforeInjection()",
          beforeInjectionCalled);
      beforeInjectionCalled = false;

      assertTrue("@BeforeClassInjection method must be called before creating the injector",
          annotatedBeforeClassInjectionCalled);

      return Guice.createInjector(new SampleModule());
    }
  }

  public static class SampleModule extends AbstractModule {
    @Override protected void configure() {
      install(new TestScopeModule());
      bindConstant().annotatedWith(Names.named("myConstant")).to("my-value");
      AopTester aopTester = new AopTester();
      bindInterceptor(Matchers.any(), Matchers.annotatedWith(TestAop.class), aopTester);
      bind(AopTester.class).toInstance(aopTester);
    }

    @Provides @TestScoped @Named("testScoped") public String testScoped() {
      return "tstScoped";
    }
  }

  @Retention(RetentionPolicy.RUNTIME) @Target(ElementType.METHOD)
  @interface TestAop {}

  static class AopTester implements MethodInterceptor {

    boolean intercepted = false;

    @Override public Object invoke(MethodInvocation methodInvocation) throws Throwable {
      this.intercepted = true;
      return methodInvocation.proceed();
    }
  }

  @Inject @ClassRule public static InjectedTestRule injectedStaticTestRule;
  @Inject @Named("myConstant") static String injectedStatic;
  private static boolean annotatedBeforeClassInjectionCalled;

  @Inject @Rule public InjectedMethodRule injectedMethodRule;
  @Inject @Rule public InjectedTestRule injectedTestRule;
  @Inject @Named("myConstant") String injectedMember;

  @Inject TestScopedValue testScoped1;
  @Inject TestScopedValue testScoped2;
  @Inject AopTester aopTester;

  @BeforeClassInjection public static void beforeClassInjection() {
    assertNull("called before injection", injectedStatic);
    assertFalse("called only once", annotatedBeforeClassInjectionCalled);
    annotatedBeforeClassInjectionCalled = true;
  }

  @BeforeClass public static void injectsStaticsBeforeClass() {
    assertEquals("my-value", injectedStatic);
  }

  @Before public void injectsMembersBeforeMethods() {
    assertEquals("my-value", injectedMember);
  }

  @Test public void injectsMembers() {
    assertEquals("my-value", injectedMember);
  }

  @Test public void injectsStatics() {
    assertEquals("my-value", injectedStatic);
  }

  @Test public void injectMethodRule() {
    assertNotNull(injectedMethodRule);
    assertEquals("my-value", injectedMethodRule.constant);
    // this requires the rule to be injected *before* the test runner looks for it
    assertTrue(injectedMethodRule.called);
  }

  @Test public void injectTestRule() {
    assertInjectedTestRule(injectedTestRule);
  }

  @Test public void injectStaticTestRule() {
    assertInjectedTestRule(injectedStaticTestRule);
  }

  private void assertInjectedTestRule(InjectedTestRule testRule) {
    assertNotNull(testRule);
    assertEquals("my-value", testRule.constant);
    // this requires the rule to be injected *before* the test runner looks for it
    assertTrue(testRule.called);
  }

  @Test public void testTestScope1() {
    testTestScope();
  }

  @Test public void testTestScope2() {
    testTestScope();
  }

  @Test public void testAop() {
    assertFalse(aopTester.intercepted);
    interceptMethod();
    assertTrue(aopTester.intercepted);
  }

  @TestAop
  protected void interceptMethod() {}

  private void testTestScope() {
    // within the same test run the same instance should be injected
    assertSame(testScoped1, testScoped2);
    // check that each test run gets its own instance
    assertFalse(testScoped1.seen);
    testScoped1.seen = true;
  }

  private static class InjectedMethodRule implements MethodRule {
    private final String constant;
    private boolean called;

    @Inject private InjectedMethodRule(@Named("myConstant") String constant) {
      this.constant = constant;
    }

    @Override public Statement apply(final Statement base, FrameworkMethod method, Object target) {
      return new Statement() {
        @Override public void evaluate() throws Throwable {
          called = true;
          base.evaluate();
        }
      };
    }
  }

  private static class InjectedTestRule extends ExternalResource {
    private final String constant;
    private boolean called;

    @Inject private InjectedTestRule(@Named("myConstant") String constant) {
      this.constant = constant;
    }

    @Override protected void before() throws Throwable {
      called = true;
    }
  }

  @TestScoped
  private static class TestScopedValue {
    private boolean seen;
  }
}
