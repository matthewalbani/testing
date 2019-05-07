package com.squareup.testing.shardingtests;

import com.squareup.testing.ShardedTestSuite;
import com.squareup.testing.ShardingTestRunner;
import com.squareup.testing.TestOrderings;
import org.junit.runner.RunWith;

@RunWith(ShardingTestRunner.class)
@ShardedTestSuite(
    packagePrefix = "com.squareup.testing.shardingtests",
    testOrdering = TestOrderings.HASH_OF_NAME)
public class ShardingSortHashOfNameTestSuite {
}
