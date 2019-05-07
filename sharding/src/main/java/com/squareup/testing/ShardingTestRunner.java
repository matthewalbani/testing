package com.squareup.testing;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ListMultimap;
import com.squareup.common.Strings2;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.RunnerBuilder;
import org.reflections.Reflections;

import static com.google.common.base.Preconditions.checkNotNull;
import static java.lang.reflect.Modifier.isAbstract;
import static java.lang.reflect.Modifier.isInterface;
import static java.util.stream.Collectors.toList;

/**
 * A test runner that allows tests to be partitioned across multiple Kochiku workers.
 * <p>
 * To use this runner, create a placeholder class and annotate it as follows:
 * </p>
 * <pre>
 *
 *   @RunWith(ShardingTestRunner.class)
 *   @ShardedTestSuite(packageName = "com.squareup.myservice")
 *   class AllMyServiceTests {
 *   }
 * </pre>
 */
public class ShardingTestRunner extends ParentRunner<Runner> {
  // Get access to STDOUT manually since Surefire redirects System.out.
  public static final PrintStream out = new PrintStream(new FileOutputStream(FileDescriptor.out));

  final ChunkConfig chunkConfig;
  final String shardedPackagePrefix;
  private final List<Runner> runners;
  private final Class<? extends Annotation> onlyIfAnnotated;
  private final Class<? extends Annotation> unlessAnnotated;
  private final ShardingStrategies shardingStrategy;
  private final TestOrderings testOrdering;

  public ShardingTestRunner(Class<?> testClass) throws InitializationError {
    this(testClass, ChunkConfig.get());
  }

  ShardingTestRunner(Class<?> testClass, ChunkConfig chunkConfig) throws InitializationError {
    super(testClass);

    ShardedTestSuite annotation = testClass.getAnnotation(ShardedTestSuite.class);
    checkNotNull(annotation, "Expected @ShardedTestSuite annotation on %s", testClass);
    String shardRange = annotation.shardRange();
    if (!Strings2.isBlank(shardRange)) {
      int fromShard = Integer.parseInt(shardRange.substring(0, shardRange.indexOf('-')));
      int toShard = Integer.parseInt(shardRange.substring(shardRange.indexOf('-') + 1));
      if (chunkConfig.runChunk < fromShard || chunkConfig.runChunk > toShard) {
        this.chunkConfig = new ChunkConfig(0, 0, false);
      } else {
        this.chunkConfig =
            new ChunkConfig(toShard - fromShard + 1, chunkConfig.runChunk - fromShard + 1,
                chunkConfig.runFunctionalTests);
      }
    } else {
      this.chunkConfig = chunkConfig;
    }

    shardedPackagePrefix = annotation.packagePrefix();
    onlyIfAnnotated = annotation.onlyIfAnnotated() != Annotation.class
        ? annotation.onlyIfAnnotated()
        : null;
    unlessAnnotated = annotation.unlessAnnotated() != Annotation.class
        ? annotation.unlessAnnotated()
        : null;

    if (onlyIfAnnotated != null && unlessAnnotated != null) {
      throw new IllegalArgumentException("cannot use both onlyIfAnnotated() and unlessAnnotated()");
    }

    shardingStrategy = annotation.shardingStrategy();
    testOrdering = annotation.testOrdering();

    Filter onlyIfAnnotatedFilter = new Filter() {
      @Override public boolean shouldRun(Description description) {
        if (description.isSuite() || onlyIfAnnotated == null) {
          return true;
        }
        return description.getTestClass().isAnnotationPresent(onlyIfAnnotated) ||
            description.getAnnotation(onlyIfAnnotated) != null;
      }

      @Override public String describe() {
        return "only if annotation shard filter";
      }
    };

    Filter unlessAnnotatedFilter = new Filter() {
      @Override public boolean shouldRun(Description description) {
        if (description.isSuite() || unlessAnnotated == null) {
          return true;
        }
        return !description.getTestClass().isAnnotationPresent(unlessAnnotated) &&
            description.getAnnotation(unlessAnnotated) == null;
      }

      @Override public String describe() {
        return "unless annotation shard filter";
      }
    };

    RunnerBuilder builder = new AllDefaultPossibilitiesBuilder(true);
    this.runners = builder.runners(null, getTestClasses(shardedPackagePrefix, this.chunkConfig));

    Filter shardingStrategyFilter = shardingStrategy.getFilter(this.chunkConfig);
    for (Runner runner : this.runners) {
      try {
        onlyIfAnnotatedFilter.apply(runner);
        unlessAnnotatedFilter.apply(runner);
      } catch (NoTestsRemainException e) {
        // This should be ok since it happens if nothing is whitelisted or everything is blacklisted
        continue;
      }
      if (shardingStrategyFilter != null) {
        try {
          shardingStrategyFilter.apply(runner);
        } catch (NoTestsRemainException e) {
          throw new RuntimeException("Test shard did not find tests for " + runner.getDescription().toString(), e);
        }
      }
    }
  }

