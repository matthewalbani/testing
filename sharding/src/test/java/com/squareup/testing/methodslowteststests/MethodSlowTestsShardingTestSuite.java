package com.squareup.testing.methodslowteststests;

import com.squareup.testing.ShardedTestSuite;
import com.squareup.testing.ShardingStrategies;
import com.squareup.testing.ShardingTestRunner;
import org.junit.runner.RunWith;

@RunWith(ShardingTestRunner.class)
@ShardedTestSuite(
    packagePrefix = "com.squareup.testing.methodslowteststests",
    shardingStrategy = ShardingStrategies.METHOD_SLOW_TESTS)
public final class MethodSlowTestsShardingTestSuite {
}
