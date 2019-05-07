// Copyright 2017 Square, Inc.
package com.squareup.testing.fixtures;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * marks a field as a ConcreteFake.
 *
 * ConcreteFake fields get initialized with TestIdGenerator.
 */
@Target(FIELD)
@Retention(RUNTIME)
@Documented
public @interface ConcreteFake {
}
