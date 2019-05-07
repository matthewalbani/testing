package com.squareup.testing.strategies;

import com.squareup.testing.ChunkConfig;
import com.squareup.testing.ShardingStrategy;
import java.util.List;
import org.junit.runner.manipulation.Filter;

public abstract class AbstractShardingStrategy implements ShardingStrategy {
  protected List<Class<?>> testClasses;

  @Override public void setTestClasses(List<Class<?>> testClasses) {
    this.testClasses = testClasses;
  }

  @Override public Filter getFilter(ChunkConfig chunkConfig) {
    return null;
  }
}