  @Override protected List<Runner> getChildren() {
    return this.runners;
  }

  @Override protected Description describeChild(Runner child) {
    return child.getDescription();
  }

  @Override protected void runChild(Runner child, RunNotifier notifier) {
    child.run(notifier);
  }

  /**
   * Search for all classes declaring either JUnit3 or JUnit4 style tests in the configured
   * package.
   *
   * @param packagePrefix Package name to search
   * @param chunkConfig the chunking config to use
   * @return Array of classes found in the package for testing.
   */
  private Class<?>[] getTestClasses(String packagePrefix, ChunkConfig chunkConfig) {
    List<Class<?>> chunkTestClasses = ImmutableList.of();

    if (chunkConfig.chunks > 0) {
      List<Class<?>> allTestClasses = getAllTestClasses(packagePrefix, chunkConfig);
      if (!allTestClasses.isEmpty()) {
        allTestClasses.sort(testOrdering);

        ListMultimap<ShardedTestType, Class<?>> testClassesByShardedTestType =
            ShardedTestType.getTestClassesByShardedTestType(allTestClasses);

        List<Class<?>> runTestOnShardTestClasses =
            testClassesByShardedTestType.get(ShardedTestType.RUN_TEST_ON_SHARD);
        int slowTestsCount = testClassesByShardedTestType.get(ShardedTestType.SLOW_TEST).size();
        int normalTestsCount = testClassesByShardedTestType.get(ShardedTestType.NORMAL_TEST).size();

        runTestOnShardTestClasses.forEach(
            testClassWithAnnotation -> checkTargetedTestClass(testClassWithAnnotation, chunkConfig));

        printTestPreamble(chunkConfig, slowTestsCount, normalTestsCount, runTestOnShardTestClasses.size());
        shardingStrategy.setTestClasses(allTestClasses);
        chunkTestClasses = shardingStrategy.getClassesForChunk(chunkConfig, out);
        chunkTestClasses.sort(testOrdering);
      } else {
        out.println("*** No test classes found ***");
      }
    } else {
      out.println("*** Test partitioning DISABLED ***\n");
    }
    return chunkTestClasses.toArray(new Class<?>[0]);
  }

  private void printTestPreamble(ChunkConfig chunkConfig, int slowTestCount, int normalTestCount, int runTestOnShardTestCount) {
    out.print("************** PARTITIONED TEST SUITE **************\n");
    out.printf("*                 Chunk: %d of %d\n", chunkConfig.runChunk, chunkConfig.chunks);
    out.printf("*            Slow Tests: %d\n", slowTestCount);
    out.printf("*        Non-Slow Tests: %d\n", normalTestCount);
    out.printf("* @RunTestOnShard Tests: %d\n", runTestOnShardTestCount);
  }

  private void checkTargetedTestClass(Class<?> testClassWithAnnotation, ChunkConfig chunkConfig) {
    RunTestOnShard annotation = testClassWithAnnotation.getAnnotation(RunTestOnShard.class);
    Preconditions.checkArgument(
        annotation.value() >= 0 && annotation.value() < chunkConfig.chunks,
        "@RunTestOnShard needs to specify a shard in [0, %d), but was %d. See " +
            "java/kochiku.yml for shard definitions by project.",
        chunkConfig.chunks, annotation.value());
  }

  private List<Class<?>> getAllTestClasses(String packagePrefix, ChunkConfig chunkConfig) {
    if (chunkConfig.chunks == 0) {
      return ImmutableList.of();
    }

    Reflections reflections = new Reflections(packagePrefix, new JUnitTestsScanner(chunkConfig));

    List<String> testClassNames =
        new ArrayList<>(reflections.getStore().get("JUnitTestsScanner").get("UnitTestClass"));

    return testClassNames.parallelStream()
        .map(ShardingTestRunner::loadClass)
        .filter(clazz1 ->
            !isAbstract(clazz1.getModifiers())
                && !isInterface(clazz1.getModifiers())
                && !clazz1.equals(ShardingTestRunner.class))
        .collect(toList());
  }

  private static Class<?> loadClass(String clazzName) {
    try {
      return Thread.currentThread().getContextClassLoader().loadClass(clazzName);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
