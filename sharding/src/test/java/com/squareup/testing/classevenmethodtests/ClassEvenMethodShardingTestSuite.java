package com.squareup.testing.classevenmethodtests;

import com.squareup.testing.ShardedTestSuite;
import com.squareup.testing.ShardingStrategies;
import com.squareup.testing.ShardingTestRunner;
import org.junit.runner.RunWith;

@RunWith(ShardingTestRunner.class)
@ShardedTestSuite(
    packagePrefix = "com.squareup.testing.classevenmethodtests",
    shardingStrategy = ShardingStrategies.CLASS_EVEN_METHODS)
public final class ClassEvenMethodShardingTestSuite {
}
