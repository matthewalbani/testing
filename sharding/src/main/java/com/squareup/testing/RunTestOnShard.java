package com.squareup.testing;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Denotes which shard a particular test should be assigned when used with {@link
 * ShardingTestRunner}. This is zero-indexed.
 *
 * For example, if I run my test suite with 5 shards, a typical use case would be to manually assign
 * particular test cases to run on different shards via this annotation: {@code @RunTestOnShard(0)}
 * or {@code @RunTestOnShard{1}, all the way up to 4.
 */
@Target(TYPE)
@Retention(RUNTIME)
public @interface RunTestOnShard {
  int value();
}
