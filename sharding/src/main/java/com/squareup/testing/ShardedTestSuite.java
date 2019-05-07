package com.squareup.testing;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Denotes a Java package prefix containing JUnit tests to be grouped together
 * into a sharded build for tests using <code>@RunWith(ShardingTestRunner.class)</code>
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface ShardedTestSuite {
  String packagePrefix();

  /** Whitelist to run only tests with this annotation. */
  Class<? extends Annotation> onlyIfAnnotated() default Annotation.class;

  /** Blacklist to run all prefixed tests except those with this annotation. */
  Class<? extends Annotation> unlessAnnotated() default Annotation.class;

  /** Determines how tests are grouped in the shards */
  ShardingStrategies shardingStrategy() default ShardingStrategies.CLASS;

  String shardRange() default "";

  /** Ordering for tests, the default is order by name */
  TestOrderings testOrdering() default TestOrderings.NAME;
}
