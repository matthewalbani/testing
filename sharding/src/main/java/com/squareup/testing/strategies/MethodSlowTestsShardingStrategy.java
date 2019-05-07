package com.squareup.testing.strategies;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.squareup.testing.ChunkConfig;
import com.squareup.testing.ChunkIndexes;
import com.squareup.testing.RunTestOnShard;
import com.squareup.testing.ShardedTestType;
import com.squareup.testing.ShardingStrategy;
import com.squareup.testing.TestMethodFilter;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.manipulation.Filter;

import static com.google.common.base.Preconditions.checkNotNull;

public class MethodSlowTestsShardingStrategy extends AbstractShardingStrategy {
  private List<Method> chunkTestMethods;

  @Override
  public List<Class<?>> getClassesForChunk(ChunkConfig chunkConfig, PrintStream out) {
    checkNotNull(testClasses, "setTestClasses should be called before getClassesForChunk");
    chunkTestMethods = new ArrayList<>();

    List<Class<?>> testClassesForChunk;

    ListMultimap<ShardedTestType, Class<?>> testClassesByShardedTestType =
        ShardedTestType.getTestClassesByShardedTestType(testClasses);

    List<Class<?>> allSlowTests = testClassesByShardedTestType.get(ShardedTestType.SLOW_TEST);
    if (allSlowTests.isEmpty()) {
      // If there are no slowTests just partition everything by class.
      ShardingStrategy classShardingStrategy = new ClassShardingStrategy();
      classShardingStrategy.setTestClasses(testClasses);
      testClassesForChunk = classShardingStrategy.getClassesForChunk(chunkConfig, out);
    } else {
      List<Method> allSlowTestMethods = getTestMethods(allSlowTests);
      allSlowTestMethods.sort(Comparator.comparing(Method::toString));

      // Shard methods from Slow Tests across chunks by method
      final List<Method> chunkSlowTestMethods;
      ChunkIndexes chunkIndexes = chunkConfig.getChunkIndexes(allSlowTestMethods.size());
      if (chunkIndexes.size() > 0) {
        chunkSlowTestMethods = allSlowTestMethods.subList(chunkIndexes.getStartIndex(), chunkIndexes.getEndIndex());
      } else {
        chunkSlowTestMethods = ImmutableList.of();
      }
      chunkTestMethods.addAll(chunkSlowTestMethods);

      // Shard methods from normal Tests by test class
      List<Class<?>> chunkNormalTests = ImmutableList.of();
      List<Class<?>> allNormalTests = testClassesByShardedTestType.get(ShardedTestType.NORMAL_TEST);
      ChunkIndexes indexes = chunkConfig.getChunkIndexes(allNormalTests.size());
      if (indexes.size() > 0) {
        chunkNormalTests = allNormalTests.subList(indexes.getStartIndex(), indexes.getEndIndex());
      }
      List<Method> chunkNormalTestMethods = getTestMethods(chunkNormalTests);
      chunkTestMethods.addAll(chunkNormalTestMethods);

      // Shard methods from @RunTestOnShard Tests by test class
      List<Class<?>> allRunTestOnShardTestClasses = testClassesByShardedTestType.get(ShardedTestType.RUN_TEST_ON_SHARD);
      List<Method> chunkRunTestOnShardMethods = Lists.newArrayList();
      for (Class runTestOnShardTestClass : allRunTestOnShardTestClasses) {
        int chunk = ((RunTestOnShard) runTestOnShardTestClass.getAnnotation(RunTestOnShard.class)).value();
        if (chunk == (chunkConfig.runChunk - 1)) {
          List<Method> runTestOnShardTestMethods = getTestMethods(runTestOnShardTestClass);
          chunkTestMethods.addAll(runTestOnShardTestMethods);
          chunkRunTestOnShardMethods.addAll(runTestOnShardTestMethods);
        }
      }

      chunkTestMethods.sort(Comparator.comparing(Method::toString));

      out.print("*************** METHOD PARTITIONING ****************\n");
      out.printf("*            Test Methods: %d\n", chunkTestMethods.size());
      out.printf("*       Slow Test Methods: %d\n", chunkSlowTestMethods.size());
      out.printf("*   Non-Slow Test Methods: %d\n", chunkNormalTestMethods.size());
      out.printf("* @RunTestOnShard Methods: %d\n", chunkRunTestOnShardMethods.size());
      out.print("****************************************************\n");
      chunkTestMethods.forEach(method -> {
        out.printf(" - %s#%s%s\n",
            method.getDeclaringClass().getCanonicalName(),
            method.getName(),
            chunkSlowTestMethods.contains(method) ? " [SLOW]" :
                (chunkRunTestOnShardMethods.contains(method) ? " [RUN_ON_SHARD]" : ""));
      });

      Set<Class<?>> chunkTestClasses = Sets.newHashSet();
      chunkTestMethods.forEach(method -> chunkTestClasses.add(method.getDeclaringClass()));
      chunkTestClasses.addAll(chunkNormalTests);
      testClassesForChunk = new ArrayList<>(chunkTestClasses);
    }
    return testClassesForChunk;
  }

  @Override
  public Filter getFilter(ChunkConfig chunkConfig) {
    return new TestMethodFilter(chunkTestMethods);
  }

  private List<Method> getTestMethods(List<Class<?>> slowTests) {
    List<Method> testMethods = Lists.newArrayList();
    for (Class testClass : slowTests) {
      testMethods.addAll(getTestMethods(testClass));
    }
    return testMethods;
  }

  private List<Method> getTestMethods(Class<?> testClass) {
    List<Method> testMethods = Lists.newArrayList();
    for (Method method : testClass.getMethods()) {
      if (method.isAnnotationPresent(Test.class)) {
        testMethods.add(method);
      }
    }
    return testMethods;
  }
}
