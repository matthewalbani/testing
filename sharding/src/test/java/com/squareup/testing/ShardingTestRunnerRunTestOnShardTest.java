package com.squareup.testing;

import com.google.common.collect.ImmutableList;
import com.squareup.testing.runtestonshardtests.RunTestOnShardMethodSlowTestsTestSuite;
import com.squareup.testing.runtestonshardtests.RunTestOnShardMethodTestSuite;
import com.squareup.testing.runtestonshardtests.RunTestOnShardTestSuite;
import java.util.List;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ShardingTestRunnerRunTestOnShardTest extends ShardingTestRunnerBaseTest {
  @Test public void assignsTeststoRunTestShard_shardByClass() throws Exception {
    ShardingTestRunner shard1 = new ShardingTestRunner(
        RunTestOnShardTestSuite.class, new ChunkConfig(3, 1, true));
    assertThat(executedTests(shard1)).containsExactlyInAnyOrder(
        "testSlow1", "testSlow2",
        "testBatch2_test1", "testBatch2_test2", "testBatch2_test3");

    ShardingTestRunner shard2 = new ShardingTestRunner(
        RunTestOnShardTestSuite.class, new ChunkConfig(3, 2, true));
    assertThat(executedTests(shard2)).isEmpty();

    ShardingTestRunner shard3 = new ShardingTestRunner(
        RunTestOnShardTestSuite.class, new ChunkConfig(3, 3, true));
    assertThat(executedTests(shard3)).containsExactlyInAnyOrder(
        "testBatch1_test1", "testBatch1_test2");
  }

  @Test public void assignsTeststoRunTestShard_shardByMethod() throws Exception {
    ShardingTestRunner shard1 = new ShardingTestRunner(
        RunTestOnShardMethodTestSuite.class, new ChunkConfig(3, 1, true));
    ShardingTestRunner shard2 = new ShardingTestRunner(
        RunTestOnShardMethodTestSuite.class, new ChunkConfig(3, 2, true));
    ShardingTestRunner shard3 = new ShardingTestRunner(
        RunTestOnShardMethodTestSuite.class, new ChunkConfig(3, 3, true));
    List<String> shard1Tests = executedTests(shard1);
    List<String> shard2Tests = executedTests(shard2);
    List<String> shard3Tests = executedTests(shard3);
    List<String> allTests = ImmutableList.of(
        "testSlow1", "testBatch2_test1",
        "testSlow2", "testBatch2_test2",
        "testBatch2_test3",
        "testBatch1_test1", "testBatch1_test2"
    );
    List<String> testsRan = ImmutableList.<String>builder()
        .addAll(shard1Tests)
        .addAll(shard2Tests)
        .addAll(shard3Tests)
        .build();

    assertThat(testsRan).containsExactlyInAnyOrderElementsOf(allTests);
    assertThat(shard1Tests)
        .doesNotContainAnyElementsOf(shard2Tests)
        .doesNotContainAnyElementsOf(shard3Tests);
    assertThat(shard2Tests)
        .doesNotContainAnyElementsOf(shard1Tests)
        .doesNotContainAnyElementsOf(shard3Tests);
    assertThat(shard3Tests)
        .doesNotContainAnyElementsOf(shard1Tests)
        .doesNotContainAnyElementsOf(shard2Tests);

    // Batch1Test must be run on shard 3.
    assertThat(shard3Tests).contains("testBatch1_test1", "testBatch1_test2");
  }

  @Test public void assignsTeststoRunTestShard_shardByMethodSlowTests() throws Exception {
    ShardingTestRunner shard1 = new ShardingTestRunner(
        RunTestOnShardMethodSlowTestsTestSuite.class, new ChunkConfig(3, 1, true));
    assertThat(executedTests(shard1)).containsExactlyInAnyOrder(
        "testSlow1",
        "testBatch2_test1", "testBatch2_test2", "testBatch2_test3");

    ShardingTestRunner shard2 = new ShardingTestRunner(
        RunTestOnShardMethodSlowTestsTestSuite.class, new ChunkConfig(3, 2, true));
    assertThat(executedTests(shard2)).containsExactlyInAnyOrder(
        "testSlow2");

    ShardingTestRunner shard3 = new ShardingTestRunner(
        RunTestOnShardMethodSlowTestsTestSuite.class, new ChunkConfig(3, 3, true));
    assertThat(executedTests(shard3)).containsExactlyInAnyOrder(
        "testBatch1_test1", "testBatch1_test2");
  }
}
