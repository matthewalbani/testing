package com.squareup.testing.annotationtests;

import com.squareup.testing.ShardedTestSuite;
import com.squareup.testing.ShardingTestRunner;
import org.junit.runner.RunWith;

@RunWith(ShardingTestRunner.class)
@ShardedTestSuite(
    packagePrefix = "com.squareup.testing.annotationtests",
    onlyIfAnnotated = Whitelisted.class)
public final class WhitelistOnlyTestSuite {
}
