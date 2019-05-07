package com.squareup.testing.guice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a {@code static} field of a test ran by the {@link InjectionTestRunner} as source for the
 * injector to be used to inject the test. The field's type should implement {@link
 * InjectorSupplier}.
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface TestInjector {
}
