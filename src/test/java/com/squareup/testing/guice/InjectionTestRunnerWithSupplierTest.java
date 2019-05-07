package com.squareup.testing.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import javax.inject.Inject;
import javax.inject.Named;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

@RunWith(InjectionTestRunner.class)
public class InjectionTestRunnerWithSupplierTest {

  private static class TestInjectorSupplier implements InjectorSupplier {
    private int callCount;

    @Override public Injector getInjector() {
      assertEquals(0, callCount);
      callCount++;
      return Guice.createInjector(new AbstractModule() {
        @Override protected void configure() {
          bindConstant().annotatedWith(Names.named("myConstant")).to("my-value");
        }
      }, new ComponentTestSupportModule());
    }
  }

  @TestInjector public static TestInjectorSupplier injectorSupplier = new TestInjectorSupplier();

  @Inject @Named("myConstant") String injectedMember;

  @Test public void injectsMembers() {
    assertEquals("my-value", injectedMember);
  }

  @Test public void moduleInitializedOnlyOnce() {
    assertEquals(1, injectorSupplier.callCount);
  }
}
