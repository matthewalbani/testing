package com.squareup.testing;

import com.google.common.collect.ImmutableList;
import com.squareup.testing.annotationtests.WhitelistOnlyMethodTestSuite;
import com.squareup.testing.methodtests.MethodShardingTestSuite;
import com.squareup.testing.shardingtests.ShardingWithRangeTestSuite;
import java.util.List;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class ShardingTestRunnerMethodTest extends ShardingTestRunnerBaseTest {
  @Test public void doesNotSplitAcrossOneShard() throws Exception {
    ShardingTestRunner shard = new ShardingTestRunner(MethodShardingTestSuite.class, ALL_CHUNKS);
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
        new ShardingTestRunner(MethodShardingTestSuite.class, new ChunkConfig(1, 1, false));
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
    List<String> allTests = ImmutableList.of("testSlow1", "testSlow2",
        "testBatch1_test1", "testBatch1_test2",
        "testBatch2_test1", "testBatch2_test2",
        "testBatch2_test3",
        "Cucumber 1 Scenario 1", "Cucumber 1 Scenario 2",
        "functionalTest1", "functionalTest2",
        "parameterized_test1[0]", "parameterized_test1[1]",
        "parameterized_test2[0]", "parameterized_test2[1]",
        "testDerived1", "testDerived2",
        "testFromAbstractClass"
    );

    ShardingTestRunner shard1Of2 = new ShardingTestRunner(MethodShardingTestSuite.class, new ChunkConfig(2, 1, true));
    List<String> shard1Tests = executedTests(shard1Of2);
    ShardingTestRunner shard2Of2 = new ShardingTestRunner(MethodShardingTestSuite.class, new ChunkConfig(2, 2, true));
    List<String> shard2Tests = executedTests(shard2Of2);

    ImmutableList<String> testsRan = ImmutableList.<String>builder()
        .addAll(shard1Tests)
        .addAll(shard2Tests)
        .build();

    assertThat(testsRan).containsExactlyInAnyOrderElementsOf(allTests);
    assertThat(shard1Tests).doesNotContainAnyElementsOf(shard2Tests);
    assertThat(shard2Tests).doesNotContainAnyElementsOf(shard1Tests);
    assertThat(shard1Tests).hasSizeGreaterThan(0);
    assertThat(shard2Tests).hasSizeGreaterThan(0);
  }

  @Test public void splitsMethodsAcrossSixShards() throws Exception {
    // TODO: If the abstract class is not on the same shard as the derived class then the test from the
    // abstract test will be skipped.  This is true for any parent test class that has a @Test in it.
    ShardingTestRunner shard1Of6 = new ShardingTestRunner(MethodShardingTestSuite.class, new ChunkConfig(6, 1, true));
    ShardingTestRunner shard2Of6 = new ShardingTestRunner(MethodShardingTestSuite.class, new ChunkConfig(6, 2, true));
    ShardingTestRunner shard3Of6 = new ShardingTestRunner(MethodShardingTestSuite.class, new ChunkConfig(6, 3, true));
    ShardingTestRunner shard4Of6 = new ShardingTestRunner(MethodShardingTestSuite.class, new ChunkConfig(6, 4, true));
    ShardingTestRunner shard5Of6 = new ShardingTestRunner(MethodShardingTestSuite.class, new ChunkConfig(6, 5, true));
    ShardingTestRunner shard6Of6 = new ShardingTestRunner(MethodShardingTestSuite.class, new ChunkConfig(6, 6, true));

    List<String> allTests = ImmutableList.of(
        "testSlow1",
        "Cucumber 1 Scenario 1", "Cucumber 1 Scenario 2",
        "testBatch1_test1",
        "testSlow2",
        "testBatch1_test2",
        "testBatch2_test1",
        "testBatch2_test2",
        "testBatch2_test3",
        "testDerived1", // "testFromAbstractClass"
        "testDerived2", // "testFromAbstractClass",
        "functionalTest1",
        "functionalTest2",
        "parameterized_test1[0]", "parameterized_test1[1]",
        "parameterized_test2[0]", "parameterized_test2[1]"
    );

    List<String> shard1Tests = executedTests(shard1Of6);
    List<String> shard2Tests = executedTests(shard2Of6);
    List<String> shard3Tests = executedTests(shard3Of6);
    List<String> shard4Tests = executedTests(shard4Of6);
    List<String> shard5Tests = executedTests(shard5Of6);
    List<String> shard6Tests = executedTests(shard6Of6);

    ImmutableList<String> testsRan = ImmutableList.<String>builder()
        .addAll(shard1Tests)
        .addAll(shard2Tests)
        .addAll(shard3Tests)
        .addAll(shard4Tests)
        .addAll(shard5Tests)
        .addAll(shard6Tests)
        .build();

    assertThat(testsRan).containsExactlyInAnyOrderElementsOf(allTests);
    assertThat(shard1Tests)
        .doesNotContainAnyElementsOf(shard2Tests)
        .doesNotContainAnyElementsOf(shard3Tests)
        .doesNotContainAnyElementsOf(shard4Tests)
        .doesNotContainAnyElementsOf(shard5Tests)
        .doesNotContainAnyElementsOf(shard6Tests);
    assertThat(shard2Tests)
        .doesNotContainAnyElementsOf(shard1Tests)
        .doesNotContainAnyElementsOf(shard3Tests)
        .doesNotContainAnyElementsOf(shard4Tests)
        .doesNotContainAnyElementsOf(shard5Tests)
        .doesNotContainAnyElementsOf(shard6Tests);
    assertThat(shard3Tests)
        .doesNotContainAnyElementsOf(shard1Tests)
        .doesNotContainAnyElementsOf(shard2Tests)
        .doesNotContainAnyElementsOf(shard4Tests)
        .doesNotContainAnyElementsOf(shard5Tests)
        .doesNotContainAnyElementsOf(shard6Tests);
    assertThat(shard4Tests)
        .doesNotContainAnyElementsOf(shard1Tests)
        .doesNotContainAnyElementsOf(shard2Tests)
        .doesNotContainAnyElementsOf(shard3Tests)
        .doesNotContainAnyElementsOf(shard5Tests)
        .doesNotContainAnyElementsOf(shard6Tests);
    assertThat(shard5Tests)
        .doesNotContainAnyElementsOf(shard1Tests)
        .doesNotContainAnyElementsOf(shard2Tests)
        .doesNotContainAnyElementsOf(shard3Tests)
        .doesNotContainAnyElementsOf(shard4Tests)
        .doesNotContainAnyElementsOf(shard6Tests);
    assertThat(shard6Tests)
        .doesNotContainAnyElementsOf(shard1Tests)
        .doesNotContainAnyElementsOf(shard2Tests)
        .doesNotContainAnyElementsOf(shard3Tests)
        .doesNotContainAnyElementsOf(shard4Tests)
        .doesNotContainAnyElementsOf(shard5Tests);

    assertThat(shard1Tests).hasSizeGreaterThan(0);
    assertThat(shard2Tests).hasSizeGreaterThan(0);
    assertThat(shard3Tests).hasSizeGreaterThan(0);
    assertThat(shard4Tests).hasSizeGreaterThan(0);
    assertThat(shard5Tests).hasSizeGreaterThan(0);
    assertThat(shard6Tests).hasSizeGreaterThan(0);
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
