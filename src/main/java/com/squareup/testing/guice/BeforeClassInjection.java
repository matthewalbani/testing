package com.squareup.testing.guice;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Similar to {@link org.junit.BeforeClass @BeforeClass}, but executes the static method before the
 * class is statically injected. Methods annotated with {@link org.junit.BeforeClass @BeforeClass}
 * on the other hand are run after injection.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface BeforeClassInjection {
}
