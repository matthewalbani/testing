package com.squareup.testing;

import com.google.common.collect.ListMultimap;
import com.squareup.core.stream.GuavaCollectors;
import java.util.List;

/** Grouping key for tests. */
public enum ShardedTestType {
  RUN_TEST_ON_SHARD,
  SLOW_TEST,
  NORMAL_TEST;

  public static ListMultimap<ShardedTestType, Class<?>> getTestClassesByShardedTestType(List<Class<?>> testClasses) {
    return testClasses.stream()
        .collect(GuavaCollectors.groupingIntoLists(clazz1 -> {
          if (clazz1.getAnnotation(RunTestOnShard.class) != null) {
            return ShardedTestType.RUN_TEST_ON_SHARD;
          }

          return new SlowTestPredicate().test(clazz1)
              ? ShardedTestType.SLOW_TEST
              : ShardedTestType.NORMAL_TEST;
        }));
  }
}
