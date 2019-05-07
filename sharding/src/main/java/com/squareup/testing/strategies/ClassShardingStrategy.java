package com.squareup.testing.strategies;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.squareup.testing.ChunkConfig;
import com.squareup.testing.ChunkIndexes;
import com.squareup.testing.RunTestOnShard;
import com.squareup.testing.ShardedTestType;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Preconditions.checkNotNull;

public class ClassShardingStrategy extends AbstractShardingStrategy {
  @Override public List<Class<?>> getClassesForChunk(ChunkConfig chunkConfig, PrintStream out) {
    checkNotNull(testClasses, "setTestClasses should be called before getClassesForChunk");
    List<Class<?>> chunkTestClasses = new ArrayList<>();

    ListMultimap<ShardedTestType, Class<?>> testClassesByShardedTestType =
        ShardedTestType.getTestClassesByShardedTestType(testClasses);
    List<Class<?>> allRunTestOnShardTests = testClassesByShardedTestType.get(ShardedTestType.RUN_TEST_ON_SHARD);
    List<Class<?>> allSlowTests = testClassesByShardedTestType.get(ShardedTestType.SLOW_TEST);
    List<Class<?>> allNormalTests = testClassesByShardedTestType.get(ShardedTestType.NORMAL_TEST);

    List<Class<?>> chunkSlowTests = ImmutableList.of();
    if (!allSlowTests.isEmpty()) {
      ChunkIndexes indexes = chunkConfig.getChunkIndexes(allSlowTests.size());
      if (indexes.size() > 0) {
        chunkSlowTests = allSlowTests.subList(indexes.getStartIndex(), indexes.getEndIndex());
        chunkTestClasses.addAll(chunkSlowTests);
        out.printf("*      Slow Tests Range: %d-%d (%d classes)\n", indexes.getStartIndex(), indexes.getEndIndex(), chunkSlowTests.size());
      } else {
        out.print("*      Slow Tests Range: NA\n");
      }
    }

    List<Class<?>> chunkNormalTests = ImmutableList.of();
    if (!allNormalTests.isEmpty()) {
      ChunkIndexes indexes = chunkConfig.getChunkIndexes(allNormalTests.size());
      if (indexes.size() > 0) {
        chunkNormalTests = allNormalTests.subList(indexes.getStartIndex(), indexes.getEndIndex());
        chunkTestClasses.addAll(chunkNormalTests);
        out.printf("*  Non-Slow Tests Range: %d-%d (%d classes)\n", indexes.getStartIndex(), indexes.getEndIndex(), chunkNormalTests.size());
      } else {
        out.print("*  Non-Slow Tests Range: NA\n");
      }
    }

    List<Class<?>> chunkRunTestOnShardTests = allRunTestOnShardTests.stream()
        .filter(clazz ->
            clazz.getAnnotation(RunTestOnShard.class).value() == (chunkConfig.runChunk - 1))
        .collect(Collectors.toList());
    chunkTestClasses.addAll(chunkRunTestOnShardTests);

    out.print("****************************************************\n");
    if (chunkSlowTests.size() > 0) {
      out.print("Slow tests to run:\n");
      chunkSlowTests.forEach(clazz -> out.printf(" - %s\n", clazz.getName()));
    }
    if (chunkNormalTests.size() > 0) {
      out.print("Non-Slow tests to run:\n");
      chunkNormalTests.forEach(clazz -> out.printf(" - %s\n", clazz.getName()));
    }
    if (chunkRunTestOnShardTests.size() > 0) {
      out.print("@RunTestOnShard tests to run:\n");
      chunkRunTestOnShardTests.forEach(clazz -> out.printf(" - %s\n", clazz.getName()));
    }

    return chunkTestClasses;
  }
}
