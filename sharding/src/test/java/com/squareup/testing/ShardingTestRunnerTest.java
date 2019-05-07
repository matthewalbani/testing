package com.squareup.testing;

import com.squareup.testing.annotationtests.BlacklistOnlyTestSuite;
import com.squareup.testing.annotationtests.UnannotatedTestSuite;
import com.squareup.testing.annotationtests.WhitelistAndBlacklistTestSuite;
import com.squareup.testing.annotationtests.WhitelistOnlyTestSuite;
import com.squareup.testing.shardingtests.EmptyTestSuite;
import com.squareup.testing.shardingtests.ShardingSortHashOfNameTestSuite;
import com.squareup.testing.shardingtests.ShardingTestSuite;
import com.squareup.testing.shardingtests.ShardingWithRangeTestSuite;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

public final class ShardingTestRunnerTest extends ShardingTestRunnerBaseTest {
  @Test public void returnsNothingForEmptyTestSuite() throws Exception {
    ShardingTestRunner shard = new ShardingTestRunner(EmptyTestSuite.class, ALL_CHUNKS);
    assertThat(executedTests(shard)).isEmpty();
  }

  @Test public void doesNotSplitAcrossOneShard() throws Exception {
    ShardingTestRunner shard = new ShardingTestRunner(ShardingTestSuite.class, ALL_CHUNKS);
    assertThat(executedTests(shard))
        .containsExactly(
            "testBatch1_test1", "testBatch1_test2",
            "testBatch2_test1", "testBatch2_test2", "testBatch2_test3",
            "Cucumber 1 Scenario 1", "Cucumber 1 Scenario 2",
            "testDerived1", "testFromAbstractClass",
            "testDerived2", "testFromAbstractClass",
            "functionalTest1", "functionalTest2",
            "parameterized_test1[0]", "parameterized_test2[0]",
            "parameterized_test1[1]", "parameterized_test2[1]",
            "testSlow1", "testSlow2");
  }

  @Test public void testMethodSortingByHashOfName() throws Exception {
    ShardingTestRunner shard = new ShardingTestRunner(ShardingSortHashOfNameTestSuite.class, ALL_CHUNKS);
    assertThat(executedTests(shard))
        .containsExactly(
            "Cucumber 1 Scenario 1", "Cucumber 1 Scenario 2",
            "testSlow1", "testSlow2",
            "testBatch2_test1", "testBatch2_test2", "testBatch2_test3",
            "testDerived2", "testFromAbstractClass",
            "testBatch1_test1", "testBatch1_test2",
            "functionalTest1", "functionalTest2",
            "testDerived1", "testFromAbstractClass",
            "parameterized_test1[0]", "parameterized_test2[0]",
            "parameterized_test1[1]", "parameterized_test2[1]");
  }

  @Test public void doesNotIncludeFunctionalTest() throws Exception {
    ShardingTestRunner shard =
        new ShardingTestRunner(ShardingTestSuite.class, new ChunkConfig(1, 1, false));
    assertThat(executedTests(shard))
        .containsExactlyInAnyOrder(
            "testBatch2_test1", "testBatch2_test2", "testBatch2_test3",
            "testBatch1_test1", "testBatch1_test2",
            "Cucumber 1 Scenario 1", "Cucumber 1 Scenario 2",
            "testDerived1", "testFromAbstractClass",
            "testDerived2", "testFromAbstractClass",
            "testSlow1", "testSlow2",
            "parameterized_test1[0]", "parameterized_test2[0]",
            "parameterized_test1[1]", "parameterized_test2[1]");
  }

  @Test public void evenSplit() throws Exception {
    ShardingTestRunner shard1 = new ShardingTestRunner(
        ShardingTestSuite.class, new ChunkConfig(3, 1, true));
    assertThat(executedTests(shard1)).containsExactlyInAnyOrder(
        "testSlow1", "testSlow2",
        "testBatch1_test1", "testBatch1_test2",
        "testBatch2_test1", "testBatch2_test2", "testBatch2_test3",
        "Cucumber 1 Scenario 1", "Cucumber 1 Scenario 2");

    ShardingTestRunner shard2 = new ShardingTestRunner(
        ShardingTestSuite.class, new ChunkConfig(3, 2, true));
    assertThat(executedTests(shard2)).containsExactlyInAnyOrder(
        "testDerived1", "testFromAbstractClass",
        "testDerived2", "testFromAbstractClass");

    ShardingTestRunner shard3 = new ShardingTestRunner(
        ShardingTestSuite.class, new ChunkConfig(3, 3, true));
    assertThat(executedTests(shard3)).containsExactlyInAnyOrder(
        "functionalTest1", "functionalTest2",
        "parameterized_test1[0]", "parameterized_test2[0]",
        "parameterized_test1[1]", "parameterized_test2[1]");
  }

