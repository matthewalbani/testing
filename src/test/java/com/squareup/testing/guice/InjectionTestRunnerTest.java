package com.squareup.testing.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.name.Names;
import com.squareup.testing.TestModule;
import com.squareup.testing.TestScoped;
import javax.inject.Inject;
import javax.inject.Named;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(InjectionTestRunner.class)
@TestModule(InjectionTestRunnerTest.SampleModule.class)
public class InjectionTestRunnerTest {

  public static class SampleModule extends AbstractModule {
    static int moduleConfigureCount = 0;

    @Override protected void configure() {
      assertEquals(0, moduleConfigureCount);
      moduleConfigureCount++;
      bindConstant().annotatedWith(Names.named("myConstant")).to("my-value");
    }

    @Provides @TestScoped @Named("testScoped") public String testScoped() {
      return "tstScoped";
    }
  }

  @Inject @Named("myConstant") String injectedMember;
  @Inject @Named("testScoped") String testScoped;

  @Test public void injectsMembers() {
    assertEquals("my-value", injectedMember);
  }

  @Test public void moduleInitializedOnlyOnce() {
    assertEquals(1, SampleModule.moduleConfigureCount);
  }

  // @TestScoped is more extensively tested in AbstractGuiceTestRunnerTest. Here we just test that
  // the test scope is enabled automatically by the InjectionTestRunner.
  @Test public void testScope() {
    assertEquals("tstScoped", testScoped);
  }
}
