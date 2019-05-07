package com.squareup.testing.annotationprocessing;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates a test method that should be run as a normal test, not in the context of annotation
 * processing.
 *
 * @see AnnotationProcessorTestRunner
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface NoProcess {
}