  @Test public void unevenSplit() throws Exception {
    ShardingTestRunner shard1 = new ShardingTestRunner(
        ShardingTestSuite.class, new ChunkConfig(2, 1, true));
    assertThat(executedTests(shard1)).containsExactlyInAnyOrder(
        "testSlow1", "testSlow2",
        "testBatch1_test1", "testBatch1_test2", "testBatch2_test1",
        "testBatch2_test2", "testBatch2_test3",
        "testDerived1", "testFromAbstractClass",
        "Cucumber 1 Scenario 1", "Cucumber 1 Scenario 2");

    ShardingTestRunner shard2 = new ShardingTestRunner(
        ShardingTestSuite.class, new ChunkConfig(2, 2, true));
    assertThat(executedTests(shard2)).containsExactlyInAnyOrder(
        "testDerived2", "testFromAbstractClass",
        "functionalTest1", "functionalTest2",
        "parameterized_test1[0]", "parameterized_test2[0]",
        "parameterized_test1[1]", "parameterized_test2[1]");
  }

  @Test public void shardOutOfShardRange() throws Exception {
    ShardingTestRunner shard6Of6 = new ShardingTestRunner(
        ShardingWithRangeTestSuite.class, new ChunkConfig(6, 6, true));
    assertThat(executedTests(shard6Of6)).isEmpty();
  }

  @Test public void noAnnotations() throws Exception {
    ShardingTestRunner testRunner = new ShardingTestRunner(UnannotatedTestSuite.class, ALL_CHUNKS);
    assertThat(executedTests(testRunner)).containsExactlyInAnyOrder(
        "blacklistedTypeUnannotatedMethod",
        "blacklistedTypeBlacklistedMethod",
        "blacklistedTypeWhitelistedMethod",
        "whitelistedTypeUnannotatedMethod",
        "whitelistedTypeBlacklistedMethod",
        "whitelistedTypeWhitelistedMethod",
        "unannotatedTypeUnannotatedMethod",
        "unannotatedTypeBlacklistedMethod",
        "unannotatedTypeWhitelistedMethod",
        "parameterizedWhiteListed[0]",
        "parameterizedWhiteListed[1]",
        "parameterizedBlackListed[0]",
        "parameterizedBlackListed[1]");
  }

  @Test public void whitelistedTestSuite() throws Exception {
    ShardingTestRunner testRunner =
        new ShardingTestRunner(WhitelistOnlyTestSuite.class, ALL_CHUNKS);
    assertThat(executedTests(testRunner)).containsExactlyInAnyOrder(
        "blacklistedTypeWhitelistedMethod",
        "whitelistedTypeUnannotatedMethod",
        "whitelistedTypeBlacklistedMethod",
        "whitelistedTypeWhitelistedMethod",
        "unannotatedTypeWhitelistedMethod",
        "parameterizedWhiteListed[0]",
        "parameterizedWhiteListed[1]");
  }

  @Test public void blacklistedTestSuite() throws Exception {
    ShardingTestRunner testRunner =
        new ShardingTestRunner(BlacklistOnlyTestSuite.class, ALL_CHUNKS);
    assertThat(executedTests(testRunner)).containsExactlyInAnyOrder(
        "whitelistedTypeUnannotatedMethod",
        "whitelistedTypeWhitelistedMethod",
        "unannotatedTypeUnannotatedMethod",
        "unannotatedTypeWhitelistedMethod",
        "parameterizedWhiteListed[0]",
        "parameterizedWhiteListed[1]");
  }

  /** Whitelists with blacklists at the same time isn't supported. */
  @Test public void whitelistAndBlacklistedTestSuite() throws Exception {
    try {
      new ShardingTestRunner(WhitelistAndBlacklistTestSuite.class, ALL_CHUNKS);
      fail();
    } catch (IllegalArgumentException expected) {
    }
  }
}
