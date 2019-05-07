package com.squareup.testing;

import com.squareup.testing.annotationtests.WhitelistOnlyMethodTestSuite;
import com.squareup.testing.methodslowteststests.MethodSlowTestsShardingTestSuite;
import com.squareup.testing.shardingtests.ShardingWithRangeTestSuite;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ShardingTestRunnerMethodSlowTestsTest extends ShardingTestRunnerBaseTest {
  @Test public void doesNotSplitAcrossOneShard() throws Exception {
    ShardingTestRunner shard = new ShardingTestRunner(MethodSlowTestsShardingTestSuite.class, ALL_CHUNKS);
    assertThat(executedTests(shard))
        .containsExactlyInAnyOrder(
            "testBatch2_test1", "testBatch2_test2", "testBatch2_test3",
            "testBatch1_test1", "testBatch1_test2",
            "testDerived1", "testFromAbstractClass",
            "testDerived2", "testFromAbstractClass",
            "testSlow1", "testSlow2",
            "functionalTest1", "functionalTest2",
            "parameterized_test1[0]", "parameterized_test2[0]",
            "parameterized_test1[1]", "parameterized_test2[1]",
            "Cucumber 1 Scenario 1", "Cucumber 1 Scenario 2");
  }

  @Test public void doesNotIncludeFunctionalTest() throws Exception {
    ShardingTestRunner shard =
        new ShardingTestRunner(MethodSlowTestsShardingTestSuite.class, new ChunkConfig(1, 1, false));
    assertThat(executedTests(shard))
        .containsExactlyInAnyOrder(
            "testBatch2_test1", "testBatch2_test2", "testBatch2_test3",
            "testBatch1_test1", "testBatch1_test2",
            "testDerived1", "testFromAbstractClass",
            "testDerived2", "testFromAbstractClass",
            "testSlow1", "testSlow2",
            "parameterized_test1[0]", "parameterized_test2[0]",
            "parameterized_test1[1]", "parameterized_test2[1]",
            "Cucumber 1 Scenario 1", "Cucumber 1 Scenario 2");
  }

  @Test public void splitsMethodsAcrossTwoShards() throws Exception {
    ShardingTestRunner shard1Of2 = new ShardingTestRunner(MethodSlowTestsShardingTestSuite.class, new ChunkConfig(2, 1, true));
    assertThat(executedTests(shard1Of2)).containsExactlyInAnyOrder(
        "testSlow1",
        "testBatch1_test1", "testBatch1_test2",
        "testBatch2_test1", "testBatch2_test2", "testBatch2_test3",
        "testDerived1", "testFromAbstractClass",
        "Cucumber 1 Scenario 1", "Cucumber 1 Scenario 2");
    ShardingTestRunner shard2Of2 = new ShardingTestRunner(MethodSlowTestsShardingTestSuite.class, new ChunkConfig(2, 2, true));
    assertThat(executedTests(shard2Of2)).containsExactlyInAnyOrder(
        "testSlow2",
        "testDerived2", "testFromAbstractClass",
        "functionalTest1", "functionalTest2",
        "parameterized_test1[0]", "parameterized_test2[0]",
        "parameterized_test1[1]", "parameterized_test2[1]");
  }

  @Test public void splitsMethodsAcrossSixShards() throws Exception {
    ShardingTestRunner shard1Of6 = new ShardingTestRunner(MethodSlowTestsShardingTestSuite.class, new ChunkConfig(6, 1, true));
    assertThat(executedTests(shard1Of6)).containsExactlyInAnyOrder(
        "testSlow1",
        "testBatch1_test1", "testBatch1_test2",
        "testBatch2_test1", "testBatch2_test2", "testBatch2_test3");

    ShardingTestRunner shard2Of6 = new ShardingTestRunner(MethodSlowTestsShardingTestSuite.class, new ChunkConfig(6, 2, true));
    assertThat(executedTests(shard2Of6)).containsExactlyInAnyOrder(
        "testSlow2",
        "Cucumber 1 Scenario 1", "Cucumber 1 Scenario 2");

    ShardingTestRunner shard3Of6 = new ShardingTestRunner(MethodSlowTestsShardingTestSuite.class, new ChunkConfig(6, 3, true));
    assertThat(executedTests(shard3Of6)).containsExactlyInAnyOrder(
        "testDerived1", "testFromAbstractClass");

    ShardingTestRunner shard4Of6 = new ShardingTestRunner(MethodSlowTestsShardingTestSuite.class, new ChunkConfig(6, 4, true));
    assertThat(executedTests(shard4Of6)).containsExactlyInAnyOrder(
        "testDerived2", "testFromAbstractClass");

    ShardingTestRunner shard5Of6 = new ShardingTestRunner(MethodSlowTestsShardingTestSuite.class, new ChunkConfig(6, 5, true));
    assertThat(executedTests(shard5Of6)).containsExactlyInAnyOrder(
        "functionalTest1", "functionalTest2");

    ShardingTestRunner shard6Of6 = new ShardingTestRunner(MethodSlowTestsShardingTestSuite.class, new ChunkConfig(6, 6, true));
    assertThat(executedTests(shard6Of6)).containsExactlyInAnyOrder(
        "parameterized_test1[0]", "parameterized_test2[0]",
        "parameterized_test1[1]", "parameterized_test2[1]");
  }

  @Test public void shardOutOfShardRange() throws Exception {
    ShardingTestRunner shard6Of6 = new ShardingTestRunner(
        ShardingWithRangeTestSuite.class, new ChunkConfig(6, 6, true));
    assertThat(executedTests(shard6Of6)).isEmpty();
  }

  @Test public void whitelistedTestSuite() throws Exception {
    ShardingTestRunner testRunner =
        new ShardingTestRunner(WhitelistOnlyMethodTestSuite.class, ALL_CHUNKS);
    assertThat(executedTests(testRunner)).containsExactlyInAnyOrder(
        "blacklistedTypeWhitelistedMethod",
        "whitelistedTypeUnannotatedMethod",
        "whitelistedTypeBlacklistedMethod",
        "whitelistedTypeWhitelistedMethod",
        "unannotatedTypeWhitelistedMethod",
        "parameterizedWhiteListed[0]",
        "parameterizedWhiteListed[1]");
  }
}
