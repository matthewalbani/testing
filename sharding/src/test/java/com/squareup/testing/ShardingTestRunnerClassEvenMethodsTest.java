package com.squareup.testing;

import com.squareup.testing.classevenmethodtests.ClassEvenMethodShardingTestSuite;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ShardingTestRunnerClassEvenMethodsTest extends ShardingTestRunnerBaseTest {
  @Test public void unevenSplit() throws Exception {
    ShardingTestRunner shard1 = new ShardingTestRunner(
        ClassEvenMethodShardingTestSuite.class, new ChunkConfig(3, 1, true));
    assertThat(executedTests(shard1)).containsExactlyInAnyOrder(
        "testBatch3_test1", "testBatch3_test2", "testBatch3_test3", "testBatch3_test4");

    ShardingTestRunner shard2 = new ShardingTestRunner(
        ClassEvenMethodShardingTestSuite.class, new ChunkConfig(3, 2, true));
    assertThat(executedTests(shard2)).containsExactlyInAnyOrder(
        "testBatch4_test1", "testBatch4_test2", "testBatch4_test3");

    ShardingTestRunner shard3 = new ShardingTestRunner(
        ClassEvenMethodShardingTestSuite.class, new ChunkConfig(3, 3, true));
    assertThat(executedTests(shard3)).containsExactlyInAnyOrder(
        "testBatch2_test1", "testBatch2_test2", "testBatch1_test1");
  }
}
