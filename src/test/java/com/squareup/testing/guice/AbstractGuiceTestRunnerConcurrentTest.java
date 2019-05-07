// Copyright 2013, Square, Inc.

package com.squareup.testing.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Provides;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.squareup.core.testing.ConcurrentRunnerScheduler;
import com.squareup.testing.TestScoped;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.model.InitializationError;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

/** Test for {@link AbstractGuiceTestRunner} when used with {@link ConcurrentRunnerScheduler} */
@RunWith(AbstractGuiceTestRunnerConcurrentTest.ConcurrentGuiceTestRunner.class)
public class AbstractGuiceTestRunnerConcurrentTest {
  public static class ConcurrentGuiceTestRunner extends AbstractGuiceTestRunner {
    /** Injector shared between tests annotated with {@link SharedInjector} */
    private Injector sharedInjector;

    public ConcurrentGuiceTestRunner(Class<?> klass) throws InitializationError {
      super(klass);
      setScheduler(new ConcurrentRunnerScheduler());
    }

    @Override protected synchronized Injector getInjector() {
      if (getCurrentTestMethod().getAnnotation(SharedInjector.class) == null) {
        return createInjector();
      } else {
        if (sharedInjector == null) {
          sharedInjector = createInjector();
        }
        return sharedInjector;
      }
    }

    private Injector createInjector() {
      return Guice.createInjector(new TestModule(), new TestScopeModule());
    }
  }

  /**
   * Tests with this annotation share the same injector. This is used here to test both tests that
   * have the same and have a different injector.
   */
  @Retention(RetentionPolicy.RUNTIME)
  private @interface SharedInjector {
  }

  private static class TestModule extends AbstractModule {
    @Override protected void configure() {
    }

    @Provides @TestScoped @Named("testScoped") String testScoped() {
      return UUID.randomUUID().toString();
    }
  }

  private static final int TEST_COUNT = 3;
  private static final int AWAIT_SECONDS = 3;

  private static AtomicInteger testCount;
  private static CountDownLatch creationLatch;
  private static CountDownLatch testLatch;

  // these two fields are set in testOne and published by testLatch.await
  private static Injector testOneInjector;
  private static String testOneTestScoped;

  @Inject @Named("testScoped") private String testScoped;
  @Inject private Injector injector;

  public AbstractGuiceTestRunnerConcurrentTest() throws InterruptedException {
    // force all test initialization run in parallel, to more fully test handling of concurrency in
    // the runner
    testCount.incrementAndGet();
    creationLatch.countDown();
    assertTrue(creationLatch.await(AWAIT_SECONDS, TimeUnit.SECONDS));
  }

  @BeforeClass public static void setupClass() {
    testCount = new AtomicInteger(0);
    creationLatch = new CountDownLatch(TEST_COUNT);
    testLatch = new CountDownLatch(TEST_COUNT);
  }

  @Before public void before() {
    assertEquals(TEST_COUNT, testCount.get());
    assertNotNull(testScoped);
    assertNotNull(injector);
  }

  @After public void after() {
    assertEquals("parallel running of tests should not interfere with test scope", testScoped,
        injector.getInstance(Key.get(String.class, Names.named("testScoped"))));
  }

  @SharedInjector @Test public void testOne() throws InterruptedException {
    testOneTestScoped = testScoped;
    testOneInjector = injector;

    testLatch.countDown();
    assertTrue(testLatch.await(AWAIT_SECONDS, TimeUnit.SECONDS));
  }

  @SharedInjector @Test public void testWithSameInjector() throws InterruptedException {
    testLatch.countDown();
    assertTrue(testLatch.await(AWAIT_SECONDS, TimeUnit.SECONDS));

    assertSame(testOneInjector, injector);
    assertNotEquals(testOneTestScoped, testScoped);
  }

  @Test public void testWithDifferentInjector() throws InterruptedException {
    testLatch.countDown();
    assertTrue(testLatch.await(AWAIT_SECONDS, TimeUnit.SECONDS));

    assertNotSame(testOneInjector, injector);
    assertNotEquals(testOneTestScoped, testScoped);
  }
}
