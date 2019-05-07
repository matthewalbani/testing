package com.squareup.testing.strategies;

import com.google.common.collect.ListMultimap;
import com.google.common.collect.Lists;
import com.squareup.testing.ChunkConfig;
import com.squareup.testing.RunTestOnShard;
import com.squareup.testing.ShardedTestType;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

import static com.google.common.base.Preconditions.checkNotNull;

public class ClassEvenMethodsShardingStrategy extends AbstractShardingStrategy {
  @Override public List<Class<?>> getClassesForChunk(ChunkConfig chunkConfig, PrintStream out) {
    checkNotNull(testClasses, "setTestClasses should be called before getClassesForChunk");
    List<Class<?>> chunkTestClasses = new ArrayList<>();

    ListMultimap<ShardedTestType, Class<?>> testClassesByShardedTestType =
        ShardedTestType.getTestClassesByShardedTestType(testClasses);
    List<Class<?>> allRunTestOnShardTests = testClassesByShardedTestType.get(ShardedTestType.RUN_TEST_ON_SHARD);
    List<Class<?>> allSlowTests = testClassesByShardedTestType.get(ShardedTestType.SLOW_TEST);
    List<Class<?>> allNormalTests = testClassesByShardedTestType.get(ShardedTestType.NORMAL_TEST);

    List<Class<?>> nonRunTestOnShardTests = new ArrayList<>();
    nonRunTestOnShardTests.addAll(allSlowTests);
    nonRunTestOnShardTests.addAll(allNormalTests);

    final List<ClassNumMethods> classesWithMethodCounts =
        nonRunTestOnShardTests.stream().map(clazz -> {
          List<Method> methods = Lists.newArrayList(clazz.getMethods());
          int numTestMethods =
              (int) methods.stream()
                  .filter(method -> method.isAnnotationPresent(Test.class))
                  .count();
          return new ClassNumMethods(clazz, numTestMethods);
        }).collect(Collectors.toList());

    classesWithMethodCounts.sort(Comparator.comparingInt(o -> o.numMethods));

    List<Integer> methodsPerChunk = new ArrayList<>(chunkConfig.chunks);
    List<List<Class<?>>> chunkedTestClasses = new ArrayList<>(chunkConfig.chunks);
    for (int i = 0; i < chunkConfig.chunks; i++) {
      methodsPerChunk.add(0);
      chunkedTestClasses.add(Lists.newArrayList());
    }
    if (!classesWithMethodCounts.isEmpty()) {
      // Try to evenly distribute test classes based on their method count in to chunks, with the
      // largest test classes by methods being distributed first.
      for (int i = nonRunTestOnShardTests.size() - 1; i >= 0; i--) {
        ClassNumMethods classNumMethods = classesWithMethodCounts.get(i);
        int minIndex = methodsPerChunk.indexOf(Collections.min(methodsPerChunk));
        chunkedTestClasses.get(minIndex).add(classNumMethods.clazz);
        methodsPerChunk.set(minIndex, methodsPerChunk.get(minIndex) + classNumMethods.numMethods);
      }
    }
    chunkTestClasses.addAll(chunkedTestClasses.get(chunkConfig.runChunk - 1));

    out.printf("*    Chunk Test Classes: %d\n", chunkTestClasses.size());
    out.print("****************************************************\n");
    for (int i = 0; i < methodsPerChunk.size(); i++) {
      out.printf("Chunk %d : numMethods = %d\n",(i + 1), methodsPerChunk.get(i));
    }


    List<Class<?>> chunkRunTestOnShardTests = allRunTestOnShardTests.stream()
        .filter(clazz ->
            clazz.getAnnotation(RunTestOnShard.class).value() == (chunkConfig.runChunk - 1))
        .collect(Collectors.toList());
    chunkTestClasses.addAll(chunkRunTestOnShardTests);
    out.print("****************************************************\n");
    out.print("Tests to run:\n");
    chunkTestClasses.forEach(clazz -> out.printf(" - %s\n", clazz.getName()));
    if (chunkRunTestOnShardTests.size() > 0) {
      out.print("@RunTestOnShard tests to run:\n");
      chunkRunTestOnShardTests.forEach(clazz -> out.printf(" - %s\n", clazz.getName()));
    }

    return chunkTestClasses;
  }

  private class ClassNumMethods {
    Class<?> clazz;
    int numMethods;

    ClassNumMethods(Class<?> clazz, int numMethods) {
      this.clazz = clazz;
      this.numMethods = numMethods;
    }
  }
}
