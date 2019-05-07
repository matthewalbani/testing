package com.squareup.testing.shardingtests;

import com.squareup.testing.ShardedTestSuite;
import com.squareup.testing.ShardingTestRunner;
import org.junit.runner.RunWith;

@RunWith(ShardingTestRunner.class)
@ShardedTestSuite(
    packagePrefix = "com.squareup.testing.shardingtests",
    shardRange = "1-5")
public final class ShardingWithRangeTestSuite {
}
