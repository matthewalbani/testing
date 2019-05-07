package com.squareup.testing.strategies;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.squareup.testing.ChunkConfig;
import com.squareup.testing.ChunkIndexes;
import com.squareup.testing.RunTestOnShard;
import com.squareup.testing.ShardedTestType;
import com.squareup.testing.TestMethodFilter;
import cucumber.api.junit.Cucumber;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runner.manipulation.Filter;

import static com.google.common.base.Preconditions.checkNotNull;

// TODO: Still doesn't work if a parent test class with an @Test method is not included on the same
// shard as the derived class
public class MethodShardingStrategy extends AbstractShardingStrategy {
  private List<Method> chunkTestMethods;
  private HashFunction murmur = Hashing.murmur3_32();

  @Override
  public List<Class<?>> getClassesForChunk(ChunkConfig chunkConfig, PrintStream out) {
    checkNotNull(testClasses, "setTestClasses should be called before getClassesForChunk");
    chunkTestMethods = new ArrayList<>();

    ListMultimap<ShardedTestType, Class<?>> testClassesByShardedTestType =
        ShardedTestType.getTestClassesByShardedTestType(testClasses);

    chunkTestMethods.addAll(getChunkTestMethods(chunkConfig, testClassesByShardedTestType));

    // Shard methods from @RunTestOnShard Tests by test class with round-robin sharding
    List<Class<?>> allRunTestOnShardTestClasses = testClassesByShardedTestType.get(ShardedTestType.RUN_TEST_ON_SHARD);
    List<Method> chunkRunTestOnShardMethods = Lists.newArrayList();
    for (Class runTestOnShardTestClass : allRunTestOnShardTestClasses) {
      int chunk = ((RunTestOnShard) runTestOnShardTestClass.getAnnotation(RunTestOnShard.class)).value();
      if (chunk == (chunkConfig.runChunk - 1)) {
        List<Method> runTestOnShardTestMethods = getTestMethods(runTestOnShardTestClass);
        chunkRunTestOnShardMethods.addAll(runTestOnShardTestMethods);
        chunkTestMethods.addAll(runTestOnShardTestMethods);
      }
    }

    // Shard Cucumber test classes
    final List<Class<?>> chunkCucumberTests;
    List<Class<?>> allCucumberTests = this.testClasses.stream()
        .filter(testClass -> {
          RunWith runWith = testClass.getAnnotation(RunWith.class);
          return runWith != null && Cucumber.class.isAssignableFrom(runWith.value());
        }).collect(Collectors.toList());
    ChunkIndexes cucumberIndexes = chunkConfig.getChunkIndexes(allCucumberTests.size());
    if (cucumberIndexes.size() > 0) {
      chunkCucumberTests = allCucumberTests.subList(cucumberIndexes.getStartIndex(), cucumberIndexes.getEndIndex());
    } else {
      chunkCucumberTests = ImmutableList.of();
    }

    chunkTestMethods.sort(Comparator.comparing(Method::toString));

    out.print("*************** METHOD PARTITIONING ****************\n");
    out.printf("*            Test Methods: %d\n", chunkTestMethods.size());
    out.printf("* @RunTestOnShard Methods: %d\n", chunkRunTestOnShardMethods.size());
    out.print("****************************************************\n");
    chunkTestMethods.forEach(method -> {
      out.printf(" - %s#%s%s\n",
          method.getDeclaringClass().getCanonicalName(),
          method.getName(),
          chunkRunTestOnShardMethods.contains(method) ? " [RUN_ON_SHARD]" : "");
    });

    Set<Class<?>> chunkTestClasses = Sets.newHashSet();
    chunkTestMethods.forEach(method -> chunkTestClasses.add(method.getDeclaringClass()));
    chunkTestClasses.addAll(chunkCucumberTests);
    return new ArrayList<>(chunkTestClasses);
  }

  @Override
  public Filter getFilter(ChunkConfig chunkConfig) {
    return new TestMethodFilter(chunkTestMethods);
  }

  private List<Method> getChunkTestMethods(ChunkConfig chunkConfig,
      ListMultimap<ShardedTestType, Class<?>> testClassesByShardedTestType) {
    List<Method> allTestMethods = new ArrayList<>();

    allTestMethods.addAll(getTestMethods(testClassesByShardedTestType.get(ShardedTestType.SLOW_TEST)));
    allTestMethods.addAll(getTestMethods(testClassesByShardedTestType.get(ShardedTestType.NORMAL_TEST)));

    allTestMethods.sort(Comparator.comparing(method -> murmur.hashString(method.toString(),
        Charset.defaultCharset()).toString()));
    ChunkIndexes chunkIndexes = chunkConfig.getChunkIndexes(allTestMethods.size());
    if (chunkIndexes.size() > 0) {
      chunkTestMethods = allTestMethods.subList(chunkIndexes.getStartIndex(), chunkIndexes.getEndIndex());
    }
    return chunkTestMethods;
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
