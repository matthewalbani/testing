package com.squareup.testing;

import com.google.common.collect.ImmutableList;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;

public class ShardingTestRunnerBaseTest {
  static final ChunkConfig ALL_CHUNKS = new ChunkConfig(1, 1, true);

  /** To figure out which tests are run by {@code testRunner}, we run them! */
  static ImmutableList<String> executedTests(ShardingTestRunner testRunner) {
    ImmutableList.Builder<String> result = ImmutableList.builder();

    RunNotifier runNotifier = new RunNotifier();
    runNotifier.addListener(new RunListener() {
      @Override public void testStarted(Description description) throws Exception {
        result.add(description.getMethodName());
      }
    });
    testRunner.run(runNotifier);

    return result.build();
  }
}
