package com.squareup.testing;

import com.squareup.testing.strategies.ClassEvenMethodsShardingStrategy;
import com.squareup.testing.strategies.ClassShardingStrategy;
import com.squareup.testing.strategies.MethodShardingStrategy;
import com.squareup.testing.strategies.MethodSlowTestsShardingStrategy;
import java.io.PrintStream;
import java.util.List;
import org.junit.runner.manipulation.Filter;

public enum ShardingStrategies implements ShardingStrategy {
  /**
   * Shard tests by class
   */
  CLASS {
    @Override public ShardingStrategy buildShardingStrategy() {
      return new ClassShardingStrategy();
    }
  },

  /**
   * Shard by class and attempt to balance the number of test methods as evenly as possible
   */
  CLASS_EVEN_METHODS {
    @Override protected ShardingStrategy buildShardingStrategy() {
      return new ClassEvenMethodsShardingStrategy();
    }
  },

  /**
   * Shard tests by test methods
   */
  METHOD {
    @Override protected ShardingStrategy buildShardingStrategy() {
      return new MethodShardingStrategy();
    }
  },

  /**
   * Shard non-slow tests by class and slow tests by method
   */
  METHOD_SLOW_TESTS {
    @Override protected ShardingStrategy buildShardingStrategy() {
      return new MethodSlowTestsShardingStrategy();
    }
  };

  private ShardingStrategy shardingStrategy;
  protected abstract ShardingStrategy buildShardingStrategy();
  ShardingStrategies() {
    shardingStrategy = buildShardingStrategy();
  }

  @Override
  public void setTestClasses(List<Class<?>> testClasses) {
    shardingStrategy.setTestClasses(testClasses);
  }

  @Override
  public List<Class<?>> getClassesForChunk(ChunkConfig chunkConfig, PrintStream out) {
    return shardingStrategy.getClassesForChunk(chunkConfig, out);
  }

  @Override
  public Filter getFilter(ChunkConfig chunkConfig) {
    return shardingStrategy.getFilter(chunkConfig);
  }
}
