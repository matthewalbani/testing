package com.squareup.testing.runtestonshardtests;

import com.squareup.testing.ShardedTestSuite;
import com.squareup.testing.ShardingTestRunner;
import org.junit.runner.RunWith;

@RunWith(ShardingTestRunner.class)
@ShardedTestSuite(
    packagePrefix = "com.squareup.testing.runtestonshardtests")
public class RunTestOnShardTestSuite {
}
