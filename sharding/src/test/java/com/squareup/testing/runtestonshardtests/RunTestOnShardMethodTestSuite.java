package com.squareup.testing.runtestonshardtests;

import com.squareup.testing.ShardedTestSuite;
import com.squareup.testing.ShardingStrategies;
import com.squareup.testing.ShardingTestRunner;
import org.junit.runner.RunWith;

@RunWith(ShardingTestRunner.class)
@ShardedTestSuite(
    packagePrefix = "com.squareup.testing.runtestonshardtests",
    shardingStrategy = ShardingStrategies.METHOD)
public class RunTestOnShardMethodTestSuite {
}
