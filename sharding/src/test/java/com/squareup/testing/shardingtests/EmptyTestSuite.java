package com.squareup.testing.shardingtests;

import com.squareup.testing.ShardedTestSuite;
import com.squareup.testing.ShardingTestRunner;
import org.junit.runner.RunWith;

@RunWith(ShardingTestRunner.class)
@ShardedTestSuite(packagePrefix = "com.squareup.testing.does.not.exist")
public class EmptyTestSuite {
}
