package com.squareup.testing;

import javax.inject.Scope;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Use TestScoped to cache values for the duration of a test.
 *
 * An example use is to have a UserId that changes each test, but you want it to be consistent for
 * the duration of the test.
 */
@Target({ElementType.TYPE, ElementType.METHOD}) @Retention(RetentionPolicy.RUNTIME) @Scope
public @interface TestScoped {
}
